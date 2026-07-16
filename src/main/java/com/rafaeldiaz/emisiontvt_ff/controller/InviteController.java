package com.rafaeldiaz.emisiontvt_ff.controller;

import com.rafaeldiaz.emisiontvt_ff.entity.Investor;
import com.rafaeldiaz.emisiontvt_ff.entity.Invite;
import com.rafaeldiaz.emisiontvt_ff.entity.InviteStatus;
import com.rafaeldiaz.emisiontvt_ff.entity.Round;
import com.rafaeldiaz.emisiontvt_ff.repository.InvestorRepository;
import com.rafaeldiaz.emisiontvt_ff.service.InvestorService;
import com.rafaeldiaz.emisiontvt_ff.service.InviteService;
import com.rafaeldiaz.emisiontvt_ff.service.PaymentService;
import com.rafaeldiaz.emisiontvt_ff.service.TrmService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import static org.thymeleaf.util.NumberUtils.formatCurrency;

@Controller
@RequiredArgsConstructor
public class InviteController {

    private final InviteService inviteService;
    private final InvestorService investorService;
    private final PaymentService paymentService;
    private final InvestorRepository investorRepo;
    private final TrmService trmService;
    // ----------------------------------------------------------------
    // 1. Mostrar formulario de registro
    // ----------------------------------------------------------------
    @GetMapping("/invite/{code}")
    public String showForm(@PathVariable String code, Model model) {
        Invite invite = inviteService.findByCode(code)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invitación no válida"));

        if (invite.getStatus() != InviteStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.GONE, "Esta invitación ya ha sido utilizada o expiró");
        }

        model.addAttribute("invite", invite);
        return "invite-form";
    }

    // ----------------------------------------------------------------
    // 2. Procesar registro del inversor
    // ----------------------------------------------------------------
    @PostMapping("/invite/{code}/register")
    public String register(@PathVariable String code,
                           @RequestParam String fullName,
                           @RequestParam String docId,
                           @RequestParam String email,
                           @RequestParam String phone,
                           @RequestParam int quantity) {

        Invite invite = inviteService.findByCode(code)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invitación no encontrada"));

        // Valida que la invitación siga en PENDING (evita registros duplicados)
        if (invite.getStatus() != InviteStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La invitación ya no está disponible");
        }

        // Valida que la cantidad solicitada esté dentro del límite
        if (quantity < 1 || quantity > invite.getMaxQuantity()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cantidad de tokens no permitida. El máximo es " + invite.getMaxQuantity());
        }

        Investor investor = investorService.register(invite, fullName, docId, email, phone, quantity);
        return "redirect:/invite/" + code + "/payment?inv=" + investor.getId();
    }

    // ----------------------------------------------------------------
    // 3. Página de pago
    // ----------------------------------------------------------------
    @GetMapping("/invite/{code}/payment")
    public String paymentPage(@PathVariable String code,
                              @RequestParam Long inv,
                              Model model) {
        Investor investor = investorRepo.findById(inv)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inversor no encontrado"));

        Invite invite = investor.getInvite();
        if (!invite.getCode().equals(code)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El inversor no corresponde a esta invitación");
        }

        Round round = invite.getRound();
        int quantity = investor.getQuantity();
        BigDecimal unitPrice = round.getPriceUsd();
        BigDecimal totalUSD = unitPrice.multiply(BigDecimal.valueOf(quantity));

        // Formatear USD sin decimales y con punto de miles
        String totalUSDFormatted = formatPesosColombianos(totalUSD);

        // Obtener TRM y calcular COP
        BigDecimal trm = null;
        BigDecimal totalCOP = null;
        String totalCOPFormatted = null;
        try {
            trm = trmService.getTrm();
            totalCOP = totalUSD.multiply(trm);
            totalCOPFormatted = formatPesosColombianos(totalCOP);
        } catch (Exception e) {
            model.addAttribute("trmError", "No se pudo obtener la TRM. Realiza el pago usando un valor aproximado.");
        }

        // Atributos para la vista
        model.addAttribute("roundName", round.getName());
        model.addAttribute("quantity", quantity);
        model.addAttribute("totalUSDFormatted", totalUSDFormatted);
        model.addAttribute("totalCOPFormatted", totalCOPFormatted);
        model.addAttribute("totalCOP", totalCOP);               // BigDecimal sin formato (para el hidden)
        model.addAttribute("trm", trm);                         // BigDecimal
        model.addAttribute("investorId", inv);
        model.addAttribute("inviteCode", code);

        return "payment";
    }

    // ----------------------------------------------------------------
    // 4. Subir comprobante de pago
    // ----------------------------------------------------------------
    @PostMapping("/invite/{code}/payment")
    public String submitPayment(@PathVariable String code,
                                @RequestParam Long inv,
                                @RequestParam BigDecimal amount,
                                @RequestParam("file") MultipartFile file) throws IOException {

        Investor investor = investorRepo.findById(inv)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inversor no encontrado"));

        Invite invite = investor.getInvite();
        if (!invite.getCode().equals(code)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El inversor no coincide con la invitación");
        }

        // Verifica que la invitación esté en estado REGISTERED (o PENDING si no se cambió)
        if (invite.getStatus() != InviteStatus.REGISTERED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El pago ya fue enviado o la invitación no está activa");
        }

        // Verifica que el archivo no esté vacío
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe adjuntar el comprobante de pago");
        }

        BigDecimal expectedUSD = invite.getRound().getPriceUsd()
                .multiply(BigDecimal.valueOf(investor.getQuantity()));
        BigDecimal expectedCOP = expectedUSD.multiply(trmService.getTrm());
        if (amount.compareTo(expectedCOP) != 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El monto no coincide con el total a pagar (COP).");
        }

        paymentService.submitPayment(investor, amount, file); // amount en COP
        return "thank-you";
    }

    private String formatPesosColombianos(BigDecimal amount) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.forLanguageTag("es-CO"));
        symbols.setGroupingSeparator('.');
        DecimalFormat df = new DecimalFormat("#,##0", symbols);
        return df.format(amount);
    }
}

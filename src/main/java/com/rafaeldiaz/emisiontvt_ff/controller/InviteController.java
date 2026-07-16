package com.rafaeldiaz.emisiontvt_ff.controller;

import com.rafaeldiaz.emisiontvt_ff.entity.Investor;
import com.rafaeldiaz.emisiontvt_ff.entity.Invite;
import com.rafaeldiaz.emisiontvt_ff.entity.InviteStatus;
import com.rafaeldiaz.emisiontvt_ff.repository.InvestorRepository;
import com.rafaeldiaz.emisiontvt_ff.service.InvestorService;
import com.rafaeldiaz.emisiontvt_ff.service.InviteService;
import com.rafaeldiaz.emisiontvt_ff.service.PaymentService;
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

@Controller
@RequiredArgsConstructor
public class InviteController {

    private final InviteService inviteService;
    private final InvestorService investorService;
    private final PaymentService paymentService;
    private final InvestorRepository investorRepo;

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

        BigDecimal unitPrice = invite.getRound().getPriceUsd();
        BigDecimal total = unitPrice.multiply(BigDecimal.valueOf(investor.getQuantity()));

        model.addAttribute("investorId", inv);
        model.addAttribute("total", total);
        model.addAttribute("roundName", invite.getRound().getName());
        model.addAttribute("quantity", investor.getQuantity());
        model.addAttribute("inviteCode", code);   // <-- AÑADIR esta línea

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

        // (Opcional) verifica que el monto enviado coincida con el total esperado
        BigDecimal expected = invite.getRound().getPriceUsd().multiply(BigDecimal.valueOf(investor.getQuantity()));
        if (amount.compareTo(expected) != 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El monto no coincide con el total a pagar");
        }

        paymentService.submitPayment(investor, amount, file);
        return "thank-you";
    }
}

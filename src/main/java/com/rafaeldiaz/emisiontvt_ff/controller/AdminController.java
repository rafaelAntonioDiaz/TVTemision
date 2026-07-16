package com.rafaeldiaz.emisiontvt_ff.controller;

import com.rafaeldiaz.emisiontvt_ff.entity.*;
import com.rafaeldiaz.emisiontvt_ff.repository.InvestorRepository;
import com.rafaeldiaz.emisiontvt_ff.repository.InviteRepository;
import com.rafaeldiaz.emisiontvt_ff.repository.PaymentRepository;
import com.rafaeldiaz.emisiontvt_ff.repository.RoundRepository;
import com.rafaeldiaz.emisiontvt_ff.service.InviteService;
import com.rafaeldiaz.emisiontvt_ff.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final InviteRepository inviteRepo;
    private final InvestorRepository investorRepo;
    private final RoundRepository roundRepo;
    private final PaymentRepository paymentRepo;
    private final TokenService tokenService;
    private final InviteService inviteService;

    @Value("${app.base-url}")
    private String baseUrl;

    // ----------------------------------------------------------------------
    // 1. Dashboard con todas las invitaciones
    // ----------------------------------------------------------------------
    @GetMapping
    public String dashboard(Model model) {
        List<Invite> invites = inviteRepo.findAll();
        model.addAttribute("invites", invites);
        return "admin/dashboard";
    }

    // ----------------------------------------------------------------------
    // 2. Detalle de una invitación (inversor, pago, tokens)
    // ----------------------------------------------------------------------
    @GetMapping("/invite/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Invite invite = inviteRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invitación no encontrada"));

        Investor investor = investorRepo.findByInvite(invite).orElse(null);
        Payment payment = (investor != null) ? paymentRepo.findByInvestor(investor).orElse(null) : null;
        List<Token> tokens = (investor != null) ? tokenService.findByInvestor(investor) : List.of();

        model.addAttribute("invite", invite);
        model.addAttribute("investor", investor);
        model.addAttribute("payment", payment);
        model.addAttribute("tokens", tokens);
        model.addAttribute("baseUrl", baseUrl);
        return "admin/invite-detail";
    }

    // ----------------------------------------------------------------------
    // 3. Confirmar pago manualmente
    // ----------------------------------------------------------------------
    @PostMapping("/invite/{id}/confirm")
    public String confirm(@PathVariable Long id) throws IOException {
        Invite invite = inviteRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (invite.getStatus() != InviteStatus.PAID) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La invitación no está en estado PAID");
        }

        Investor investor = investorRepo.findByInvite(invite)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No hay inversor asociado"));

        // Genera los tokens y el PDF del contrato
        tokenService.generateTokens(investor);

        // Cambia el estado a CONFIRMED
        invite.setStatus(InviteStatus.CONFIRMED);
        inviteRepo.save(invite);

        // Marca el pago como verificado
        Payment payment = paymentRepo.findByInvestor(investor).orElse(null);
        if (payment != null) {
            payment.setVerified(true);
            payment.setVerifiedAt(java.time.LocalDateTime.now());
            paymentRepo.save(payment);
        }

        // Redirige al detalle mostrando el código de reclamo
        return "redirect:/admin/invite/" + id + "?confirmed=true";    }

    // ----------------------------------------------------------------------
    // 4. Crear nueva invitación (mostrar formulario)
    // ----------------------------------------------------------------------
    @GetMapping("/create-invite")
    public String showCreateInviteForm(Model model) {
        List<Round> rounds = roundRepo.findAll();
        model.addAttribute("rounds", rounds);
        return "admin/create-invite";
    }

    // ----------------------------------------------------------------------
    // 5. Procesar creación de invitación
    // ----------------------------------------------------------------------
    @PostMapping("/create-invite")
    public String createInvite(@RequestParam Long roundId,
                               @RequestParam int maxQuantity,
                               Model model) {
        Round round = roundRepo.findById(roundId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ronda no encontrada"));

        if (maxQuantity < 1 || maxQuantity > round.getMaxTokensPerInvestor()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La cantidad debe estar entre 1 y " + round.getMaxTokensPerInvestor());
        }

        Invite invite = inviteService.createInvite(round, maxQuantity);
        model.addAttribute("invite", invite);
        return "redirect:/admin/invite/" + invite.getId();
    }

    @GetMapping("/payment/{id}/proof")
    @ResponseBody
    public ResponseEntity<Resource> viewProof(@PathVariable Long id) {
        Payment payment = paymentRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pago no encontrado"));

        Path path = Paths.get(payment.getProofFilePath());
        if (!Files.exists(path)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Archivo no encontrado");
        }

        byte[] bytes;
        try {
            bytes = Files.readAllBytes(path);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al leer el comprobante");
        }

        ByteArrayResource resource = new ByteArrayResource(bytes);

        // Determinar el tipo de contenido (imagen)
        MediaType mediaType;
        try {
            String mimeType = Files.probeContentType(path);
            mediaType = mimeType != null ? MediaType.parseMediaType(mimeType) : MediaType.IMAGE_JPEG;
        } catch (IOException e) {
            mediaType = MediaType.IMAGE_JPEG;
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .body((Resource) resource);
    }
    @GetMapping("/admin/login")
    public String loginPage() {

        return "admin/login";
    }
}
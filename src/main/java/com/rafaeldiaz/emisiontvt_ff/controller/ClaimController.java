package com.rafaeldiaz.emisiontvt_ff.controller;


import com.rafaeldiaz.emisiontvt_ff.config.FractalParams;
import com.rafaeldiaz.emisiontvt_ff.entity.Investor;
import com.rafaeldiaz.emisiontvt_ff.entity.InviteStatus;
import com.rafaeldiaz.emisiontvt_ff.entity.Token;
import com.rafaeldiaz.emisiontvt_ff.repository.InvestorRepository;
import com.rafaeldiaz.emisiontvt_ff.repository.TokenRepository;
import com.rafaeldiaz.emisiontvt_ff.service.FractalService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ClaimController {

    private final InvestorRepository investorRepo;
    private final TokenRepository tokenRepo;
    private final FractalService fractalService;

    // -------------------------------------------------------------------------
    // 1. Página de reclamo (experiencia fractal animada)
    // -------------------------------------------------------------------------
    @GetMapping("/claim/{claimCode}")
    public String claimPage(@PathVariable String claimCode, Model model) {
        Investor investor = investorRepo.findByClaimCode(claimCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Código de reclamo inválido"));

        if (investor.getInvite().getStatus() != InviteStatus.CONFIRMED) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Tu pago aún no ha sido confirmado. Te avisaremos pronto.");
        }

        model.addAttribute("claimCode", claimCode);
        return "claim";
    }

    // -------------------------------------------------------------------------
    // 2. Parámetros del fractal para la animación (JSON)
    // -------------------------------------------------------------------------
    @GetMapping("/api/claim/{claimCode}/fractal-params")
    @ResponseBody
    public FractalParams fractalParams(@PathVariable String claimCode) {
        Investor investor = investorRepo.findByClaimCode(claimCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (investor.getInvite().getStatus() != InviteStatus.CONFIRMED) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Pago no confirmado aún");
        }

        Token firstToken = tokenRepo.findByInvestor(investor)
                .stream()
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "No se encontraron tokens generados"));

        return fractalService.fromJson(firstToken.getFractalParams());
    }

    // -------------------------------------------------------------------------
    // 3. Descargar el certificado de arte (imagen fractal PNG)
    // -------------------------------------------------------------------------
    @GetMapping("/api/claim/{claimCode}/fractal-image")
    @ResponseBody
    public ResponseEntity<Resource> downloadFractalImage(@PathVariable String claimCode) {
        Investor investor = getConfirmedInvestor(claimCode);
        Token firstToken = tokenRepo.findByInvestor(investor)
                .stream()
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "No se encontraron tokens"));

        byte[] imageBytes = loadFileBytes(firstToken.getFractalImagePath());
        ByteArrayResource resource = new ByteArrayResource(imageBytes);

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"certificado-tvt-" + claimCode + ".png\"")
                .body(resource);
    }

    // -------------------------------------------------------------------------
    // 4. Descargar el contrato legal (PDF)
    // -------------------------------------------------------------------------
    @GetMapping("/api/claim/{claimCode}/contract")
    @ResponseBody
    public ResponseEntity<Resource> downloadContract(@PathVariable String claimCode) {
        Investor investor = getConfirmedInvestor(claimCode);
        Token firstToken = tokenRepo.findByInvestor(investor)
                .stream()
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "No se encontraron tokens"));

        byte[] pdfBytes = loadFileBytes(firstToken.getContractPath());
        ByteArrayResource resource = new ByteArrayResource(pdfBytes);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"contrato-tvt-" + claimCode + ".pdf\"")
                .body(resource);
    }

    // -------------------------------------------------------------------------
    // Métodos privados de ayuda
    // -------------------------------------------------------------------------
    private Investor getConfirmedInvestor(String claimCode) {
        Investor investor = investorRepo.findByClaimCode(claimCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (investor.getInvite().getStatus() != InviteStatus.CONFIRMED) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Pago no confirmado aún");
        }
        return investor;
    }

    private byte[] loadFileBytes(String filePath) {
        try {
            Path path = Paths.get(filePath);
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error al leer el archivo solicitado");
        }
    }
    @GetMapping("/api/claim/{claimCode}/tokens-info")
    @ResponseBody
    public List<Map<String, Object>> tokensInfo(@PathVariable String claimCode) {
        Investor investor = investorRepo.findByClaimCode(claimCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (investor.getInvite().getStatus() != InviteStatus.CONFIRMED) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Pago no confirmado aún");
        }

        List<Token> tokens = tokenRepo.findByInvestor(investor);
        if (tokens.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No se encontraron tokens para este inversor");
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Token token : tokens) {
            Map<String, Object> info = new HashMap<>();
            info.put("tokenCode", token.getCode());
            info.put("fractalParams", fractalService.fromJson(token.getFractalParams()));
            info.put("imageUrl", "/api/claim/" + claimCode + "/fractal-image/" + token.getId());
            info.put("contractUrl", "/api/claim/" + claimCode + "/contract");
            result.add(info);
        }
        return result;
    }

    // Endpoint para descargar la imagen de un token específico
    @GetMapping("/api/claim/{claimCode}/fractal-image/{tokenId}")
    @ResponseBody
    public ResponseEntity<Resource> fractalImageByToken(@PathVariable String claimCode,
                                                        @PathVariable Long tokenId) {
        Token token = tokenRepo.findById(tokenId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        byte[] bytes = loadFileBytes(token.getFractalImagePath());
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + token.getCode() + ".png\"")
                .body(new ByteArrayResource(bytes));
    }
}
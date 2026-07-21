package com.rafaeldiaz.emisiontvt_ff.service;

import com.rafaeldiaz.emisiontvt_ff.config.FractalParams;
import com.rafaeldiaz.emisiontvt_ff.entity.*;

import com.rafaeldiaz.emisiontvt_ff.repository.InvestorRepository;
import com.rafaeldiaz.emisiontvt_ff.repository.TokenRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import com.itextpdf.html2pdf.HtmlConverter;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TokenService {
    private final TokenRepository tokenRepo;
    private final FractalService fractalService;
    private final InvestorRepository investorRepo;
    private final TemplateEngine templateEngine; // Thymeleaf

    @Value("${app.upload.dir}")
    private String uploadDir;
    
    private int sequenceCounter = 1; // simplificado; en producción usa sincronización o BD

    public void generateTokens(Investor investor) throws IOException {
        Invite invite = investor.getInvite();
        Round round = invite.getRound();
        int qty = investor.getQuantity();
        // Verificar límite total de la ronda
        long tokensYaEmitidos = tokenRepo.countByRound(round);
        if (tokensYaEmitidos + qty > round.getTotalSupply()) {
            throw new IllegalStateException(
                    String.format("La ronda %s solo tiene %d tokens en total. Ya se han emitido %d.",
                            round.getName(), round.getTotalSupply(), tokensYaEmitidos)
            );
        }
        List<Token> tokens = new ArrayList<>();

        for (int i = 0; i < qty; i++) {
            String code = generateNextCode();
            FractalParams params = fractalService.generateParams(investor.getId(), code, round.getName());
            BufferedImage img = fractalService.renderStatic(params, 4000, 3000);
            String imgPath = fractalService.saveFractalImage(img, code);

            Token token = new Token();
            token.setCode(code);
            token.setInvestor(investor);
            token.setRound(round);
            token.setFractalParams(fractalService.toJson(params));
            token.setFractalImagePath(imgPath);
            tokens.add(token);
        }

        tokenRepo.saveAll(tokens);

        // Generar un único contrato PDF con todos los tokens
        String contractPath = generateContractPdf(investor, tokens);
        tokens.forEach(t -> t.setContractPath(contractPath));
        tokenRepo.saveAll(tokens);
    }

    private String generateNextCode() {
        // En producción deberías sincronizar o usar AtomicInteger/sequence en BD
        return String.format("TVT-F%04d", sequenceCounter++);
    }

    private String generateContractPdf(Investor investor, List<Token> tokens) throws IOException {
        Files.createDirectories(Paths.get(uploadDir + "contracts/"));
        String fileName = "contrato-" + investor.getId() + ".pdf";
        String outputPath = uploadDir + "contracts/" + fileName;

        // Preparar contexto para Thymeleaf
        Context ctx = new Context();
        ctx.setVariable("investor", investor);
        ctx.setVariable("tokens", tokens);
        ctx.setVariable("totalAmount", investor.getQuantity() * investor.getInvite().getRound().getPriceUsd().intValue());
        ctx.setVariable("currentDate", LocalDateTime.now().toLocalDate().toString());

        String htmlContent = templateEngine.process("contract-template", ctx);

        try (OutputStream os = new FileOutputStream(outputPath)) {
            HtmlConverter.convertToPdf(htmlContent, os);
        }

        return outputPath;
    }
    @PostConstruct
    void initCounter() {
        Token last = tokenRepo.findTopByOrderByIdDesc().orElse(null);
        if (last != null && last.getCode() != null) {
            String numPart = last.getCode().substring(4); // "TVT-F" tiene 5 caracteres? ajusta
            sequenceCounter = Integer.parseInt(numPart) + 1;
        }
    }

    public List<Token> findByInvestor(Investor investor) {
        return tokenRepo.findByInvestor(investor);
    }
}
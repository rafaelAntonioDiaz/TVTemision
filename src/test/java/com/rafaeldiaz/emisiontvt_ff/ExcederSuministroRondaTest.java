package com.rafaeldiaz.emisiontvt_ff;

import com.rafaeldiaz.emisiontvt_ff.entity.*;
import com.rafaeldiaz.emisiontvt_ff.repository.*;
import com.rafaeldiaz.emisiontvt_ff.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class ExcederSuministroRondaTest {

    @Autowired private RoundRepository roundRepo;
    @Autowired private InviteService inviteService;
    @Autowired private InvestorService investorService;
    @Autowired private TokenService tokenService;
    @Autowired private InvestorRepository investorRepo;
    @Autowired private InviteRepository inviteRepo;
    @Autowired private TokenRepository tokenRepo;
    @Autowired private PaymentRepository paymentRepo;

    private Round rondaFF;

    @BeforeEach
    void setUp() {
        rondaFF = roundRepo.findAll().stream()
                .filter(r -> r.getName().equals("F&F"))
                .findFirst()
                .orElseThrow();
    }

    @Test
    void deberiaRechazarTokenExtraCuandoRondaAgotada() throws Exception {
        // 1. Vender 20 tokens (4 inversores de a 5)
        for (int i = 1; i <= 4; i++) {
            Invite invite = inviteService.createInvite(rondaFF, 5);
            Investor investor = investorService.register(invite,
                    "Inversor " + i, "CC1000" + i, "inv" + i + "@test.com", "300000" + i, 5);
            // Simular pago sin archivo
            Payment payment = new Payment();
            payment.setInvestor(investor);
            payment.setAmount(BigDecimal.valueOf(2500));
            payment.setCurrency("USD");
            payment.setProofFilePath("test-path"); // ruta ficticia
            payment.setVerified(true);
            payment.setVerifiedAt(LocalDateTime.now());
            paymentRepo.save(payment);
            // Confirmar y generar tokens
            tokenService.generateTokens(investor);
            invite.setStatus(InviteStatus.CONFIRMED);
            inviteRepo.save(invite);
        }

        long tokensEmitidos = tokenRepo.countByRound(rondaFF);
        assertEquals(20, tokensEmitidos, "Deben haberse emitido 20 tokens");

        // 2. Intentar vender un token más
        Invite inviteExtra = inviteService.createInvite(rondaFF, 1);
        Investor investorExtra = investorService.register(inviteExtra,
                "Inversor Extra", "CC999", "extra@test.com", "300000", 1);
        Payment paymentExtra = new Payment();
        paymentExtra.setInvestor(investorExtra);
        paymentExtra.setAmount(BigDecimal.valueOf(500));
        paymentExtra.setCurrency("USD");
        paymentExtra.setProofFilePath("test-path");
        paymentExtra.setVerified(true);
        paymentExtra.setVerifiedAt(LocalDateTime.now());
        paymentRepo.save(paymentExtra);

        // Debe lanzar excepción al generar tokens
        assertThrows(IllegalStateException.class, () -> {
            tokenService.generateTokens(investorExtra);
        }, "Debería fallar porque la ronda está agotada");
    }
}
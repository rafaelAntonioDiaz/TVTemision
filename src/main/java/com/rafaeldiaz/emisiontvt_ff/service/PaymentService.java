package com.rafaeldiaz.emisiontvt_ff.service;

import com.rafaeldiaz.emisiontvt_ff.entity.Investor;
import com.rafaeldiaz.emisiontvt_ff.entity.InviteStatus;
import com.rafaeldiaz.emisiontvt_ff.entity.Payment;
import com.rafaeldiaz.emisiontvt_ff.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepo;
    private final InviteService inviteService;

    @Value("${app.upload.dir}")
    private final String uploadDir;

    public Payment submitPayment(Investor investor, BigDecimal amountCop, MultipartFile file) throws IOException {
        Files.createDirectories(Paths.get(uploadDir + "comprobantes/"));
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path path = Paths.get(uploadDir + "comprobantes/" + fileName);
        file.transferTo(path);

        Payment payment = new Payment();
        payment.setInvestor(investor);
        payment.setAmount(amountCop);       // ahora se guarda en COP
        payment.setCurrency("COP");         // indicamos la moneda
        payment.setProofFilePath(path.toString());
        paymentRepo.save(payment);
        inviteService.updateStatus(investor.getInvite(), InviteStatus.PAID);
        return payment;
    }
}

package com.rafaeldiaz.emisiontvt_ff.service;

import com.rafaeldiaz.emisiontvt_ff.entity.Investor;
import com.rafaeldiaz.emisiontvt_ff.entity.Invite;
import com.rafaeldiaz.emisiontvt_ff.entity.InviteStatus;
import com.rafaeldiaz.emisiontvt_ff.repository.InvestorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InvestorService {
    private final InvestorRepository investorRepo;
    private final InviteService inviteService;
    public Investor register(Invite invite, String fullName, String docId,
                             String email, String phone, int quantity) {
        if (quantity > invite.getMaxQuantity())
            throw new IllegalArgumentException("Cantidad excede el límite permitido");
        if (invite.getStatus() != InviteStatus.PENDING)
            throw new IllegalStateException("Invitación no válida");
        Investor investor = new Investor();
        investor.setInvite(invite);
        investor.setFullName(fullName);
        investor.setDocId(docId);
        investor.setEmail(email);
        investor.setPhone(phone);
        investor.setQuantity(quantity);
        investor = investorRepo.save(investor);
        inviteService.updateStatus(invite, InviteStatus.REGISTERED);
        return investor;
    }
    public Optional<Investor> findByClaimCode(String code) {
        return investorRepo.findByClaimCode(code);
    }
    public Investor save(Investor investor) { return investorRepo.save(investor); }
}

package com.rafaeldiaz.emisiontvt_ff.service;

import com.rafaeldiaz.emisiontvt_ff.entity.Invite;
import com.rafaeldiaz.emisiontvt_ff.entity.InviteStatus;
import com.rafaeldiaz.emisiontvt_ff.entity.Round;
import com.rafaeldiaz.emisiontvt_ff.repository.InviteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InviteService {
    private final InviteRepository inviteRepo;
    public Invite createInvite(Round round, int maxQty) {
        Invite invite = new Invite();
        invite.setRound(round);
        invite.setMaxQuantity(Math.min(maxQty, round.getMaxTokensPerInvestor()));
        return inviteRepo.save(invite);
    }
    public Optional<Invite> findByCode(String code) {
        return inviteRepo.findByCode(code);
    }
    public Invite updateStatus(Invite invite, InviteStatus status) {
        invite.setStatus(status);
        return inviteRepo.save(invite);
    }
}

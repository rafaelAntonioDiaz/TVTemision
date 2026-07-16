package com.rafaeldiaz.emisiontvt_ff.repository;

import com.rafaeldiaz.emisiontvt_ff.entity.Investor;
import com.rafaeldiaz.emisiontvt_ff.entity.Invite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InvestorRepository extends JpaRepository<Investor, Long> {
    Optional<Investor> findByInvite(Invite invite);
    Optional<Investor> findByClaimCode(String claimCode);
}
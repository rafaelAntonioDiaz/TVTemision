package com.rafaeldiaz.emisiontvt_ff.repository;

import com.rafaeldiaz.emisiontvt_ff.entity.Invite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InviteRepository extends JpaRepository<Invite, Long> {
    Optional<Invite> findByCode(String code);
}

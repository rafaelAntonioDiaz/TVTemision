package com.rafaeldiaz.emisiontvt_ff.repository;

import com.rafaeldiaz.emisiontvt_ff.entity.Investor;
import com.rafaeldiaz.emisiontvt_ff.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByInvestor(Investor investor);
}

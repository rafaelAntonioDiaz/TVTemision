package com.rafaeldiaz.emisiontvt_ff.repository;

import com.rafaeldiaz.emisiontvt_ff.entity.Investor;
import com.rafaeldiaz.emisiontvt_ff.entity.Round;
import com.rafaeldiaz.emisiontvt_ff.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {
    List<Token> findByInvestor(Investor investor);
    Optional<Token> findTopByOrderByIdDesc(); // para obtener último código

    long countByRound(Round round);
}

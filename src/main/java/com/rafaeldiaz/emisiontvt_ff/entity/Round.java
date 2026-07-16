package com.rafaeldiaz.emisiontvt_ff.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Round {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String name;        // F&F, HITO1, HITO2, CIRCULO
    private BigDecimal priceUsd;
    private int maxTokensPerInvestor;
    private int totalSupply;
}
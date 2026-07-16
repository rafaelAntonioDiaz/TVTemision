package com.rafaeldiaz.emisiontvt_ff.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Data @NoArgsConstructor @AllArgsConstructor
public class Payment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne
    private Investor investor;
    private BigDecimal amount;
    private String currency = "USD";
    private String proofFilePath; // ruta del comprobante subido
    private boolean verified = false;
    private LocalDateTime verifiedAt;
}
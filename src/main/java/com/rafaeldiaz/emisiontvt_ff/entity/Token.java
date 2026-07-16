package com.rafaeldiaz.emisiontvt_ff.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Data @NoArgsConstructor @AllArgsConstructor
public class Token {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String code;        // TVT-F0001
    @ManyToOne
    private Investor investor;
    @ManyToOne
    private Round round;
    // Parámetros en JSON: {"startRe":..., "startIm":..., "startZoom":..., "targetRe":..., ...}
    @Column(columnDefinition = "TEXT")
    private String fractalParams;
    private String fractalImagePath;   // PNG estático generado en Java
    private String contractPath;       // PDF del contrato
}
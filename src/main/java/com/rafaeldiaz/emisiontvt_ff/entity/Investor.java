package com.rafaeldiaz.emisiontvt_ff.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity @Data @NoArgsConstructor @AllArgsConstructor
public class Investor {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne
    private Invite invite;
    private String fullName;
    private String docId;
    private String email;
    private String phone;
    private int quantity; // tokens comprados
    @Column(unique = true)
    private String claimCode = UUID.randomUUID().toString(); // enlace de reclamo
}
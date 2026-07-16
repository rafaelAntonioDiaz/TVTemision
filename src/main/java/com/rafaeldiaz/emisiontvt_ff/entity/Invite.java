package com.rafaeldiaz.emisiontvt_ff.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity @Data @NoArgsConstructor @AllArgsConstructor
public class Invite {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String code = UUID.randomUUID().toString();
    @ManyToOne
    private Round round;
    private int maxQuantity; // máximo de tokens que puede comprar este inversor
    @Enumerated(EnumType.STRING)
    private InviteStatus status = InviteStatus.PENDING;
}

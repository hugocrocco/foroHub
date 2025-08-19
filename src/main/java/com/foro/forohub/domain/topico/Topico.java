package com.foro.forohub.domain.topico;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "topicos",
        uniqueConstraints = @UniqueConstraint(name="uk_topicos_titulo_mensaje", columnNames = {"titulo","mensaje"}))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Topico {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 200, nullable = false)
    private String titulo;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String mensaje;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Builder.Default
    @Column(nullable = false)
    private String status = "ABIERTO";

    @Column(nullable = false)
    private String autor;

    @Column(nullable = false)
    private String curso;
}
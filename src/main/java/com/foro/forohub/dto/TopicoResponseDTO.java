package com.foro.forohub.api.dto;

import com.foro.forohub.domain.topico.StatusTopico;
import java.time.LocalDateTime;

public record TopicoResponseDTO(
        Long id,
        String titulo,
        String mensaje,
        LocalDateTime fechaCreacion,
        StatusTopico status,
        String autor,
        String curso
) {}
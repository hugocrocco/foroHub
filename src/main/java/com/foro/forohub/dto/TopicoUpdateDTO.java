package com.foro.forohub.api.dto;

import jakarta.validation.constraints.Size;

public record TopicoUpdateDTO(
        @Size(max = 200) String titulo,
        String mensaje,
        String autor,
        String curso
) {}
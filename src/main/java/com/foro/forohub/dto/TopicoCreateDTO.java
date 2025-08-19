package com.foro.forohub.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TopicoCreateDTO(
        @NotBlank @Size(max = 200) String titulo,
        @NotBlank String mensaje,
        @NotBlank @Size(max = 100) String autor,
        @NotBlank @Size(max = 100) String curso
) {}
package com.foro.forohub.api;

import com.foro.forohub.api.dto.TopicoCreateDTO;
import com.foro.forohub.api.dto.TopicoUpdateDTO;
import com.foro.forohub.api.dto.TopicoResponseDTO;
import com.foro.forohub.domain.topico.Topico;
import com.foro.forohub.domain.topico.TopicoRepository;
import com.foro.forohub.domain.topico.StatusTopico;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

import static org.springframework.data.domain.Sort.Direction.DESC;

@RestController
@RequestMapping("/topicos")
public class TopicoController {

    private final TopicoRepository repository;

    public TopicoController(TopicoRepository repository) {
        this.repository = repository;
    }

    // CREATE
    @PostMapping
    @Transactional
    public ResponseEntity<?> crear(
            @RequestBody @Valid TopicoCreateDTO dto,
            UriComponentsBuilder uriBuilder
    ) {
        if (repository.existsByTituloAndMensaje(dto.titulo(), dto.mensaje())) {
            return ResponseEntity.unprocessableEntity().build(); // 422
        }

        Topico topico = Topico.builder()
                .titulo(dto.titulo())
                .mensaje(dto.mensaje())
                .autor(dto.autor())
                .curso(dto.curso())
                .build();

        topico = repository.save(topico);

        TopicoResponseDTO body = toDto(topico);
        URI location = uriBuilder.path("/topicos/{id}").buildAndExpand(topico.getId()).toUri();
        return ResponseEntity.created(location).body(body); // 201
    }

    // READ (lista paginada)
    @GetMapping
    public ResponseEntity<Page<TopicoResponseDTO>> listar(
            @PageableDefault(size = 10, sort = "fechaCreacion", direction = DESC) Pageable pageable
    ) {
        Page<TopicoResponseDTO> page = repository.findAll(pageable).map(this::toDto);
        return ResponseEntity.ok(page); // 200
    }

    // READ (detalle)
    @GetMapping("/{id}")
    public ResponseEntity<TopicoResponseDTO> detalle(@PathVariable Long id) {
        return repository.findById(id)
                .map(topico -> ResponseEntity.ok(toDto(topico)))
                .orElseGet(() -> ResponseEntity.notFound().build()); // 404 si no existe
    }

    // UPDATE (parcial)
    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> actualizar(
            @PathVariable Long id,
            @RequestBody @Valid TopicoUpdateDTO dto
    ) {
        return repository.findById(id).map(topico -> {
            // Calcular valores efectivos para la validación de duplicados
            String tituloEfectivo = dto.titulo() != null ? dto.titulo() : topico.getTitulo();
            String mensajeEfectivo = dto.mensaje() != null ? dto.mensaje() : topico.getMensaje();

            // Evitar duplicados (mismo titulo+mensaje en otro id)
            if (repository.existsByTituloAndMensajeAndIdNot(tituloEfectivo, mensajeEfectivo, id)) {
                return ResponseEntity.unprocessableEntity().build(); // 422
            }

            // Aplicar cambios parciales
            if (dto.titulo() != null)  topico.setTitulo(dto.titulo());
            if (dto.mensaje() != null) topico.setMensaje(dto.mensaje());
            if (dto.autor() != null)   topico.setAutor(dto.autor());
            if (dto.curso() != null)   topico.setCurso(dto.curso());

            Topico guardado = repository.save(topico);
            return ResponseEntity.ok(toDto(guardado)); // 200
        }).orElseGet(() -> ResponseEntity.notFound().build()); // 404
    }

    // DELETE
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (!repository.existsById(id)) return ResponseEntity.notFound().build(); // 404
        repository.deleteById(id);
        return ResponseEntity.noContent().build(); // 204
    }

    private TopicoResponseDTO toDto(Topico t) {
        return new TopicoResponseDTO(
                t.getId(), t.getTitulo(), t.getMensaje(),
                t.getFechaCreacion(), StatusTopico.valueOf(t.getStatus()),
                t.getAutor(), t.getCurso()
        );
    }
}
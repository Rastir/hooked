package com.flaco.hooked.domain.controller;

import com.flaco.hooked.domain.request.*;
import com.flaco.hooked.domain.response.*;
import com.flaco.hooked.domain.service.CategoriaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
@Validated
public class CategoriaController {

    @Autowired private CategoriaService categoriaService;

    // ========== CRUD ==========

    @PostMapping
    public ResponseEntity<CategoriaResponse> crear(@Valid @RequestBody CrearCategoriaRequest request) {
        CategoriaResponse creada = categoriaService.crearCategoria(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .header("X-Category-Created", "true")
                .header("X-Category-ID", creada.getId().toString())
                .body(creada);
    }

    @GetMapping
    public ResponseEntity<List<CategoriaResponse>> listar() {
        List<CategoriaResponse> categorias = categoriaService.obtenerTodasLasCategorias();

        return ResponseEntity.ok()
                .header("X-Total-Categories", String.valueOf(categorias.size()))
                .header("Cache-Control", "public, max-age=600")
                .body(categorias);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoriaResponse> obtener(@PathVariable @Positive Long id) {
        CategoriaResponse categoria = categoriaService.obtenerCategoriaPorId(id);

        return ResponseEntity.ok()
                .header("X-Category-ID", categoria.getId().toString())
                .header("Cache-Control", "public, max-age=300")
                .body(categoria);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoriaResponse> actualizar(
            @PathVariable @Positive Long id,
            @Valid @RequestBody ActualizarCategoriaRequest request) {

        CategoriaResponse actualizada = categoriaService.actualizarCategoria(id, request);

        return ResponseEntity.ok()
                .header("X-Category-Updated", "true")
                .header("X-Category-ID", actualizada.getId().toString())
                .body(actualizada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable @Positive Long id) {
        categoriaService.eliminarCategoria(id);

        return ResponseEntity.noContent()
                .header("X-Category-Deleted", "true")
                .header("X-Deleted-ID", id.toString())
                .build();
    }

    // ========== CONSULTAS ==========

    @GetMapping("/buscar")
    public ResponseEntity<List<CategoriaResponse>> buscar(
            @RequestParam @Size(min = 2) String nombre) {

        List<CategoriaResponse> categorias = categoriaService.buscarPorNombre(nombre.trim());

        return ResponseEntity.ok()
                .header("X-Search-Term", nombre.trim())
                .header("X-Results-Found", String.valueOf(categorias.size()))
                .body(categorias);
    }

    // ========== ESTADÍSTICAS ==========

    @GetMapping("/stats")
    public ResponseEntity<?> estadisticas() {
        long total = categoriaService.contarCategorias();

        return ResponseEntity.ok()
                .header("X-Total-Categories", String.valueOf(total))
                .header("Cache-Control", "public, max-age=300")
                .body(new StatsResponse(total));
    }

    // Record para respuesta de stats
    private record StatsResponse(long totalCategorias) {}
}
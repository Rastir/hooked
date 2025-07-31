package com.flaco.hooked.domain.controller;

import com.flaco.hooked.domain.service.CategoriaService;
import com.flaco.hooked.domain.request.ActualizarCategoriaRequest;
import com.flaco.hooked.domain.request.CrearCategoriaRequest;
import com.flaco.hooked.domain.response.CategoriaResponse;
import com.flaco.hooked.model.Post;
import com.flaco.hooked.domain.repository.PostRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    @Autowired
    private CategoriaService categoriaService;

    @Autowired
    private PostRepository postRepository;

    //Crear categoría (CREATE)
    @PostMapping
    public ResponseEntity<?> crearCategoria(@Valid @RequestBody CrearCategoriaRequest request) {
        try {
            CategoriaResponse categoria = categoriaService.crearCategoria(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(categoria);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    //Traer categorías (READ)
    @GetMapping
    public ResponseEntity<List<CategoriaResponse>> obtenerTodasLasCategorias() {
        List<CategoriaResponse> categorias = categoriaService.obtenerTodasLasCategorias();
        return ResponseEntity.ok(categorias);
    }

    //Traer categoría por ID (READ)
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerCategoriaPorId(@PathVariable Long id) {
        try {
            CategoriaResponse categoria = categoriaService.obtenerCategoriaPorId(id);
            return ResponseEntity.ok(categoria);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    //Actualizar categoría (UPDATE)
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarCategoria(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarCategoriaRequest request) {
        try {
            CategoriaResponse categoria = categoriaService.actualizarCategoria(id, request);
            return ResponseEntity.ok(categoria);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    //Borrar categoría(DELETE)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarCategoria(@PathVariable Long id) {
        try {
            categoriaService.eliminarCategoria(id);
            return ResponseEntity.ok("Categoría eliminada exitosamente");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    //Traer categorías por ID (READ)
    @GetMapping("/{id}/posts")
    public ResponseEntity<?> obtenerPostsPorCategoria(@PathVariable Long id) {
        // Verificar que la categoría existe usando el service
        if (!categoriaService.existeCategoria(id)) {
            return ResponseEntity.notFound().build();
        }

        List<Post> posts = postRepository.findByCategoriaIdOrderByFechaCreacionDesc(id);
        return ResponseEntity.ok(posts);
    }

    //Buscar categorias por nombre (READ)
    @GetMapping("/buscar")
    public ResponseEntity<List<CategoriaResponse>> buscarCategorias(@RequestParam String nombre) {
        List<CategoriaResponse> categorias = categoriaService.buscarPorNombre(nombre);
        return ResponseEntity.ok(categorias);
    }

    //Datos estadisticos
    @GetMapping("/stats")
    public ResponseEntity<?> obtenerEstadisticas() {
        long totalCategorias = categoriaService.contarCategorias();
        return ResponseEntity.ok("Total de categorías: " + totalCategorias);
    }
}
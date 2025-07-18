package com.flaco.hooked.domain.controller;

import com.flaco.hooked.domain.categoria.Categoria;
import com.flaco.hooked.domain.categoria.CategoriaRepository;
import com.flaco.hooked.domain.request.CrearCategoriaRequest;
import com.flaco.hooked.domain.post.Post;
import com.flaco.hooked.domain.post.PostRepository;
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
    private CategoriaRepository categoriaRepository;

    @Autowired
    private PostRepository postRepository;

    @PostMapping
    public ResponseEntity<?> crearCategoria(@Valid @RequestBody CrearCategoriaRequest request){

        //Verificar si ya existe una categoria con ese nombre
        if (categoriaRepository.existsByNombre(request.getNombre())) {
            return ResponseEntity.badRequest()
                    .body("Ya existe una categoría con ese nombre");
        }

        //Crea la categoría
        Categoria categoria = new Categoria();
        categoria.setNombre(request.getNombre());
        categoria.setDescripcion(request.getDescripcion());

        Categoria categoriaGuardada = categoriaRepository.save(categoria);

        return ResponseEntity.status(HttpStatus.CREATED).body(categoriaGuardada);
    }

    @GetMapping
    public List<Categoria> obtenerTodasLasCategorias(){
        return categoriaRepository.findAll();
    }

    @GetMapping("/{id}/posts")
    public ResponseEntity<?> obtenerPostsPorCategoria(@PathVariable Long id){
        Categoria categoria = categoriaRepository.findById(id)
                .orElse(null);

        if (categoria == null){
            return ResponseEntity.notFound().build();
        }

        List<Post> posts = postRepository.findByCategoriaIdOrderByFechaCreacionDesc(id);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerCategoriaPorId(@PathVariable Long id){
        return categoriaRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }



}

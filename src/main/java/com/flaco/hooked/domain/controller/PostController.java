package com.flaco.hooked.domain.controller;

import com.flaco.hooked.domain.categoria.Categoria;
import com.flaco.hooked.domain.categoria.CategoriaRepository;
import com.flaco.hooked.domain.request.CrearPostRequest;
import com.flaco.hooked.domain.post.Post;
import com.flaco.hooked.domain.post.PostRepository;
import com.flaco.hooked.domain.usuario.Usuario;
import com.flaco.hooked.domain.usuario.UsuarioRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @PostMapping
    public ResponseEntity<?> crearPost(@Valid @RequestBody CrearPostRequest request){

        //verificamos que el usuario exista
        Usuario usuario = usuarioRepository.findById(request.getUsuarioId())
                .orElse(null);
        if (usuario == null){
            return ResponseEntity.badRequest().body("Usuario no encontrado");
        }

        // Verificar que la categoría existe
        Categoria categoria = categoriaRepository.findById(request.getCategoriaId())
                .orElse(null);
        if(categoria == null){
            return ResponseEntity.badRequest().body("Categoria no encontrada");
        }

        // Creacion del post
        Post post = new Post();
        post.setTitulo(request.getTitulo());
        post.setContenido(request.getContenido());
        post.setFotoLink(request.getFotoLink());
        post.setUsuario(usuario);
        post.setCategoria(categoria);

        Post postGuargado = postRepository.save(post);

        return ResponseEntity.status(HttpStatus.CREATED).body(postGuargado);
    }

    @GetMapping
    public List<Post> obtenerTodosLosPosts() {
        return postRepository.findAllByOrderByFechaCreacionDesc();
    }

    @GetMapping("/usuario/{usuarioId}")
    public List<Post> obtenerPostPorUsuario(@PathVariable Long usuarioId){
        return postRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPostPorId(@PathVariable Long id){
        return postRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

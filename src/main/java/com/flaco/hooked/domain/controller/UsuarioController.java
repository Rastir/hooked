package com.flaco.hooked.domain.controller;

import com.flaco.hooked.domain.service.UsuarioService;
import com.flaco.hooked.domain.usuario.CrearUsuarioRequest;
import com.flaco.hooked.domain.usuario.Usuario;
import com.flaco.hooked.domain.usuario.UsuarioRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping
    public ResponseEntity<?> crearUsuario(@Valid @RequestBody CrearUsuarioRequest request,
                                          BindingResult bindingResult) {
        // Verificar errores de validación
        if (bindingResult.hasErrors()) {
            Map<String, String> errores = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    errores.put(error.getField(), error.getDefaultMessage())
            );
            return ResponseEntity.badRequest().body(errores);
        }

        try {
            Usuario usuario = usuarioService.crearUsuario(request);
            return ResponseEntity.ok(usuario);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
     @GetMapping("/{email}")
    public ResponseEntity<Usuario> obtenerUsuarioPorEmail(@PathVariable String email){
        try {
            Usuario usuario = usuarioService.buscarPorEmail(email);
            return ResponseEntity.ok(usuario);
        }catch (RuntimeException e){
            return ResponseEntity.notFound().build();
        }
     }

}

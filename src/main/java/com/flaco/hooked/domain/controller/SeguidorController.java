package com.flaco.hooked.domain.controller;

import com.flaco.hooked.domain.response.SeguidorResponse;
import com.flaco.hooked.domain.service.SeguidorService;
import com.flaco.hooked.model.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class SeguidorController {

    @Autowired
    private SeguidorService seguidorService;

    // Seguir a un usuario
    @PostMapping("/{id}/seguir")
    public ResponseEntity<SeguidorResponse> seguir(
            @PathVariable Long id,
            Authentication auth) {

        Usuario usuarioActual = (Usuario) auth.getPrincipal();
        SeguidorResponse response = seguidorService.seguir(usuarioActual.getId(), id);

        return ResponseEntity.ok()
                .header("X-Follow-Action", "followed")
                .header("X-Is-Fishing-Buddy", String.valueOf(response.isEsFishingBuddy()))
                .body(response);
    }

    // Dejar de seguir
    @DeleteMapping("/{id}/seguir")
    public ResponseEntity<Void> dejarDeSeguir(
            @PathVariable Long id,
            Authentication auth) {

        Usuario usuarioActual = (Usuario) auth.getPrincipal();
        seguidorService.dejarDeSeguir(usuarioActual.getId(), id);

        return ResponseEntity.noContent()
                .header("X-Follow-Action", "unfollowed")
                .build();
    }

    // Lista de seguidores de un usuario
    @GetMapping("/{id}/seguidores")
    public ResponseEntity<List<SeguidorResponse>> obtenerSeguidores(
            @PathVariable Long id,
            Authentication auth) {

        Long usuarioActualId = auth != null
                ? ((Usuario) auth.getPrincipal()).getId()
                : null;

        List<SeguidorResponse> seguidores = seguidorService.obtenerSeguidores(id, usuarioActualId);

        return ResponseEntity.ok()
                .header("X-Total-Seguidores", String.valueOf(seguidores.size()))
                .body(seguidores);
    }

    // Lista de siguiendo de un usuario
    @GetMapping("/{id}/siguiendo")
    public ResponseEntity<List<SeguidorResponse>> obtenerSiguiendo(
            @PathVariable Long id,
            Authentication auth) {

        Long usuarioActualId = auth != null
                ? ((Usuario) auth.getPrincipal()).getId()
                : null;

        List<SeguidorResponse> siguiendo = seguidorService.obtenerSiguiendo(id, usuarioActualId);

        return ResponseEntity.ok()
                .header("X-Total-Siguiendo", String.valueOf(siguiendo.size()))
                .body(siguiendo);
    }

    // ¿Sigues a este usuario? (para el botón en el perfil)
    @GetMapping("/{id}/es-seguidor")
    public ResponseEntity<Boolean> esSeguidor(
            @PathVariable Long id,
            Authentication auth) {

        Usuario usuarioActual = (Usuario) auth.getPrincipal();
        boolean sigue = seguidorService.sigueA(usuarioActual.getId(), id);

        return ResponseEntity.ok(sigue);
    }
}
package com.flaco.hooked.domain.controller;

import com.flaco.hooked.domain.request.CrearUsuarioRequest;
import com.flaco.hooked.domain.request.LoginRequest;
import com.flaco.hooked.domain.response.LoginResponse;
import com.flaco.hooked.domain.service.JwtService;
import com.flaco.hooked.domain.service.UsuarioService;
import com.flaco.hooked.domain.usuario.Usuario;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest){

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        //Autenticar
        Usuario usuario = (Usuario) authentication.getPrincipal();

        //Crear token
        String token = jwtService.generarToken(usuario);

        //Crear respons
        LoginResponse response = new LoginResponse(
                token,
                usuario.getId(),
                usuario.getEmail(),
                usuario.getNombre()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/registro")
    public ResponseEntity<LoginResponse> registro (@Valid @RequestBody CrearUsuarioRequest request){

        //Se crea al nuevo usuario
        Usuario nuevoUsuario = usuarioService.crearUsuario(request);

        // Crear el token para el nuevo usuario
        String token = jwtService.generarToken(nuevoUsuario);

        // La respuesta
        LoginResponse response = new LoginResponse(
                token,
                nuevoUsuario.getId(),
                nuevoUsuario.getEmail(),
                nuevoUsuario.getNombre()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

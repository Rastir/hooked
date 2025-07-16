package com.flaco.hooked.domain.service;

import com.flaco.hooked.domain.usuario.CrearUsuarioRequest;
import com.flaco.hooked.domain.usuario.Usuario;
import com.flaco.hooked.domain.usuario.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Usuario crearUsuario(CrearUsuarioRequest request){

        if (usuarioRepository.existsByEmail(request.getEmail())){
            throw new RuntimeException("El email ya está registrado");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre());
        usuario.setEmail(request.getEmail());
        //Encriptación de la contraseña
        usuario.setContrasena(passwordEncoder.encode(request.getContrasena()));

        return usuarioRepository.save(usuario);
    }

    public Usuario buscarPorEmail(String email){
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }
}

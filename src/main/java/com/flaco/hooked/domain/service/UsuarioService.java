package com.flaco.hooked.domain.service;

import com.flaco.hooked.domain.request.CrearUsuarioRequest;
import com.flaco.hooked.domain.usuario.Usuario;
import com.flaco.hooked.domain.usuario.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // NUEVO MÉTODO - Listar todos los usuarios
    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    // Método existente para crear usuario
    public Usuario crearUsuario(CrearUsuarioRequest request) {
        // Verificar si el email ya existe
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }

        // Crear nuevo usuario
        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre());
        usuario.setEmail(request.getEmail());
        usuario.setContrasena(passwordEncoder.encode(request.getContrasena()));

        return usuarioRepository.save(usuario);
    }

    // Método existente para buscar por email
    public Usuario buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    // Si quieres, puedes agregar más métodos útiles:
    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    public long contarUsuarios() {
        return usuarioRepository.count();
    }

    public void eliminarUsuario(Long id) {
        usuarioRepository.deleteById(id);
    }
}
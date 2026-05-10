package com.flaco.hooked.domain.service;

import com.flaco.hooked.domain.repository.SeguidorRepository;
import com.flaco.hooked.domain.repository.UsuarioRepository;
import com.flaco.hooked.domain.response.SeguidorResponse;
import com.flaco.hooked.model.Seguidor;
import com.flaco.hooked.model.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SeguidorService {

    @Autowired
    private SeguidorRepository seguidorRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Seguir a un usuario
    public SeguidorResponse seguir(Long seguidorId, Long seguidoId) {

        // No puedes seguirte a ti mismo (fulpruf, por si las dudas)
        if (seguidorId.equals(seguidoId)) {
            throw new RuntimeException("No puedes seguirte a ti mismo");
        }

        // ¿Ya lo sigues?
        if (seguidorRepository.existsBySeguidorIdAndSeguidoId(seguidorId, seguidoId)) {
            throw new RuntimeException("Ya sigues a este usuario");
        }

        Usuario seguidor = usuarioRepository.findById(seguidorId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Usuario seguido = usuarioRepository.findById(seguidoId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Seguidor nuevoFollow = new Seguidor(seguidor, seguido);
        seguidorRepository.save(nuevoFollow);

        // ¿Se convirtieron en Fishing Buddies?
        boolean esBuddy = seguidorRepository.esFishingBuddy(seguidorId, seguidoId);

        return new SeguidorResponse(seguido, esBuddy, true);
    }

    // Dejar de seguir
    public void dejarDeSeguir(Long seguidorId, Long seguidoId) {
        if (!seguidorRepository.existsBySeguidorIdAndSeguidoId(seguidorId, seguidoId)) {
            throw new RuntimeException("No sigues a este usuario");
        }
        seguidorRepository.deleteBySeguidorIdAndSeguidoId(seguidorId, seguidoId);
    }

    // ¿Sigues a este usuario?
    @Transactional(readOnly = true)
    public boolean sigueA(Long seguidorId, Long seguidoId) {
        return seguidorRepository.existsBySeguidorIdAndSeguidoId(seguidorId, seguidoId);
    }

    // Lista de seguidores (quién me sigue)
    @Transactional(readOnly = true)
    public List<SeguidorResponse> obtenerSeguidores(Long usuarioId, Long usuarioActualId) {
        return seguidorRepository.findBySeguidoIdOrderByFechaSeguimientoDesc(usuarioId)
                .stream()
                .map(s -> {
                    boolean esBuddy = seguidorRepository.esFishingBuddy(
                            s.getSeguidor().getId(), usuarioId
                    );
                    boolean yoLoSigo = usuarioActualId != null &&
                            seguidorRepository.existsBySeguidorIdAndSeguidoId(
                                    usuarioActualId, s.getSeguidor().getId()
                            );
                    return new SeguidorResponse(s.getSeguidor(), esBuddy, yoLoSigo);
                })
                .collect(Collectors.toList());
    }

    // Lista de siguiendo (a quién sigo)
    @Transactional(readOnly = true)
    public List<SeguidorResponse> obtenerSiguiendo(Long usuarioId, Long usuarioActualId) {
        return seguidorRepository.findBySeguidorIdOrderByFechaSeguimientoDesc(usuarioId)
                .stream()
                .map(s -> {
                    boolean esBuddy = seguidorRepository.esFishingBuddy(
                            usuarioId, s.getSeguido().getId()
                    );
                    boolean yoLoSigo = usuarioActualId != null &&
                            seguidorRepository.existsBySeguidorIdAndSeguidoId(
                                    usuarioActualId, s.getSeguido().getId()
                            );
                    return new SeguidorResponse(s.getSeguido(), esBuddy, yoLoSigo);
                })
                .collect(Collectors.toList());
    }

    // Contadores para el perfil
    @Transactional(readOnly = true)
    public long contarSeguidores(Long usuarioId) {
        return seguidorRepository.countBySeguidoId(usuarioId);
    }

    @Transactional(readOnly = true)
    public long contarSiguiendo(Long usuarioId) {
        return seguidorRepository.countBySeguidorId(usuarioId);
    }
}
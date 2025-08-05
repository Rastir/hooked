package com.flaco.hooked.domain.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface ImageStorageService {

    // Sube imagen y retorna URL pública
    String subirImagen(MultipartFile archivo, String carpeta) throws IOException;

    // Elimina imagen por URL o identificador
    void eliminarImagen(String identificador) throws IOException;

    // Valida si el servicio está disponible
    boolean estaDisponible();
}
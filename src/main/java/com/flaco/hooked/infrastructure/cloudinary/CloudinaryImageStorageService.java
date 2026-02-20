package com.flaco.hooked.infrastructure.cloudinary;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.flaco.hooked.domain.service.ImageStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryImageStorageService implements ImageStorageService {

    private final Cloudinary cloudinary;

    public CloudinaryImageStorageService(
            @Value("${cloudinary.cloud_name:}") String cloudName,
            @Value("${cloudinary.api_key:}") String apiKey,
            @Value("${cloudinary.api_secret:}") String apiSecret) {

        if (cloudName.isEmpty() || apiKey.isEmpty() || apiSecret.isEmpty()) {
            throw new IllegalStateException("Cloudinary configuration missing. Check application.properties");
        }

        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true
        ));
    }

    @Override
    public String subirImagen(MultipartFile archivo, String carpeta) throws IOException {
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("El archivo no puede estar vacío");
        }

        Map<?, ?> uploadResult = cloudinary.uploader().upload(
                archivo.getBytes(),
                ObjectUtils.asMap(
                        "folder", carpeta != null ? carpeta : "hooked",
                        "resource_type", "auto"
                )
        );

        return uploadResult.get("secure_url").toString();
    }

    @Override
    public void eliminarImagen(String url) throws IOException {
        if (url == null || url.isEmpty()) return;

        // Extraer public_id de la URL de Cloudinary
        String publicId = extraerPublicId(url);
        if (publicId != null) {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        }
    }

    @Override
    public boolean estaDisponible() {
        return cloudinary != null;
    }

    private String extraerPublicId(String url) {
        // URL típica: https://res.cloudinary.com/cloud_name/image/upload/v1234567890/folder/public_id.jpg
        try {
            int uploadIndex = url.indexOf("/upload/");
            if (uploadIndex == -1) return null;

            String afterUpload = url.substring(uploadIndex + 8); // +8 para saltar "/upload/"

            // Saltar versión si existe (v1234567890/)
            int slashIndex = afterUpload.indexOf("/");
            if (slashIndex != -1) {
                String path = afterUpload.substring(slashIndex + 1);
                // Quitar extensión
                int dotIndex = path.lastIndexOf(".");
                return dotIndex != -1 ? path.substring(0, dotIndex) : path;
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
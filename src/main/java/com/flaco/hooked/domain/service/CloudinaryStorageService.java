package com.flaco.hooked.domain.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@Profile("cloudinary")
public class CloudinaryStorageService implements ImageStorageService {

    private final Cloudinary cloudinary;

    public CloudinaryStorageService(
            @Value("${cloudinary.cloud_name}") String cloudName,
            @Value("${cloudinary.api_key}") String apiKey,
            @Value("${cloudinary.api_secret}") String apiSecret) {

        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret
        ));
    }

    @Override
    public String subirImagen(MultipartFile archivo, String carpeta) throws IOException {

        Map uploadResult = cloudinary.uploader().upload(archivo.getBytes(),
                ObjectUtils.asMap(
                        "folder", "hooked/" + carpeta,
                        "transformation", "c_fill,w_400,h_400,q_auto",
                        "format", "jpg"
                )
        );

        return (String) uploadResult.get("secure_url");
    }

    @Override
    public void eliminarImagen(String url) throws IOException {
        // Extraer public_id de la URL para eliminar
        String publicId = extraerPublicId(url);
        if (publicId != null) {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        }
    }

    @Override
    public boolean estaDisponible() {
        try {
            cloudinary.api().ping(ObjectUtils.emptyMap());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String extraerPublicId(String url) {
        // Extraer public_id de URL de Cloudinary
        if (url != null && url.contains("cloudinary.com")) {
            String[] partes = url.split("/");
            for (int i = 0; i < partes.length; i++) {
                if (partes[i].equals("upload") && i + 2 < partes.length) {
                    String publicId = partes[i + 2];
                    return publicId.substring(0, publicId.lastIndexOf('.'));
                }
            }
        }
        return null;
    }
}
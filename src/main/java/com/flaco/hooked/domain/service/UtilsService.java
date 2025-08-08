package com.flaco.hooked.domain.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

@Service
public class UtilsService {

    // Obtener información del dispositivo desde el User-Agent
    public String obtenerInfoDispositivo(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null || userAgent.isEmpty()) {
            return "Dispositivo desconocido";
        }

        // Extraer información relevante del User-Agent
        StringBuilder dispositivoInfo = new StringBuilder();

        // Detectar navegador
        if (userAgent.contains("Chrome")) {
            dispositivoInfo.append("Chrome");
        } else if (userAgent.contains("Firefox")) {
            dispositivoInfo.append("Firefox");
        } else if (userAgent.contains("Safari")) {
            dispositivoInfo.append("Safari");
        } else if (userAgent.contains("Edge")) {
            dispositivoInfo.append("Edge");
        } else {
            dispositivoInfo.append("Navegador desconocido");
        }

        // Detectar sistema operativo
        if (userAgent.contains("Windows")) {
            dispositivoInfo.append(" - Windows");
        } else if (userAgent.contains("Mac")) {
            dispositivoInfo.append(" - macOS");
        } else if (userAgent.contains("Linux")) {
            dispositivoInfo.append(" - Linux");
        } else if (userAgent.contains("Android")) {
            dispositivoInfo.append(" - Android");
        } else if (userAgent.contains("iPhone") || userAgent.contains("iPad")) {
            dispositivoInfo.append(" - iOS");
        } else {
            dispositivoInfo.append(" - SO desconocido");
        }

        return dispositivoInfo.toString();
    }

    // Obtener la dirección IP real del cliente
    public String obtenerIPAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");

        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_X_FORWARDED");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_X_CLUSTER_CLIENT_IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_FORWARDED_FOR");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_FORWARDED");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_VIA");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }

        // Si hay múltiples IPs, tomar la primera
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }

        return ipAddress != null ? ipAddress : "IP desconocida";
    }
}

package com.example.AUHT_SERVICE.UTILS;


import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.Date;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private String jwtExpirationString;

    private Key signingKey;

    @PostConstruct
    public void init() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        // Padding si la clave es muy corta
        if (keyBytes.length < 64) {
            byte[] padded = new byte[64];
            System.arraycopy(keyBytes, 0, padded, 0, keyBytes.length);
            keyBytes = padded;
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }
    private long getJwtExpiration() {
        try {
            return Long.parseLong(jwtExpirationString);
        } catch (Exception e) {
            return 86400000L; // 24 horas por defecto
        }
    }

    private Key getKey() {
        return signingKey; // ← siempre la misma
    }
    // Agrega este método para refresh token
    public String generateRefreshToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .claim("type", "refresh")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 7)) // 7 días
                .signWith(getKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    // Verifica que sea refresh token y no access token
    public boolean isRefreshToken(String token) {
        try {
            String type = getClaims(token).get("type", String.class);
            return "refresh".equals(type);
        } catch (Exception e) {
            return false;
        }
    }

    //  GENERAR TOKEN CON ROL
    public String generateToken(String email, String rol) {
        return Jwts.builder()
                .setSubject(email)
                .claim("rol", rol) //  AQUÍ VA EL ROL
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + getJwtExpiration()))
                .signWith(getKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    //  EXTRAER EMAIL
    public String getEmailFromToken(String token) {
        return getClaims(token).getSubject();
    }

    //  EXTRAER ROL
    public String getRolFromToken(String token) {
        return getClaims(token).get("rol", String.class);
    }

    //  VALIDAR TOKEN
    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // OBTENER TIEMPO DE EXPIRACIÓN EN SEGUNDOS (para Redis blacklist)
    public long getExpirationSeconds(String token) {
        try {
            Claims claims = getClaims(token);
            Date expirationDate = claims.getExpiration();
            long expirationTimeMs = expirationDate.getTime();
            long currentTimeMs = System.currentTimeMillis();
            long remainingMs = expirationTimeMs - currentTimeMs;
            return Math.max(0, remainingMs / 1000); // Convertir a segundos
        } catch (Exception e) {
            return 0;
        }
    }

    //MÉTODOCENTRAL (PRO)
    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
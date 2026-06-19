package com.group.artifName.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    // Inyectamos la clave secreta desde el application.properties
    @Value("${jwt.secret}")
    private String secretKey;

    // 100 años en milisegundos. La 'L' fuerza a Java a usar un formato de número gigante (Long)
    private final long JWT_EXPIRATION = 100L * 365 * 24 * 60 * 60 * 1000;

    // Obtener la llave de firmado segura
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    // 1. GENERAR EL TOKEN
    public String generateToken(String email, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role); // Guardamos el rol dentro del token

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(email) // El "Subject" usualmente es el dato identificador (email)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // Firmamos con el algoritmo HS256
                .compact();
    }

    // 2. EXTRAER EL EMAIL DEL TOKEN
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // 3. VALIDAR SI EL TOKEN NO HA EXPIRADO
    public boolean isTokenValid(String token) {
        try {
            Date expiration = extractClaim(token, Claims::getExpiration);
            return expiration.after(new Date());
        } catch (Exception e) {
            return false; // Si el token fue manipulado o expiró, dará error y será inválido
        }
    }

    // Método utilitario genérico para extraer datos (Claims) del token
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claimsResolver.apply(claims);
    }
}
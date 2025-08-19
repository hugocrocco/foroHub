package com.foro.forohub.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final SecretKey key;
    private final SignatureAlgorithm alg;
    private final Duration defaultTtl;

    public JwtService(
            @Value("${jwt.secret}") String secretOrBase64,
            @Value("${jwt.alg:HS256}") String algName,
            @Value("${jwt.ttl:PT24H}") String ttl // ISO-8601, ej: PT24H = 24 horas
    ) {
        this.key = buildKey(secretOrBase64);
        this.alg = SignatureAlgorithm.forName(algName);
        this.defaultTtl = Duration.parse(ttl);
    }

    // ================= API usada por tus clases =================

    /** Genera un JWT para el usuario con el TTL por defecto. */
    public String generateToken(UserDetails user) {
        return buildToken(Collections.emptyMap(), user, defaultTtl);
    }

    /** Extrae el username (subject) del token. */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /** Valida que el token pertenezca al usuario y no esté expirado. */
    public boolean isValid(String token, UserDetails user) {
        String username = extractUsername(token);
        return username.equals(user.getUsername()) && !isExpired(token);
    }

    // ================= Utilidades =================

    public String buildToken(Map<String, Object> extraClaims, UserDetails user, Duration ttl) {
        Date now = new Date();
        Date exp = Date.from(now.toInstant().plus(ttl));

        // Compatible con JJWT 0.11.x (API con métodos setX)
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(user.getUsername())
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key, alg)
                .compact();
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return resolver.apply(claims);
    }

    private boolean isExpired(String token) {
        Date exp = extractClaim(token, Claims::getExpiration);
        return exp.before(new Date());
    }

    public SecretKey getKey() { return key; }
    public SignatureAlgorithm getAlg() { return alg; }

    // ================= Helpers =================

    /**
     * Acepta secreto en Base64 o en texto plano.
     * Si es Base64 válido, lo decodifica. Si no, usa los bytes del texto.
     * Debe tener al menos 32 bytes (256 bits) para HS256, de lo contrario se lanza IllegalArgumentException.
     */
    private static SecretKey buildKey(String secretOrBase64) {
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secretOrBase64);
            if (keyBytes.length == 0) {
                // Cuando la cadena no es Base64, algunos decoders devuelven [].
                keyBytes = secretOrBase64.getBytes(StandardCharsets.UTF_8);
            }
        } catch (IllegalArgumentException ex) {
            // No es Base64: usar como texto plano
            keyBytes = secretOrBase64.getBytes(StandardCharsets.UTF_8);
        }

        if (keyBytes.length < 32) {
            throw new IllegalArgumentException(
                    "jwt.secret es demasiado corto. Se requieren al menos 32 bytes (256 bits). " +
                    "Genera uno con: openssl rand -base64 32 y colócalo en application.properties"
            );
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
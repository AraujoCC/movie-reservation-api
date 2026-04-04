package com.isaac.moviereservation.config;
 
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
 
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;
 
@Service
public class JwtService {
 
    @Value("${jwt.secret}")
    private String secret;
 
    @Value("${jwt.expiration}")
    private long expirationMs;
 
    // ── Geração ───────────────────────────────────────────────────────────────
 
    public String generateToken(UserDetails userDetails) {
        return generateToken(Map.of(), userDetails);
    }
 
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey())
                .compact();
    }
 
    // ── Validação ─────────────────────────────────────────────────────────────
 
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }
 
    // ── Extração de claims ────────────────────────────────────────────────────
 
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
 
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(extractAllClaims(token));
    }
 
    // ── Helpers privados ──────────────────────────────────────────────────────
 
    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }
 
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
 
    private SecretKey getSigningKey() {
        // O secret no application.properties deve ter pelo menos 32 chars.
        // Em prod, use uma chave Base64 de 256 bits gerada com:
        //   openssl rand -base64 32
        byte[] keyBytes = Decoders.BASE64.decode(
                java.util.Base64.getEncoder().encodeToString(secret.getBytes())
        );
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
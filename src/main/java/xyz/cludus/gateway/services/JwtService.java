package xyz.cludus.gateway.services;

import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.OffsetDateTime;
import java.util.Date;

@Service
public class JwtService {
    private String secretKey;

    private String issuer;

    private final Key signInKey;

    private final JwtParser parser;

    public JwtService(
            @Value("${jwt.secret-key}") String secretKey,
            @Value("${jwt.issuer}") String issuer) {
        this.secretKey = secretKey.trim();
        this.issuer = issuer;
        var keyBytes = Decoders.BASE64.decode(this.secretKey);
        signInKey = Keys.hmacShaKeyFor(keyBytes);
        parser = Jwts.parser()
                .setSigningKey(signInKey)
                .requireIssuer(issuer);
    }

    public String createToken(String username) {
        var now = OffsetDateTime.now();
        var expiresAt = now.plusDays(20);
        return Jwts.builder()
                .setIssuer(issuer)
                .setIssuedAt(Date.from(now.toInstant()))
                .setSubject(username)
                .setExpiration(Date.from(expiresAt.toInstant()))
                .signWith(signInKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String parseToken(String token) {
        if (token != null) {
            var jwt = parser
                    .parseClaimsJws(token);
            if (jwt.getBody().getExpiration().after(new Date())) {
                return jwt.getBody().getSubject();
            }
        }
        return null;
    }
}

package xyz.cludus.gateway.services;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class JwtService {
    private String secretKey;

    private String issuer;

    private final Key signInKey;

    public JwtService(
            @Value("${jwt.secret-key}") String secretKey,
            @Value("${jwt.issuer}") String issuer) {
        this.secretKey = secretKey.trim();
        this.issuer = issuer;
        var keyBytes = Decoders.BASE64.decode(this.secretKey);
        signInKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String parseToken(String token) {
        if (token != null) {
            var jwt = Jwts.parserBuilder()
                    .setSigningKey(signInKey)
                    .requireIssuer(issuer)
                    .build()
                    .parseClaimsJws(token);
            if (jwt.getBody().getExpiration().after(new Date())) {
                return jwt.getBody().getSubject();
            }
        }
        return null;
    }
}

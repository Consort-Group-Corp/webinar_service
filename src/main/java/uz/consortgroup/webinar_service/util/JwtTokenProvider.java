package uz.consortgroup.webinar_service.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.UUID;

@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${security.token}")
    private String jwtSecret;

    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (JwtException e) {
            log.error("JWT validation error: {}", e.getMessage());
            return false;
        }
    }

    public UUID getUserIdFromToken(String token) {
        return UUID.fromString(getClaims(token).get("userId", String.class));
    }

    public String getUserRoleFromToken(String token) {
        return getClaims(token).get("userType", String.class);
    }
}


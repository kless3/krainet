package com.example.user_service.config;

import com.example.user_service.service.UserDetailsImpl;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtCore {

    @Value("${project.app.secret}")
    private String secret;

    @Value("${project.app.lifetime}")
    private int lifetime;

    public String generateToken(Authentication authentication) {
        if (authentication.getPrincipal() instanceof UserDetailsImpl userDetails) {
            return buildToken(userDetails.getUsername());
        }
        throw new IllegalArgumentException("Unsupported authentication type");
    }

    private String buildToken(String subject) {
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date().getTime() + lifetime)))
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    public String getNameFromJwt(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = getNameFromJwt(token);
        return username.equals(userDetails.getUsername());
    }
}
package com.elephant.safetybackend.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    public String extractUsername(String token) {
        String username = extractClaim(token, Claims::getSubject);
        System.out.println("=== JWT EXTRACT USERNAME ===");
        System.out.println("Extracted username: '" + username + "'");
        return username;
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        System.out.println("=== JWT EXTRACT ALL CLAIMS ===");
        System.out.println("Token preview: " + (token.length() > 50 ? token.substring(0, 50) + "..." : token));
        System.out.println("Secret used: " + (secret != null ? secret.substring(0, Math.min(20, secret.length())) + "..." : "NULL"));

        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();
            System.out.println("Claims extracted successfully");
            System.out.println("Subject (email): '" + claims.getSubject() + "'");
            System.out.println("Issued at: " + claims.getIssuedAt());
            System.out.println("Expiration: " + claims.getExpiration());
            return claims;
        } catch (Exception e) {
            System.out.println("Error extracting claims: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public String generateToken(String username) {
        System.out.println("=== JWT GENERATE TOKEN ===");
        System.out.println("Generating token for username: '" + username + "'");
        Map<String, Object> claims = new HashMap<>();
        String token = createToken(claims, username);
        System.out.println("Token generated successfully");
        System.out.println("Token preview: " + (token.length() > 50 ? token.substring(0, 50) + "..." : token));
        return token;
    }

    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        System.out.println("Token created at: " + now);
        System.out.println("Token expires at: " + expiryDate);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        boolean isValid = username.equals(userDetails.getUsername()) && !isTokenExpired(token);

        System.out.println("=== JWT VALIDATE TOKEN ===");
        System.out.println("Token username: '" + username + "'");
        System.out.println("UserDetails username: '" + userDetails.getUsername() + "'");
        System.out.println("Is token valid: " + isValid);

        return isValid;
    }

    private Boolean isTokenExpired(String token) {
        final Date expiration = extractExpiration(token);
        boolean isExpired = expiration.before(new Date());
        System.out.println("Token expiration date: " + expiration);
        System.out.println("Current date: " + new Date());
        System.out.println("Is expired: " + isExpired);
        return isExpired;
    }
}
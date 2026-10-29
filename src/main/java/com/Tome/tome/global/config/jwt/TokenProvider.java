package com.Tome.tome.global.config.jwt;

import com.Tome.tome.domain.user.entity.User;
import com.Tome.tome.domain.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;

@RequiredArgsConstructor
@Service
public class TokenProvider {

    public static final String TYPE_ACCESS = "access";
    public static final String TYPE_REFRESH = "refresh";
    private static final String TYPE_CLAIM = "type";

    private final JwtProperties jwtProperties;
    private final UserRepository userRepository;

    public String generateToken(User user, Duration expriedAt, String type){
        Date now = new Date();
        return makeToken(new Date(now.getTime() + expriedAt.toMillis()), user, type);
    }

    private String makeToken(Date expriy, User user, String type){
        Date now = new Date();

        return Jwts.builder()
                .header().type("JWT").and()
                .issuer(jwtProperties.getIssuer())
                .issuedAt(now)
                .expiration(expriy)
                .subject(user.getEmail())
                .claim("id", user.getId())
                .claim(TYPE_CLAIM, type)
                .signWith(signingKey())
                .compact();
    }

    public boolean validToken(String token, String type){
        try{
            return type.equals(getClaims(token).get(TYPE_CLAIM, String.class));
        } catch (Exception e){
            return false;
        }
    }

    public Authentication getAuthentication(String token){
        Long userId = getUserId(token);
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("not found " + userId));

        return new UsernamePasswordAuthenticationToken(user, token, user.getAuthorities());
    }

    public Long getUserId(String token){
        Claims claims = getClaims(token);

        return claims.get("id", Long.class);
    }

    private Claims getClaims(String token){
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey signingKey(){
        return Keys.hmacShaKeyFor(jwtProperties.getSecretKey().getBytes(StandardCharsets.UTF_8));
    }
}

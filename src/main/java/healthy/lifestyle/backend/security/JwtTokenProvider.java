package healthy.lifestyle.backend.security;

import static java.util.Objects.isNull;

import healthy.lifestyle.backend.exception.ApiException;
import healthy.lifestyle.backend.exception.ErrorMessage;
import healthy.lifestyle.backend.users.service.AuthService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import java.util.Date;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {
    private final SecurityProps securityProps;

    private final AuthService authService;

    public JwtTokenProvider(SecurityProps securityProps, AuthService authService) {
        this.securityProps = securityProps;
        this.authService = authService;
    }

    public String generateToken(Authentication authentication) {
        String username = authentication.getName();
        Date currentDate = new Date();
        Date expirationDate =
                new Date(currentDate.getTime() + securityProps.Jwt().expirationMilliseconds());

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(currentDate)
                .setExpiration(expirationDate)
                .signWith(Keys.hmacShaKeyFor(securityProps.Jwt().secret().getBytes()), SignatureAlgorithm.HS512)
                .compact();
    }

    public String getUsernameFromJwt(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(securityProps.Jwt().secret().getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder()
                    .setSigningKey(
                            Keys.hmacShaKeyFor(securityProps.Jwt().secret().getBytes()))
                    .build()
                    .parseClaimsJws(token);

            String signature = claims.getSignature();
            if (isNull(signature)) {
                throw new ApiException(ErrorMessage.INVALID_TOKEN, HttpStatus.UNAUTHORIZED);
            }

            Date issuedAt = claims.getBody().getIssuedAt();
            Date expiredAt = claims.getBody().getExpiration();
            if ((expiredAt.getTime() - issuedAt.getTime())
                    != securityProps.Jwt().expirationMilliseconds()) {
                throw new ApiException(ErrorMessage.INVALID_TOKEN, HttpStatus.UNAUTHORIZED);
            }

            String usernameOrEmail = claims.getBody().getSubject();
            if (authService
                    .findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                    .isEmpty()) {
                throw new ApiException(ErrorMessage.INVALID_TOKEN, HttpStatus.UNAUTHORIZED);
            }

        } catch (ExpiredJwtException
                | UnsupportedJwtException
                | MalformedJwtException
                | SignatureException
                | IllegalArgumentException ex) {
            throw new ApiException(ErrorMessage.INVALID_TOKEN, HttpStatus.UNAUTHORIZED);
        }

        return true;
    }
}

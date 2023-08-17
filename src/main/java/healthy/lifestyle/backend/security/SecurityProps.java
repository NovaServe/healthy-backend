package healthy.lifestyle.backend.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security")
public record SecurityProps(Jwt Jwt) {
    record Jwt(long expirationMilliseconds, String secret) {}
}

package healthy.lifestyle.backend.shared.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security")
public record SecurityProps(Jwt Jwt) {
    public record Jwt(long expirationMilliseconds, String secret) {}
}

package com.example.BlogApp.Jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Binds properties under the 'jwt' prefix (jwt.secret, jwt.expirationMs, jwt.tokenPrefix).
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String secret;
    private long expirationMs;
    private String tokenPrefix = "Bearer";

}

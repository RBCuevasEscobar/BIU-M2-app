package com.ecommerce.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtConfig {

    // Secret Key length minimum for HS256 is 256 bits (32 bytes)
    @Value("${app.jwt.secret:EcommerceSecretKeySuperSeguraConLongitudApropiadaParaJwt}")
    private String secretKey;
    
    // Default Expiration in milliseconds (24 Hours default)
    @Value("${app.jwt.expirationMs:86400000}")
    private long expirationTime;

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public long getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(long expirationTime) {
        this.expirationTime = expirationTime;
    }
}

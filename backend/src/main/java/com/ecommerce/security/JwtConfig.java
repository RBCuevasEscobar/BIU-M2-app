package com.ecommerce.security;

import org.springframework.stereotype.Component;

@Component
public class JwtConfig {

    // Secret Key length minimum for HS256 is 256 bits (32 bytes)
    private String secretKey = "EcommerceSecretKeySuperSeguraConLongitudApropiadaParaJwt";
    
    // Default Expiration in milliseconds (24 Hours default)
    private long expirationTime = 86400000;

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

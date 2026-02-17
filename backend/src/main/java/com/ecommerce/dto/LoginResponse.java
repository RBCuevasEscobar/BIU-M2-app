package com.ecommerce.dto;

import com.ecommerce.model.Role;

public record LoginResponse(Long id, String nombre, Role role, String token) {
}

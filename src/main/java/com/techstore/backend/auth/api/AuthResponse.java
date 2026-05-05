package com.techstore.backend.auth.api;

import com.techstore.backend.user.api.UserResponse;

public record AuthResponse(String token, String tokenType, UserResponse user) {
}

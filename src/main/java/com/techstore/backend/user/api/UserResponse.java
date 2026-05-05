package com.techstore.backend.user.api;

import com.techstore.backend.user.domain.AppUser;
import com.techstore.backend.user.domain.Role;

public record UserResponse(Long id, String name, String email, Role role) {
	public static UserResponse from(AppUser user) {
		return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole());
	}
}

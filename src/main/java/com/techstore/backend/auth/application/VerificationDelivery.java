package com.techstore.backend.auth.application;

import com.techstore.backend.user.domain.AppUser;

public record VerificationDelivery(AppUser user, boolean emailSent) {
}

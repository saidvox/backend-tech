package com.techstore.backend.user.infrastructure;

import java.util.Optional;

import com.techstore.backend.user.domain.AppUser;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<AppUser, Long> {
	Optional<AppUser> findByEmail(String email);

	boolean existsByEmail(String email);
}

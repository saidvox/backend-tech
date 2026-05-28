package com.techstore.backend.user.infrastructure;

import java.util.Optional;
import java.util.List;

import com.techstore.backend.user.domain.AppUser;
import com.techstore.backend.user.domain.Role;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<AppUser, Long> {
	Optional<AppUser> findByEmail(String email);

	boolean existsByEmail(String email);

	List<AppUser> findByEmailVerifiedTrueAndRole(Role role);
}

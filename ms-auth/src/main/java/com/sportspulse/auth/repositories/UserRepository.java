package com.sportspulse.auth.repositories;

import com.sportspulse.auth.models.UserEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Entity query interface. */
public interface UserRepository extends JpaRepository<UserEntity, UUID> {

  boolean existsByEmail(String email);

  boolean existsByUsername(String username);
}

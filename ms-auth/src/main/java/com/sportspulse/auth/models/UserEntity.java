package com.sportspulse.auth.models;

import com.sportspulse.auth.utils.enums.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity that represents a user in the system.
 *
 * <p>Stores authentication and authorization data such as username, email, password, and role. It
 * also keeps track of creation and update timestamps.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(
    name = "users",
    uniqueConstraints = {
      @UniqueConstraint(name = "uk_username", columnNames = "username"),
      @UniqueConstraint(name = "uk_email", columnNames = "email")
    })
public class UserEntity {

  @Id @GeneratedValue private UUID id;

  @Column(nullable = false)
  private String username;

  @Column(nullable = false)
  private String email;

  @Column(nullable = false)
  private String password;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private UserRole role;

  @Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
  private Instant createdDate;

  @Column(nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
  private Instant updatedDate;

  @PrePersist
  protected void onCreate() {
    Instant now = Instant.now();
    this.createdDate = now;
    this.updatedDate = now;
  }

  @PreUpdate
  protected void onUpdate() {
    this.updatedDate = Instant.now();
  }
}

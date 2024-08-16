package com.felipe.trip_planner_user_service.repositories;

import com.felipe.trip_planner_user_service.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
  Optional<User> findByEmail(String email);
}

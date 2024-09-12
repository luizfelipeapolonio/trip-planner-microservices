package com.felipe.trip_planner_trip_service.repositories;

import com.felipe.trip_planner_trip_service.models.Invite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface InviteRepository extends JpaRepository<Invite, UUID> {

  @Query("SELECT i FROM Invite i WHERE i.code=:code AND i.isValid=true")
  Optional<Invite> findByCodeAndIsValidTrue(@Param("code") UUID code);

  @Query("SELECT i FROM Invite i WHERE i.userEmail=:userEmail AND i.trip.id=:tripId AND i.isValid=true")
  Optional<Invite> findByUserEmailAndTripIdAndIsValidTrue(@Param("userEmail") String userEmail, @Param("tripId") UUID tripId);
}

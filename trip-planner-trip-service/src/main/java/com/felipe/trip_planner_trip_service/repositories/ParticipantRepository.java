package com.felipe.trip_planner_trip_service.repositories;

import com.felipe.trip_planner_trip_service.models.Participant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ParticipantRepository extends JpaRepository<Participant, UUID> {

  @Query("SELECT p FROM Participant p WHERE p.email=:email AND p.trip.id=:tripId")
  Optional<Participant> findByEmailAndTripId(@Param("email") String email, @Param("tripId") UUID tripId);
}

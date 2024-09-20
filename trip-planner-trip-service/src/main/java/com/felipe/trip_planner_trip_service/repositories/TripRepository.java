package com.felipe.trip_planner_trip_service.repositories;

import com.felipe.trip_planner_trip_service.models.Trip;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TripRepository extends JpaRepository<Trip, UUID> {
  Page<Trip> findAllByOwnerEmail(String ownerEmail, Pageable pageable);
  List<Trip> findAllByOwnerEmail(String ownerEmail);

  @Query("SELECT t FROM Trip t JOIN t.participants p WHERE p.email=:participantEmail")
  Page<Trip> findAllByParticipantEmail(@Param("participantEmail") String participantEmail, Pageable pageable);
}

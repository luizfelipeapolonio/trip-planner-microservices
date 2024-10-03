package com.felipe.trip_planner_trip_service.repositories;

import com.felipe.trip_planner_trip_service.models.Activity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ActivityRepository extends JpaRepository<Activity, UUID> {
  Page<Activity> findAllByTripId(UUID tripId, Pageable pageable);
  List<Activity> findAllByTripId(UUID tripId);
  Optional<Activity> findByIdAndTripId(UUID id, UUID tripId);

  @Modifying
  @Transactional
  @Query("DELETE FROM Activity a WHERE a.ownerEmail = :ownerEmail AND a.trip.id = :tripId")
  int deleteAllByOwnerEmailAndTripId(@Param("ownerEmail") String ownerEmail, @Param("tripId") UUID tripId);
}

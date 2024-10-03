package com.felipe.trip_planner_trip_service.repositories;

import com.felipe.trip_planner_trip_service.models.Link;
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

public interface LinkRepository extends JpaRepository<Link, UUID> {
  Page<Link> findAllByTripId(UUID tripId, Pageable pageable);
  List<Link> findAllByTripId(UUID tripId);
  Optional<Link> findByIdAndTripId(UUID id, UUID tripId);

  @Modifying
  @Transactional
  @Query("DELETE FROM Link l WHERE l.ownerEmail = :ownerEmail AND l.trip.id = :tripId")
  int deleteAllByOwnerEmailAndTripId(@Param("ownerEmail") String ownerEmail, @Param("tripId") UUID tripId);
}

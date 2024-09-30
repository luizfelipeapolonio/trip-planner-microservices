package com.felipe.trip_planner_trip_service.repositories;

import com.felipe.trip_planner_trip_service.models.Link;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LinkRepository extends JpaRepository<Link, UUID> {
  Page<Link> findAllByTripId(UUID tripId, Pageable pageable);
  List<Link> findAllByTripId(UUID tripId);
  Optional<Link> findByIdAndTripId(UUID id, UUID tripId);
}

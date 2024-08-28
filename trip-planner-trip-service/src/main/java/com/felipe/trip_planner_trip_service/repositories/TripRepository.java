package com.felipe.trip_planner_trip_service.repositories;

import com.felipe.trip_planner_trip_service.models.Trip;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TripRepository extends JpaRepository<Trip, UUID> {
  Page<Trip> findAllByOwnerEmail(String ownerEmail, Pageable pageable);
  List<Trip> findAllByOwnerEmail(String ownerEmail);
}

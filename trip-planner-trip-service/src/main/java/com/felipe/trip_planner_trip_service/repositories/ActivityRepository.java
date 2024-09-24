package com.felipe.trip_planner_trip_service.repositories;

import com.felipe.trip_planner_trip_service.models.Activity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ActivityRepository extends JpaRepository<Activity, UUID> {
}

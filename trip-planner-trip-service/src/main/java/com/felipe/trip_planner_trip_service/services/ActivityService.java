package com.felipe.trip_planner_trip_service.services;

import com.felipe.trip_planner_trip_service.dtos.activity.ActivityCreateDTO;
import com.felipe.trip_planner_trip_service.models.Activity;
import com.felipe.trip_planner_trip_service.models.Trip;
import com.felipe.trip_planner_trip_service.repositories.ActivityRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ActivityService {

  private final ActivityRepository activityRepository;
  private final TripService tripService;

  public ActivityService(ActivityRepository activityRepository, TripService tripService) {
    this.activityRepository = activityRepository;
    this.tripService = tripService;
  }

  public Activity create(UUID tripId, String userEmail, ActivityCreateDTO activityDTO) {
    // Checks if the authenticated user is the trip owner or a trip participant
    Trip trip = this.tripService.getById(tripId, userEmail);

    Activity newActivity = new Activity();
    newActivity.setDescription(activityDTO.description());
    newActivity.setOwnerEmail(userEmail);
    newActivity.setTrip(trip);

    return this.activityRepository.save(newActivity);
  }

  public Page<Activity> getAllTripActivities(UUID tripId, String userEmail, int pageNumber) {
    Trip trip = this.tripService.getById(tripId, userEmail);
    Pageable pagination = PageRequest.of(pageNumber, 10);
    return this.activityRepository.findAllByTripId(trip.getId(), pagination);
  }
}

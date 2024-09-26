package com.felipe.trip_planner_trip_service.services;

import com.felipe.trip_planner_trip_service.dtos.activity.ActivityCreateOrUpdateDTO;
import com.felipe.trip_planner_trip_service.exceptions.AccessDeniedException;
import com.felipe.trip_planner_trip_service.exceptions.RecordNotFoundException;
import com.felipe.trip_planner_trip_service.models.Activity;
import com.felipe.trip_planner_trip_service.models.Trip;
import com.felipe.trip_planner_trip_service.repositories.ActivityRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ActivityService {

  private final ActivityRepository activityRepository;
  private final TripService tripService;

  public ActivityService(ActivityRepository activityRepository, TripService tripService) {
    this.activityRepository = activityRepository;
    this.tripService = tripService;
  }

  public Activity create(UUID tripId, String userEmail, ActivityCreateOrUpdateDTO activityDTO) {
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

  public Activity getById(UUID tripId, UUID activityId, String userEmail) {
    Trip trip = this.tripService.getById(tripId, userEmail);
    return this.activityRepository.findByIdAndTripId(activityId, trip.getId())
      .orElseThrow(() -> new RecordNotFoundException("Atividade de id: '" + activityId + "' não encontrada"));
  }

  public Activity update(UUID tripId, UUID activityId, String userEmail, ActivityCreateOrUpdateDTO activityDTO) {
    Trip trip = this.tripService.getById(tripId, userEmail);
    return this.activityRepository.findByIdAndTripId(activityId, trip.getId())
      .map(foundActivity -> {
        String tripOwnerEmail = trip.getOwnerEmail();
        String activityOwnerEmail = foundActivity.getOwnerEmail();

        if(!tripOwnerEmail.equals(userEmail) && !activityOwnerEmail.equals(userEmail)) {
          throw new AccessDeniedException("Acesso negado: Você não tem permissão para alterar este recurso");
        }
        foundActivity.setDescription(activityDTO.description());
        return this.activityRepository.save(foundActivity);
      })
      .orElseThrow(() -> new RecordNotFoundException("Atividade de id: '" + activityId + "' não encontrada"));
  }

  public Activity delete(UUID tripId, UUID activityId, String userEmail) {
    Trip trip = this.tripService.getById(tripId, userEmail);
    Activity activity = this.activityRepository.findByIdAndTripId(activityId, trip.getId())
      .orElseThrow(() -> new RecordNotFoundException("Atividade de id: '" + activityId + "' não encontrada"));

    String tripOwnerEmail = trip.getOwnerEmail();
    String activityOwnerEmail = activity.getOwnerEmail();

    if(!tripOwnerEmail.equals(userEmail) && !activityOwnerEmail.equals(userEmail)) {
      throw new AccessDeniedException("Acesso negado: Você não tem permissão para excluir este recurso");
    }

    this.activityRepository.delete(activity);
    return activity;
  }

  public int deleteAllTripActivities(UUID tripId, String userEmail) {
    Trip trip = this.tripService.getById(tripId, userEmail);

    if(!trip.getOwnerEmail().equals(userEmail)) {
      throw new AccessDeniedException("Acesso negado: Você não tem permissão para remover estes recursos");
    }

    List<Activity> activities = this.activityRepository.findAllByTripId(tripId);
    this.activityRepository.deleteAll(activities);
    return activities.size();
  }
}

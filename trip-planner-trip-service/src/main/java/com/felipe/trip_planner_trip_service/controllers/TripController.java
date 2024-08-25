package com.felipe.trip_planner_trip_service.controllers;

import com.felipe.trip_planner_trip_service.dtos.trip.TripCreateDTO;
import com.felipe.trip_planner_trip_service.dtos.trip.TripResponseDTO;
import com.felipe.trip_planner_trip_service.models.Trip;
import com.felipe.trip_planner_trip_service.services.TripService;
import com.felipe.trip_planner_trip_service.utils.response.CustomResponseBody;
import com.felipe.trip_planner_trip_service.utils.response.ResponseConditionStatus;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trips")
public class TripController {

  private final TripService tripService;
  private final Logger logger = LoggerFactory.getLogger(TripController.class);

  public TripController(TripService tripService) {
    this.tripService = tripService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public CustomResponseBody<TripResponseDTO> create(
    @RequestHeader("username") String ownerName,
    @RequestHeader("userEmail") String ownerEmail,
    @RequestBody @Valid TripCreateDTO tripDTO
  ) {
    logger.info("Request Headers -> username: {} - userEmail: {}", ownerName, ownerEmail);
    Trip newTrip = this.tripService.create(ownerName, ownerEmail, tripDTO);
    TripResponseDTO tripResponseDTO = new TripResponseDTO(newTrip);

    CustomResponseBody<TripResponseDTO> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.CREATED);
    response.setMessage("Viagem criada com sucesso");
    response.setData(tripResponseDTO);
    return response;
  }
}

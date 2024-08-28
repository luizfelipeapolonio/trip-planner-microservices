package com.felipe.trip_planner_trip_service.controllers;

import com.felipe.trip_planner_trip_service.dtos.trip.TripCreateDTO;
import com.felipe.trip_planner_trip_service.dtos.trip.TripPageResponseDTO;
import com.felipe.trip_planner_trip_service.dtos.trip.TripResponseDTO;
import com.felipe.trip_planner_trip_service.dtos.trip.TripUpdateDTO;
import com.felipe.trip_planner_trip_service.models.Trip;
import com.felipe.trip_planner_trip_service.services.TripService;
import com.felipe.trip_planner_trip_service.utils.response.CustomResponseBody;
import com.felipe.trip_planner_trip_service.utils.response.ResponseConditionStatus;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  public CustomResponseBody<TripPageResponseDTO> getAllTripsFromAuthUser(
    @RequestHeader("userEmail") String ownerEmail,
    @RequestParam(defaultValue = "0") int page
  ) {
    logger.info("Request Headers -> userEmail: {}", ownerEmail);
    Page<Trip> trips = this.tripService.getAllTripsFromAuthUser(ownerEmail, page);
    List<TripResponseDTO> tripDTOs = trips.getContent().stream().map(TripResponseDTO::new).toList();
    TripPageResponseDTO tripPageDTO = new TripPageResponseDTO(tripDTOs, trips.getTotalElements(), trips.getTotalPages());

    CustomResponseBody<TripPageResponseDTO> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.OK);
    response.setMessage("Todas as viagens do usuário de email '" + ownerEmail + "'");
    response.setData(tripPageDTO);
    return response;
  }

  @DeleteMapping
  @ResponseStatus(HttpStatus.OK)
  public CustomResponseBody<String> deleteAllTripsFromAuthUser(@RequestHeader("userEmail") String ownerEmail) {
    logger.info("Request Headers -> userEmail: {}", ownerEmail);
    int quantityOfDeletedTrips = this.tripService.deleteAllTripsFromAuthUser(ownerEmail);

    CustomResponseBody<String> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.OK);
    response.setMessage("Todas as viagens do usuário de email: '" + ownerEmail + "' foram excluídas com sucesso");
    response.setData("Quantidade de viagens excluídas: " + quantityOfDeletedTrips);
    return response;
  }

  @PatchMapping("/{tripId}")
  @ResponseStatus(HttpStatus.OK)
  public CustomResponseBody<TripResponseDTO> update(
    @RequestHeader("userEmail") String ownerEmail,
    @PathVariable UUID tripId,
    @RequestBody @Valid TripUpdateDTO tripDTO
  ) {
    logger.info("Request Header -> userEmail: {}", ownerEmail);
    Trip updatedTrip = this.tripService.update(tripId, ownerEmail, tripDTO);
    TripResponseDTO tripResponseDTO = new TripResponseDTO(updatedTrip);

    CustomResponseBody<TripResponseDTO> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.OK);
    response.setMessage("Viagem atualizada com sucesso");
    response.setData(tripResponseDTO);
    return response;
  }

  @GetMapping("/{tripId}")
  @ResponseStatus(HttpStatus.OK)
  public CustomResponseBody<TripResponseDTO> getById(
    @RequestHeader("userEmail") String ownerEmail,
    @PathVariable UUID tripId
  ) {
    logger.info("Request Header -> userEmail: {}", ownerEmail);
    Trip trip = this.tripService.getById(tripId, ownerEmail);
    TripResponseDTO tripResponseDTO = new TripResponseDTO(trip);

    CustomResponseBody<TripResponseDTO> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.OK);
    response.setMessage("Viagem de id: '" + tripId + "' encontrada");
    response.setData(tripResponseDTO);
    return response;
  }

  @DeleteMapping("/{tripId}")
  @ResponseStatus(HttpStatus.OK)
  public CustomResponseBody<Map<String, TripResponseDTO>> delete(
    @RequestHeader("userEmail") String ownerEmail,
    @PathVariable UUID tripId
  ) {
    logger.info("Request Header -> userEmail: {}", ownerEmail);
    Trip deletedTrip = this.tripService.delete(tripId, ownerEmail);
    TripResponseDTO tripResponseDTO = new TripResponseDTO(deletedTrip);

    Map<String, TripResponseDTO> tripResponseMap = new HashMap<>(1);
    tripResponseMap.put("deletedTrip", tripResponseDTO);

    CustomResponseBody<Map<String, TripResponseDTO>> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.OK);
    response.setMessage("Viagem de id: '" + tripId + "' excluída com sucesso");
    response.setData(tripResponseMap);
    return response;
  }
}

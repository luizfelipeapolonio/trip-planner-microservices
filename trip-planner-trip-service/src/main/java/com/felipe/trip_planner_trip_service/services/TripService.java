package com.felipe.trip_planner_trip_service.services;

import com.felipe.trip_planner_trip_service.dtos.trip.TripCreateDTO;
import com.felipe.trip_planner_trip_service.dtos.trip.TripDateDTO;
import com.felipe.trip_planner_trip_service.dtos.trip.TripUpdateDTO;
import com.felipe.trip_planner_trip_service.exceptions.AccessDeniedException;
import com.felipe.trip_planner_trip_service.exceptions.InvalidDateException;
import com.felipe.trip_planner_trip_service.exceptions.RecordNotFoundException;
import com.felipe.trip_planner_trip_service.models.Trip;
import com.felipe.trip_planner_trip_service.repositories.TripRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.UUID;

@Service
public class TripService {

  private final TripRepository tripRepository;
  private final Logger logger = LoggerFactory.getLogger(TripService.class);

  public TripService(TripRepository tripRepository) {
    this.tripRepository = tripRepository;
  }

  public Trip create(String ownerName, String ownerEmail, TripCreateDTO tripDTO) {
    LocalDate startDate = this.convertDate(tripDTO.startsAt());
    LocalDate endDate = this.convertDate(tripDTO.endsAt());

    if(endDate.isBefore(startDate)) {
      logger.error("Data do término antes da data do início. startsAt: {} - endsAt: {}", startDate, endDate);
      throw new InvalidDateException("A data de término da viagem não pode ser antes da data de início");
    }

    Trip newTrip = new Trip();
    newTrip.setDestination(tripDTO.destination());
    newTrip.setOwnerEmail(ownerEmail);
    newTrip.setOwnerName(ownerName);
    newTrip.setStartsAt(startDate);
    newTrip.setEndsAt(endDate);

    return this.tripRepository.save(newTrip);
  }

  public Page<Trip> getAllTripsFromAuthUser(String ownerEmail, int pageNumber) {
    Pageable pagination = PageRequest.of(pageNumber, 10);
    return this.tripRepository.findAllByOwnerEmail(ownerEmail, pagination);
  }

  public Trip getById(UUID tripId, String ownerEmail) {
    Trip trip = this.tripRepository.findById(tripId)
      .orElseThrow(() -> new RecordNotFoundException("Viagem de id: '" + tripId + "' não encontrada"));

    // TODO: verificar se o usuário autenticado é um participante da viagem
    if(!trip.getOwnerEmail().equals(ownerEmail)) {
      throw new AccessDeniedException("Acesso negado: Você não tem permissão para acessar este recurso");
    }
    return trip;
  }

  public Trip update(UUID tripId, String ownerEmail, TripUpdateDTO tripDTO) {
    return this.tripRepository.findById(tripId)
      .map(foundTrip -> {
        if(!foundTrip.getOwnerEmail().equals(ownerEmail)) {
          throw new AccessDeniedException("Acesso negado: Você não tem permissão para alterar este recurso");
        }
        if(tripDTO.destination() != null) {
          foundTrip.setDestination(tripDTO.destination());
        }

        // When only new startsAt date is provided
        if(tripDTO.startsAt() != null && tripDTO.endsAt() == null) {
          LocalDate newStartDate = this.convertDate(tripDTO.startsAt());
          if(newStartDate.isAfter(foundTrip.getEndsAt())) {
            throw new InvalidDateException("A data de início não pode ser depois da data do término");
          }
          foundTrip.setStartsAt(newStartDate);
        }

        // When only new endsAt date is provided
        if(tripDTO.endsAt() != null && tripDTO.startsAt() == null) {
          LocalDate newEndDate = this.convertDate(tripDTO.endsAt());
          if(newEndDate.isBefore(foundTrip.getStartsAt())) {
            throw new InvalidDateException("A data do término não pode ser antes da data do início");
          }
          foundTrip.setEndsAt(newEndDate);
        }

        // When both startsAt and endsAt is provided
        if(tripDTO.startsAt() != null && tripDTO.endsAt() != null) {
          LocalDate newStartDate = this.convertDate(tripDTO.startsAt());
          LocalDate newEndDate = this.convertDate(tripDTO.endsAt());

          if(newEndDate.isBefore(newStartDate)) {
            throw new InvalidDateException("A data do término não pode ser antes da data do início");
          }
          foundTrip.setStartsAt(newStartDate);
          foundTrip.setEndsAt(newEndDate);
        }
        return this.tripRepository.save(foundTrip);
      })
      .orElseThrow(() -> new RecordNotFoundException("Viagem de id: '" + tripId + "' não encontrada"));
  }

  private LocalDate convertDate(TripDateDTO date) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    String dateString = String.format("%s-%s-%s", date.day(), date.month(), date.year());

    try {
      return LocalDate.parse(dateString, formatter);
    } catch(DateTimeParseException e) {
      logger.error("Falha ao converter data! Valor: {} \nMessage: {}", e.getParsedString(), e.getMessage());
      throw new InvalidDateException(
        "Data inválida! Não foi possível converter o valor '" + e.getParsedString()  + "' em data", e
      );
    }
  }
}

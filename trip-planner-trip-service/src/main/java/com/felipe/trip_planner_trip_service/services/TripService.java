package com.felipe.trip_planner_trip_service.services;

import com.felipe.trip_planner_trip_service.dtos.trip.TripCreateDTO;
import com.felipe.trip_planner_trip_service.dtos.trip.TripDateDTO;
import com.felipe.trip_planner_trip_service.exceptions.InvalidDateException;
import com.felipe.trip_planner_trip_service.models.Trip;
import com.felipe.trip_planner_trip_service.repositories.TripRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

@Service
public class TripService {

  private final TripRepository tripRepository;
  private final Logger logger = LoggerFactory.getLogger(TripService.class);

  public TripService(TripRepository tripRepository) {
    this.tripRepository = tripRepository;
  }

  public Trip create(String ownerName, String ownerEmail, TripCreateDTO tripDTO) {
    Map<String, LocalDate> tripDates = this.convertAndValidateDate(tripDTO.startsAt(), tripDTO.endsAt());

    Trip newTrip = new Trip();
    newTrip.setDestination(tripDTO.destination());
    newTrip.setOwnerEmail(ownerEmail);
    newTrip.setOwnerName(ownerName);
    newTrip.setStartsAt(tripDates.get("startsAt"));
    newTrip.setEndsAt(tripDates.get("endsAt"));

    return this.tripRepository.save(newTrip);
  }

  private Map<String, LocalDate> convertAndValidateDate(TripDateDTO startsAt, TripDateDTO endsAt) {
    String startDateString = startsAt.day() + "-" + startsAt.month() + "-" + startsAt.year();
    String endDateString = endsAt.day() + "-" + endsAt.month() + "-" + endsAt.year();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    Map<String, LocalDate> tripDates = new HashMap<>(2);

    try {
      LocalDate startDate = LocalDate.parse(startDateString, formatter);
      LocalDate endDate = LocalDate.parse(endDateString, formatter);

      if(endDate.isBefore(startDate)) {
        logger.error("Data do término antes da data do início. startsAt: {} - endsAt: {}", startDate, endDate);
        throw new InvalidDateException("A data de término da viagem não pode ser antes da data de início");
      }

      tripDates.put("startsAt", startDate);
      tripDates.put("endsAt", endDate);
      return tripDates;

    } catch(DateTimeParseException e) {
      logger.error("Falha ao converter data! Valor: {} \nMessage: {}", e.getParsedString(), e.getMessage());
      throw new InvalidDateException(
        "Data inválida! Não foi possível converter o valor '" + e.getParsedString()  + "' em data", e
      );
    }
  }
}

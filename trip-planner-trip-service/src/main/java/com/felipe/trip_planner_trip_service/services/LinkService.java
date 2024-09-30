package com.felipe.trip_planner_trip_service.services;

import com.felipe.trip_planner_trip_service.dtos.link.LinkCreateDTO;
import com.felipe.trip_planner_trip_service.dtos.link.LinkUpdateDTO;
import com.felipe.trip_planner_trip_service.exceptions.AccessDeniedException;
import com.felipe.trip_planner_trip_service.exceptions.RecordNotFoundException;
import com.felipe.trip_planner_trip_service.models.Link;
import com.felipe.trip_planner_trip_service.models.Trip;
import com.felipe.trip_planner_trip_service.repositories.LinkRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class LinkService {

  private final LinkRepository linkRepository;
  private final TripService tripService;

  public LinkService(LinkRepository linkRepository, TripService tripService) {
    this.linkRepository = linkRepository;
    this.tripService = tripService;
  }

  public Link create(UUID tripId, String userEmail, LinkCreateDTO linkDTO) {
    Trip trip = this.tripService.getById(tripId, userEmail);

    Link newLink = new Link();
    newLink.setTitle(linkDTO.title());
    newLink.setUrl(linkDTO.url());
    newLink.setOwnerEmail(userEmail);
    newLink.setTrip(trip);

    return this.linkRepository.save(newLink);
  }

  public Page<Link> getAllTripLinks(UUID tripId, String userEmail, int pageNumber) {
    Trip trip = this.tripService.getById(tripId, userEmail);
    Pageable pagination = PageRequest.of(pageNumber, 10);
    return this.linkRepository.findAllByTripId(trip.getId(), pagination);
  }

  public Link getById(UUID tripId, UUID linkId,  String userEmail) {
    Trip trip = this.tripService.getById(tripId, userEmail);
    return this.linkRepository.findByIdAndTripId(linkId, trip.getId())
      .orElseThrow(() -> new RecordNotFoundException("Link de id: '" + linkId + "' não encontrado"));
  }
  
  public Link update(UUID tripId, UUID linkId, String userEmail, LinkUpdateDTO linkDTO) {
    Trip trip = this.tripService.getById(tripId, userEmail);
    return this.linkRepository.findByIdAndTripId(linkId, trip.getId())
      .map(foundLink -> {
        String tripOwnerEmail = trip.getOwnerEmail();
        String linkOwnerEmail = foundLink.getOwnerEmail();

        if(!tripOwnerEmail.equals(userEmail) && !linkOwnerEmail.equals(userEmail)) {
          throw new AccessDeniedException("Acesso negado: Você não tem permissão para alterar este recurso");
        }

        if(linkDTO.title() != null) {
          foundLink.setTitle(linkDTO.title());
        }
        if(linkDTO.url() != null) {
          foundLink.setUrl(linkDTO.url());
        }
        return this.linkRepository.save(foundLink);
      })
      .orElseThrow(() -> new RecordNotFoundException("Link de id: '" + linkId + "' não encontrado"));
  }
}

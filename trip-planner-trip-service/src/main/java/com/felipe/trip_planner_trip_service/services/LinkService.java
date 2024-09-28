package com.felipe.trip_planner_trip_service.services;

import com.felipe.trip_planner_trip_service.dtos.link.LinkCreateDTO;
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
    newLink.setLink(linkDTO.link());
    newLink.setOwnerEmail(userEmail);
    newLink.setTrip(trip);

    return this.linkRepository.save(newLink);
  }

  public Page<Link> getAllTripLinks(UUID tripId, String userEmail, int pageNumber) {
    Trip trip = this.tripService.getById(tripId, userEmail);
    Pageable pagination = PageRequest.of(pageNumber, 10);
    return this.linkRepository.findAllByTripId(trip.getId(), pagination);
  }
}

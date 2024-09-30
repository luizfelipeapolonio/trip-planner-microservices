package com.felipe.trip_planner_trip_service.services;

import com.felipe.trip_planner_trip_service.dtos.link.LinkCreateDTO;
import com.felipe.trip_planner_trip_service.dtos.link.LinkUpdateDTO;
import com.felipe.trip_planner_trip_service.exceptions.AccessDeniedException;
import com.felipe.trip_planner_trip_service.exceptions.RecordNotFoundException;
import com.felipe.trip_planner_trip_service.models.Link;
import com.felipe.trip_planner_trip_service.models.Trip;
import com.felipe.trip_planner_trip_service.repositories.LinkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.catchException;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.any;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class LinkServiceTest {

  @InjectMocks
  LinkService linkService;

  @Mock
  LinkRepository linkRepository;

  @Mock
  TripService tripService;

  private List<Link> links;
  private Trip trip;

  @BeforeEach
  void setUp() {
    LocalDateTime mockDateTime = LocalDateTime.parse("2024-01-01T12:00:00.123456");
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    Trip trip = new Trip();
    trip.setId(UUID.fromString("62dac895-a1f0-4140-b52b-4c12cb82c6ff"));
    trip.setDestination("Destino 1");
    trip.setOwnerName("User 1");
    trip.setOwnerEmail("user1@email.com");
    trip.setStartsAt(LocalDate.parse("24-08-2024", formatter));
    trip.setEndsAt(LocalDate.parse("26-08-2024", formatter));
    trip.setCreatedAt(mockDateTime);
    trip.setUpdatedAt(mockDateTime);

    Link link1 = new Link();
    link1.setId(UUID.fromString("77b52d55-3430-4829-a8a4-64ee68336a35"));
    link1.setTitle("Link 1");
    link1.setUrl("https://somelink.com");
    link1.setOwnerEmail("user2@email.com");
    link1.setCreatedAt(mockDateTime);
    link1.setUpdatedAt(mockDateTime);
    link1.setTrip(trip);

    Link link2 = new Link();
    link2.setId(UUID.fromString("002d3420-7af9-4ea2-9ab8-8afc2fa81da8"));
    link2.setTitle("Link 2");
    link2.setUrl("https://somelink2.com");
    link2.setOwnerEmail("user2@email.com");
    link2.setCreatedAt(mockDateTime);
    link2.setUpdatedAt(mockDateTime);
    link2.setTrip(trip);

    this.links = List.of(link1, link2);
    this.trip = trip;
  }

  @Test
  @DisplayName("create - Should successfully create a link and return it")
  void createSuccess() {
    Link link = this.links.get(0);
    String userEmail = "user2@email.com";
    var linkDTO = new LinkCreateDTO("Link 1", "https://somelink.com");

    when(this.tripService.getById(this.trip.getId(), userEmail)).thenReturn(this.trip);
    when(this.linkRepository.save(any(Link.class))).thenReturn(link);

    Link createdLink = this.linkService.create(this.trip.getId(), userEmail, linkDTO);

    assertThat(createdLink.getId()).isEqualTo(link.getId());
    assertThat(createdLink.getTitle()).isEqualTo(link.getTitle());
    assertThat(createdLink.getUrl()).isEqualTo(link.getUrl());
    assertThat(createdLink.getOwnerEmail()).isEqualTo(link.getOwnerEmail());
    assertThat(createdLink.getCreatedAt()).isEqualTo(link.getCreatedAt());
    assertThat(createdLink.getUpdatedAt()).isEqualTo(link.getUpdatedAt());
    assertThat(createdLink.getTrip().getId()).isEqualTo(link.getTrip().getId());

    verify(this.tripService, times(1)).getById(this.trip.getId(), userEmail);
    verify(this.linkRepository, times(1)).save(any(Link.class));
  }

  @Test
  @DisplayName("getAllTripLinks - Should successfully get all links from a trip and return a page of links")
  void getAllTripLinksSuccess() {
    Page<Link> links = new PageImpl<>(this.links);
    Pageable pagination = PageRequest.of(0, 10);
    String userEmail = "user2@email.com";

    when(this.tripService.getById(this.trip.getId(), userEmail)).thenReturn(this.trip);
    when(this.linkRepository.findAllByTripId(this.trip.getId(), pagination)).thenReturn(links);

    Page<Link> allLinks = this.linkService.getAllTripLinks(this.trip.getId(), userEmail, 0);

    assertThat(allLinks.getTotalElements()).isEqualTo(links.getTotalElements());
    assertThat(allLinks.getTotalPages()).isEqualTo(links.getTotalPages());
    assertThat(allLinks.getContent())
      .allSatisfy(link -> assertThat(link.getTrip().getId()).isEqualTo(this.trip.getId()));

    verify(this.tripService, times(1)).getById(this.trip.getId(), userEmail);
    verify(this.linkRepository, times(1)).findAllByTripId(this.trip.getId(), pagination);
  }

  @Test
  @DisplayName("getById - Should successfully get a link from a trip")
  void getByIdSuccess() {
    Link link = this.links.get(0);
    String userEmail = "user2@email.com";

    when(this.tripService.getById(this.trip.getId(), userEmail)).thenReturn(this.trip);
    when(this.linkRepository.findByIdAndTripId(link.getId(), this.trip.getId())).thenReturn(Optional.of(link));

    Link foundLink = this.linkService.getById(this.trip.getId(), link.getId(), userEmail);

    assertThat(foundLink.getId()).isEqualTo(link.getId());
    assertThat(foundLink.getTitle()).isEqualTo(link.getTitle());
    assertThat(foundLink.getUrl()).isEqualTo(link.getUrl());
    assertThat(foundLink.getOwnerEmail()).isEqualTo(link.getOwnerEmail());
    assertThat(foundLink.getTrip().getId()).isEqualTo(link.getTrip().getId());
    assertThat(foundLink.getCreatedAt()).isEqualTo(link.getCreatedAt());
    assertThat(foundLink.getUpdatedAt()).isEqualTo(link.getUpdatedAt());

    verify(this.tripService, times(1)).getById(this.trip.getId(), userEmail);
    verify(this.linkRepository, times(1)).findByIdAndTripId(link.getId(), this.trip.getId());
  }

  @Test
  @DisplayName("getById - Should throw a RecordNotFoundException if the link is not found")
  void getByIdFailsByLinkNotFound() {
    UUID linkId = this.links.get(0).getId();
    String userEmail = "user2@email.com";

    when(this.tripService.getById(this.trip.getId(), userEmail)).thenReturn(this.trip);
    when(this.linkRepository.findByIdAndTripId(linkId, this.trip.getId())).thenReturn(Optional.empty());

    Exception thrown = catchException(() -> this.linkService.getById(this.trip.getId(), linkId, userEmail));

    assertThat(thrown)
      .isExactlyInstanceOf(RecordNotFoundException.class)
      .hasMessage("Link de id: '%s' não encontrado", linkId);

    verify(this.tripService, times(1)).getById(this.trip.getId(), userEmail);
    verify(this.linkRepository, times(1)).findByIdAndTripId(linkId, this.trip.getId());
  }

  @Test
  @DisplayName("update - The link owner should be allowed to successfully update the link")
  void updateBeingLinkOwnerSuccess() {
    Link link = this.links.get(0);
    String userEmail = "user2@email.com";
    LinkUpdateDTO linkUpdateDTO = new LinkUpdateDTO("Link 1 atualizado", "https://updatedlink.com");

    when(this.tripService.getById(this.trip.getId(), userEmail)).thenReturn(this.trip);
    when(this.linkRepository.findByIdAndTripId(link.getId(), this.trip.getId())).thenReturn(Optional.of(link));
    when(this.linkRepository.save(link)).thenReturn(link);

    Link updatedLink = this.linkService.update(this.trip.getId(), link.getId(), userEmail, linkUpdateDTO);

    assertThat(updatedLink.getId()).isEqualTo(link.getId());
    assertThat(updatedLink.getTitle()).isEqualTo(linkUpdateDTO.title());
    assertThat(updatedLink.getUrl()).isEqualTo(linkUpdateDTO.url());
    assertThat(updatedLink.getOwnerEmail()).isEqualTo(link.getOwnerEmail());
    assertThat(updatedLink.getTrip().getId()).isEqualTo(link.getTrip().getId());
    assertThat(updatedLink.getCreatedAt()).isEqualTo(link.getCreatedAt());
    assertThat(updatedLink.getUpdatedAt()).isEqualTo(link.getUpdatedAt());

    verify(this.tripService, times(1)).getById(this.trip.getId(), userEmail);
    verify(this.linkRepository, times(1)).findByIdAndTripId(link.getId(), this.trip.getId());
    verify(this.linkRepository, times(1)).save(link);
  }

  @Test
  @DisplayName("update - The trip owner should be allowed to successfully update the link")
  void updateBeingTripOwnerSuccess() {
    Link link = this.links.get(0);
    String userEmail = "user1@email.com";
    LinkUpdateDTO linkUpdateDTO = new LinkUpdateDTO("Link 1 atualizado", "https://updatedlink.com");

    when(this.tripService.getById(this.trip.getId(), userEmail)).thenReturn(this.trip);
    when(this.linkRepository.findByIdAndTripId(link.getId(), this.trip.getId())).thenReturn(Optional.of(link));
    when(this.linkRepository.save(link)).thenReturn(link);

    Link updatedLink = this.linkService.update(this.trip.getId(), link.getId(), userEmail, linkUpdateDTO);

    assertThat(updatedLink.getId()).isEqualTo(link.getId());
    assertThat(updatedLink.getTitle()).isEqualTo(linkUpdateDTO.title());
    assertThat(updatedLink.getUrl()).isEqualTo(linkUpdateDTO.url());
    assertThat(updatedLink.getOwnerEmail()).isEqualTo(link.getOwnerEmail());
    assertThat(updatedLink.getTrip().getId()).isEqualTo(link.getTrip().getId());
    assertThat(updatedLink.getCreatedAt()).isEqualTo(link.getCreatedAt());
    assertThat(updatedLink.getUpdatedAt()).isEqualTo(link.getUpdatedAt());

    verify(this.tripService, times(1)).getById(this.trip.getId(), userEmail);
    verify(this.linkRepository, times(1)).findByIdAndTripId(link.getId(), this.trip.getId());
    verify(this.linkRepository, times(1)).save(link);
  }

  @Test
  @DisplayName("update - Should throw an AccessDeniedException if the user is not the trip or the link owner")
  void updateFailsByUserIsNotTripOrLinkOwner() {
    Link link = this.links.get(0);
    String userEmail = "user3@email.com";
    LinkUpdateDTO linkUpdateDTO = new LinkUpdateDTO("Link 1 atualizado", "https://updatedlink.com");

    when(this.tripService.getById(this.trip.getId(), userEmail)).thenReturn(this.trip);
    when(this.linkRepository.findByIdAndTripId(link.getId(), this.trip.getId())).thenReturn(Optional.of(link));

    Exception thrown = catchException(() -> this.linkService.update(this.trip.getId(), link.getId(), userEmail, linkUpdateDTO));

    assertThat(thrown)
      .isExactlyInstanceOf(AccessDeniedException.class)
      .hasMessage("Acesso negado: Você não tem permissão para alterar este recurso");

    verify(this.tripService, times(1)).getById(this.trip.getId(), userEmail);
    verify(this.linkRepository, times(1)).findByIdAndTripId(link.getId(), this.trip.getId());
    verify(this.linkRepository, never()).save(any());
  }

  @Test
  @DisplayName("update - Should throw a RecordNotFoundException if the link is not found")
  void updateFailsByLinkNotFound() {
    Link link = this.links.get(0);
    String userEmail = "user2@email.com";
    LinkUpdateDTO linkUpdateDTO = new LinkUpdateDTO("Link 1 atualizado", "https://updatedlink.com");

    when(this.tripService.getById(this.trip.getId(), userEmail)).thenReturn(this.trip);
    when(this.linkRepository.findByIdAndTripId(link.getId(), this.trip.getId())).thenReturn(Optional.empty());

    Exception thrown = catchException(() -> this.linkService.update(this.trip.getId(), link.getId(), userEmail, linkUpdateDTO));

    assertThat(thrown)
      .isExactlyInstanceOf(RecordNotFoundException.class)
      .hasMessage("Link de id: '%s' não encontrado", link.getId());

    verify(this.tripService, times(1)).getById(this.trip.getId(), userEmail);
    verify(this.linkRepository, times(1)).findByIdAndTripId(link.getId(), this.trip.getId());
    verify(this.linkRepository, never()).save(any());
  }

  @Test
  @DisplayName("delete - The link owner should be allowed to successfully delete a link")
  void deleteBeingLinkOwnerSuccess() {
    Link link = this.links.get(0);
    String userEmail = "user2@email.com";
    ArgumentCaptor<Link> linkCapture = ArgumentCaptor.forClass(Link.class);

    when(this.tripService.getById(this.trip.getId(), userEmail)).thenReturn(this.trip);
    when(this.linkRepository.findByIdAndTripId(link.getId(), this.trip.getId())).thenReturn(Optional.of(link));
    doNothing().when(this.linkRepository).delete(linkCapture.capture());

    Link deletedLink = this.linkService.delete(this.trip.getId(), link.getId(), userEmail);

    assertThat(deletedLink.getId()).isEqualTo(linkCapture.getValue().getId());
    assertThat(deletedLink.getTitle()).isEqualTo(linkCapture.getValue().getTitle());
    assertThat(deletedLink.getUrl()).isEqualTo(linkCapture.getValue().getUrl());
    assertThat(deletedLink.getOwnerEmail()).isEqualTo(linkCapture.getValue().getOwnerEmail());
    assertThat(deletedLink.getTrip().getId()).isEqualTo(linkCapture.getValue().getTrip().getId());
    assertThat(deletedLink.getCreatedAt()).isEqualTo(linkCapture.getValue().getCreatedAt());
    assertThat(deletedLink.getUpdatedAt()).isEqualTo(linkCapture.getValue().getUpdatedAt());

    verify(this.tripService, times(1)).getById(this.trip.getId(), userEmail);
    verify(this.linkRepository, times(1)).findByIdAndTripId(link.getId(), this.trip.getId());
    verify(this.linkRepository, times(1)).delete(link);
  }

  @Test
  @DisplayName("delete - The trip owner should be allowed to successfully delete a link")
  void deleteBeingTripOwnerSuccess() {
    Link link = this.links.get(0);
    String userEmail = "user1@email.com";
    ArgumentCaptor<Link> linkCapture = ArgumentCaptor.forClass(Link.class);

    when(this.tripService.getById(this.trip.getId(), userEmail)).thenReturn(this.trip);
    when(this.linkRepository.findByIdAndTripId(link.getId(), this.trip.getId())).thenReturn(Optional.of(link));
    doNothing().when(this.linkRepository).delete(linkCapture.capture());

    Link deletedLink = this.linkService.delete(this.trip.getId(), link.getId(), userEmail);

    assertThat(deletedLink.getId()).isEqualTo(linkCapture.getValue().getId());
    assertThat(deletedLink.getTitle()).isEqualTo(linkCapture.getValue().getTitle());
    assertThat(deletedLink.getUrl()).isEqualTo(linkCapture.getValue().getUrl());
    assertThat(deletedLink.getOwnerEmail()).isEqualTo(linkCapture.getValue().getOwnerEmail());
    assertThat(deletedLink.getTrip().getId()).isEqualTo(linkCapture.getValue().getTrip().getId());
    assertThat(deletedLink.getCreatedAt()).isEqualTo(linkCapture.getValue().getCreatedAt());
    assertThat(deletedLink.getUpdatedAt()).isEqualTo(linkCapture.getValue().getUpdatedAt());

    verify(this.tripService, times(1)).getById(this.trip.getId(), userEmail);
    verify(this.linkRepository, times(1)).findByIdAndTripId(link.getId(), this.trip.getId());
    verify(this.linkRepository, times(1)).delete(link);
  }

  @Test
  @DisplayName("delete - Should throw an AccessDeniedException if the user is not the trip or the link owner")
  void deleteFailsByUserIsNotTripOrLinkOwner() {
    Link link = this.links.get(0);
    String userEmail = "user3@email.com";

    when(this.tripService.getById(this.trip.getId(), userEmail)).thenReturn(this.trip);
    when(this.linkRepository.findByIdAndTripId(link.getId(), this.trip.getId())).thenReturn(Optional.of(link));

    Exception thrown = catchException(() -> this.linkService.delete(this.trip.getId(), link.getId(), userEmail));

    assertThat(thrown)
      .isExactlyInstanceOf(AccessDeniedException.class)
      .hasMessage("Acesso negado: Você não tem permissão para remover este recurso");

    verify(this.tripService, times(1)).getById(this.trip.getId(), userEmail);
    verify(this.linkRepository, times(1)).findByIdAndTripId(link.getId(), this.trip.getId());
    verify(this.linkRepository, never()).delete(any());
  }

  @Test
  @DisplayName("delete - Should throw a RecordNotFoundException if the link is not found")
  void deleteFailsByLinkNotFound() {
    UUID linkId = this.links.get(0).getId();
    String userEmail = "user2@email.com";

    when(this.tripService.getById(this.trip.getId(), userEmail)).thenReturn(this.trip);
    when(this.linkRepository.findByIdAndTripId(linkId, this.trip.getId())).thenReturn(Optional.empty());

    Exception thrown = catchException(() -> this.linkService.delete(this.trip.getId(), linkId, userEmail));

    assertThat(thrown)
      .isExactlyInstanceOf(RecordNotFoundException.class)
      .hasMessage("Link de id: '%s' não encontrado", linkId);

    verify(this.tripService, times(1)).getById(this.trip.getId(), userEmail);
    verify(this.linkRepository, times(1)).findByIdAndTripId(linkId, this.trip.getId());
    verify(this.linkRepository, never()).delete(any());
  }
}

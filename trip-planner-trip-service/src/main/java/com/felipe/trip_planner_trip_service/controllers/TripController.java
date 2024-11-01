package com.felipe.trip_planner_trip_service.controllers;

import com.felipe.trip_planner_trip_service.dtos.activity.ActivityCreateOrUpdateDTO;
import com.felipe.trip_planner_trip_service.dtos.activity.ActivityResponseDTO;
import com.felipe.trip_planner_trip_service.dtos.activity.ActivityResponsePageDTO;
import com.felipe.trip_planner_trip_service.dtos.activity.mapper.ActivityMapper;
import com.felipe.trip_planner_trip_service.dtos.invite.InviteParticipantDTO;
import com.felipe.trip_planner_trip_service.dtos.link.LinkCreateDTO;
import com.felipe.trip_planner_trip_service.dtos.link.LinkResponseDTO;
import com.felipe.trip_planner_trip_service.dtos.link.LinkResponsePageDTO;
import com.felipe.trip_planner_trip_service.dtos.link.LinkUpdateDTO;
import com.felipe.trip_planner_trip_service.dtos.link.mapper.LinkMapper;
import com.felipe.trip_planner_trip_service.dtos.participant.ParticipantResponseDTO;
import com.felipe.trip_planner_trip_service.dtos.participant.ParticipantResponsePageDTO;
import com.felipe.trip_planner_trip_service.dtos.participant.mapper.ParticipantMapper;
import com.felipe.trip_planner_trip_service.dtos.trip.TripCreateDTO;
import com.felipe.trip_planner_trip_service.dtos.trip.TripExtraInfoResponseDTO;
import com.felipe.trip_planner_trip_service.dtos.trip.TripFullResponseDTO;
import com.felipe.trip_planner_trip_service.dtos.trip.TripPageResponseDTO;
import com.felipe.trip_planner_trip_service.dtos.trip.TripResponseDTO;
import com.felipe.trip_planner_trip_service.dtos.trip.TripUpdateDTO;
import com.felipe.trip_planner_trip_service.models.Activity;
import com.felipe.trip_planner_trip_service.models.Link;
import com.felipe.trip_planner_trip_service.models.Participant;
import com.felipe.trip_planner_trip_service.models.Trip;
import com.felipe.trip_planner_trip_service.services.ActivityService;
import com.felipe.trip_planner_trip_service.services.InviteService;
import com.felipe.trip_planner_trip_service.services.LinkService;
import com.felipe.trip_planner_trip_service.services.ParticipantService;
import com.felipe.trip_planner_trip_service.services.TripService;
import com.felipe.trip_planner_trip_service.utils.response.CustomResponseBody;
import com.felipe.trip_planner_trip_service.utils.response.ResponseConditionStatus;
import jakarta.validation.Valid;
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
  private final InviteService inviteService;
  private final ParticipantService participantService;
  private final ParticipantMapper participantMapper;
  private final ActivityService activityService;
  private final ActivityMapper activityMapper;
  private final LinkService linkService;
  private final LinkMapper linkMapper;

  public TripController(
    TripService tripService,
    InviteService inviteService,
    ParticipantService participantService,
    ParticipantMapper participantMapper,
    ActivityService activityService,
    ActivityMapper activityMapper,
    LinkService linkService,
    LinkMapper linkMapper
  ) {
    this.tripService = tripService;
    this.inviteService = inviteService;
    this.participantService = participantService;
    this.participantMapper = participantMapper;
    this.activityService = activityService;
    this.activityMapper = activityMapper;
    this.linkService = linkService;
    this.linkMapper = linkMapper;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public CustomResponseBody<TripResponseDTO> create(
    @RequestHeader("username") String ownerName,
    @RequestHeader("userEmail") String ownerEmail,
    @RequestBody @Valid TripCreateDTO tripDTO
  ) {
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

  @GetMapping("/participant")
  @ResponseStatus(HttpStatus.OK)
  public CustomResponseBody<TripPageResponseDTO> getAllTripsAuthUserIsParticipant(
    @RequestHeader("userEmail") String userEmail,
    @RequestParam(defaultValue = "0") int page
  ) {
    Page<Trip> allTrips = this.tripService.getAllTripsAuthenticatedUserIsParticipant(userEmail, page);
    List<TripResponseDTO> tripDTOs = allTrips.stream().map(TripResponseDTO::new).toList();
    TripPageResponseDTO tripPageDTO = new TripPageResponseDTO(tripDTOs, allTrips.getTotalElements(), allTrips.getTotalPages());

    CustomResponseBody<TripPageResponseDTO> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.OK);
    response.setMessage("Todas as viagens que o usuário de e-mail: '" + userEmail + "' é um participante");
    response.setData(tripPageDTO);
    return response;
  }

  @DeleteMapping
  @ResponseStatus(HttpStatus.OK)
  public CustomResponseBody<String> deleteAllTripsFromAuthUser(@RequestHeader("userEmail") String ownerEmail) {
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
  public CustomResponseBody<TripFullResponseDTO> getById(
    @RequestHeader("userEmail") String userEmail,
    @PathVariable UUID tripId
  ) {
    Trip trip = this.tripService.getById(tripId, userEmail);
    Page<Participant> participants = this.participantService.getAllTripParticipants(tripId, userEmail, 0);
    Page<Activity> activities = this.activityService.getAllTripActivities(tripId, userEmail, 0);
    Page<Link> links = this.linkService.getAllTripLinks(tripId, userEmail, 0);

    TripResponseDTO tripResponseDTO = new TripResponseDTO(trip);
    ParticipantResponsePageDTO participantsPageDTO = this.participantMapper.toParticipantResponsePageDTO(participants);
    ActivityResponsePageDTO activitiesPageDTO = this.activityMapper.toActivityResponsePageDTO(activities);
    LinkResponsePageDTO linksPageDTO = this.linkMapper.toLinkResponsePageDTO(links);

    TripFullResponseDTO tripFullResponseDTO = new TripFullResponseDTO(
      tripResponseDTO,
      new TripExtraInfoResponseDTO(participantsPageDTO, activitiesPageDTO, linksPageDTO)
    );

    CustomResponseBody<TripFullResponseDTO> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.OK);
    response.setMessage("Viagem de id: '" + tripId + "' encontrada");
    response.setData(tripFullResponseDTO);
    return response;
  }

  @DeleteMapping("/{tripId}")
  @ResponseStatus(HttpStatus.OK)
  public CustomResponseBody<Map<String, TripResponseDTO>> delete(
    @RequestHeader("userEmail") String ownerEmail,
    @PathVariable UUID tripId
  ) {
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

  @PatchMapping("/{tripId}/confirm")
  @ResponseStatus(HttpStatus.OK)
  public CustomResponseBody<Void> confirmTrip(@RequestHeader("userEmail") String ownerEmail, @PathVariable UUID tripId) {
    this.tripService.confirmOrCancelTrip(tripId, ownerEmail, true);

    CustomResponseBody<Void> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.OK);
    response.setMessage("A viagem de id: '" + tripId + "' foi confirmada com sucesso");
    response.setData(null);
    return response;
  }

  @PatchMapping("/{tripId}/cancel")
  @ResponseStatus(HttpStatus.OK)
  public CustomResponseBody<Void> cancelTrip(@RequestHeader("userEmail") String ownerEmail, @PathVariable UUID tripId) {
    this.tripService.confirmOrCancelTrip(tripId, ownerEmail, false);

    CustomResponseBody<Void> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.OK);
    response.setMessage("A viagem de id: '" + tripId + "' foi cancelada com sucesso");
    response.setData(null);
    return response;
  }

  @PostMapping("/{tripId}/invite")
  @ResponseStatus(HttpStatus.OK)
  public CustomResponseBody<Void> inviteParticipant(
    @RequestHeader("userEmail") String ownerEmail,
    @PathVariable UUID tripId,
    @RequestBody InviteParticipantDTO inviteDTO
  ) {
    String email = this.inviteService.invite(tripId, ownerEmail, inviteDTO);

    CustomResponseBody<Void> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.OK);
    response.setMessage("Convite enviado com sucesso para: " + email);
    response.setData(null);
    return response;
  }

  @GetMapping("/{tripId}/participants")
  @ResponseStatus(HttpStatus.OK)
  public CustomResponseBody<ParticipantResponsePageDTO> getAllTripParticipants(
    @RequestHeader("userEmail") String userEmail,
    @PathVariable UUID tripId,
    @RequestParam(defaultValue = "0") int page
  ) {
    Page<Participant> allParticipants = this.participantService.getAllTripParticipants(tripId, userEmail, page);
    ParticipantResponsePageDTO participantPageDTO = this.participantMapper.toParticipantResponsePageDTO(allParticipants);

    CustomResponseBody<ParticipantResponsePageDTO> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.OK);
    response.setMessage("Todos os participantes da viagem de id: '" + tripId + "'");
    response.setData(participantPageDTO);
    return response;
  }

  @DeleteMapping("/{tripId}/participants/{participantId}")
  @ResponseStatus(HttpStatus.OK)
  public CustomResponseBody<Map<String, ParticipantResponseDTO>> removeParticipant(
    @RequestHeader("userEmail") String userEmail,
    @PathVariable UUID tripId,
    @PathVariable UUID participantId
  ) {
    Participant deletedParticipant = this.participantService.removeParticipant(tripId, participantId, userEmail);
    ParticipantResponseDTO participantResponseDTO = new ParticipantResponseDTO(deletedParticipant);

    Map<String, ParticipantResponseDTO> removedParticipantMap = new HashMap<>(1);
    removedParticipantMap.put("removedParticipant", participantResponseDTO);

    CustomResponseBody<Map<String, ParticipantResponseDTO>> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.OK);
    response.setMessage("Participante removido com sucesso");
    response.setData(removedParticipantMap);
    return response;
  }

  @PostMapping("/{tripId}/activities")
  @ResponseStatus(HttpStatus.CREATED)
  public CustomResponseBody<ActivityResponseDTO> createActivity(
    @RequestHeader("userEmail") String userEmail,
    @PathVariable UUID tripId,
    @RequestBody @Valid ActivityCreateOrUpdateDTO activityDTO
  ) {
    Activity createdActivity = this.activityService.create(tripId, userEmail, activityDTO);
    ActivityResponseDTO activityResponseDTO = new ActivityResponseDTO(createdActivity);

    CustomResponseBody<ActivityResponseDTO> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.CREATED);
    response.setMessage("Atividade criada com sucesso");
    response.setData(activityResponseDTO);
    return response;
  }

  @GetMapping("/{tripId}/activities")
  @ResponseStatus(HttpStatus.OK)
  public CustomResponseBody<ActivityResponsePageDTO> getAllTripActivities(
    @RequestHeader("userEmail") String userEmail,
    @PathVariable UUID tripId,
    @RequestParam(defaultValue = "0") int page
  ) {
    Page<Activity> allActivities = this.activityService.getAllTripActivities(tripId, userEmail, page);
    ActivityResponsePageDTO activityPageDTO = this.activityMapper.toActivityResponsePageDTO(allActivities);

    CustomResponseBody<ActivityResponsePageDTO> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.OK);
    response.setMessage("Todas as atividades da viagem de id: '" + tripId + "'");
    response.setData(activityPageDTO);
    return response;
  }

  @DeleteMapping("/{tripId}/activities")
  @ResponseStatus(HttpStatus.OK)
  public CustomResponseBody<String> deleteAllTripActivities(
    @RequestHeader("userEmail") String userEmail,
    @PathVariable UUID tripId
  ) {
    int quantityOfDeletedActivities = this.activityService.deleteAllTripActivities(tripId, userEmail);

    CustomResponseBody<String> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.OK);
    response.setMessage("Todas as atividades da viagem de id: '" + tripId + "' foram excluídas com sucesso");
    response.setData("Quantidade de atividades excluídas: " + quantityOfDeletedActivities);
    return response;
  }

  @GetMapping("/{tripId}/activities/{activityId}")
  @ResponseStatus(HttpStatus.OK)
  public CustomResponseBody<ActivityResponseDTO> getActivityById(
    @RequestHeader("userEmail") String userEmail,
    @PathVariable UUID tripId,
    @PathVariable UUID activityId
  ) {
    Activity activity = this.activityService.getById(tripId, activityId, userEmail);
    ActivityResponseDTO activityResponseDTO = new ActivityResponseDTO(activity);

    CustomResponseBody<ActivityResponseDTO> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.OK);
    response.setMessage("Atividade de id: '" + activityId + "' encontrada");
    response.setData(activityResponseDTO);
    return response;
  }

  @PatchMapping("/{tripId}/activities/{activityId}")
  @ResponseStatus(HttpStatus.OK)
  public CustomResponseBody<ActivityResponseDTO> updateActivity(
    @RequestHeader("userEmail") String userEmail,
    @PathVariable UUID tripId,
    @PathVariable UUID activityId,
    @RequestBody @Valid ActivityCreateOrUpdateDTO activityDTO
  ) {
    Activity updatedActivity = this.activityService.update(tripId, activityId, userEmail, activityDTO);
    ActivityResponseDTO activityResponseDTO = new ActivityResponseDTO(updatedActivity);

    CustomResponseBody<ActivityResponseDTO> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.OK);
    response.setMessage("Atividade atualizada com sucesso");
    response.setData(activityResponseDTO);
    return response;
  }

  @DeleteMapping("/{tripId}/activities/{activityId}")
  @ResponseStatus(HttpStatus.OK)
  public CustomResponseBody<Map<String, ActivityResponseDTO>> deleteActivity(
    @RequestHeader("userEmail") String userEmail,
    @PathVariable UUID tripId,
    @PathVariable UUID activityId
  ) {
    Activity deletedActivity = this.activityService.delete(tripId, activityId, userEmail);
    ActivityResponseDTO activityResponseDTO = new ActivityResponseDTO(deletedActivity);

    Map<String, ActivityResponseDTO> activityResponseMap = new HashMap<>(1);
    activityResponseMap.put("deletedActivity", activityResponseDTO);

    CustomResponseBody<Map<String, ActivityResponseDTO>> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.OK);
    response.setMessage("Atividade excluída com sucesso");
    response.setData(activityResponseMap);
    return response;
  }

  @PostMapping("/{tripId}/links")
  @ResponseStatus(HttpStatus.CREATED)
  public CustomResponseBody<LinkResponseDTO> createLink(
    @RequestHeader("userEmail") String userEmail,
    @PathVariable UUID tripId,
    @RequestBody @Valid LinkCreateDTO linkDTO
    ) {
    Link createdLink = this.linkService.create(tripId, userEmail, linkDTO);
    LinkResponseDTO linkResponseDTO = new LinkResponseDTO(createdLink);

    CustomResponseBody<LinkResponseDTO> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.CREATED);
    response.setMessage("Link criado com sucesso");
    response.setData(linkResponseDTO);
    return response;
  }

  @GetMapping("/{tripId}/links")
  @ResponseStatus(HttpStatus.OK)
  public CustomResponseBody<LinkResponsePageDTO> getAllTripLinks(
    @RequestHeader("userEmail") String userEmail,
    @PathVariable UUID tripId,
    @RequestParam(defaultValue = "0") int page
  ) {
    Page<Link> allLinks = this.linkService.getAllTripLinks(tripId, userEmail, page);
    LinkResponsePageDTO linkResponsePageDTO = this.linkMapper.toLinkResponsePageDTO(allLinks);

    CustomResponseBody<LinkResponsePageDTO> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.OK);
    response.setMessage("Todos os links da viagem de id: '" + tripId + "'");
    response.setData(linkResponsePageDTO);
    return response;
  }

  @DeleteMapping("/{tripId}/links")
  @ResponseStatus(HttpStatus.OK)
  public CustomResponseBody<String> deleteAllTripLinks(
    @RequestHeader("userEmail") String userEmail,
    @PathVariable UUID tripId
  ) {
    int quantityOfDeletedLinks = this.linkService.deleteAllTripLinks(tripId, userEmail);

    CustomResponseBody<String> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.OK);
    response.setMessage("Todos os links da viagem de id: '" + tripId + "' foram excluídos com sucesso");
    response.setData("Quantidade de links excluídos: " + quantityOfDeletedLinks);
    return response;
  }

  @GetMapping("/{tripId}/links/{linkId}")
  @ResponseStatus(HttpStatus.OK)
  public CustomResponseBody<LinkResponseDTO> getLinkById(
    @RequestHeader("userEmail") String userEmail,
    @PathVariable UUID tripId,
    @PathVariable UUID linkId
  ) {
    Link link = this.linkService.getById(tripId, linkId, userEmail);
    LinkResponseDTO linkResponseDTO = new LinkResponseDTO(link);

    CustomResponseBody<LinkResponseDTO> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.OK);
    response.setMessage("Link de id: '" + linkId + "' encontrado");
    response.setData(linkResponseDTO);
    return response;
  }

  @PatchMapping("/{tripId}/links/{linkId}")
  @ResponseStatus(HttpStatus.OK)
  public CustomResponseBody<LinkResponseDTO> updateLink(
    @RequestHeader("userEmail") String userEmail,
    @PathVariable UUID tripId,
    @PathVariable UUID linkId,
    @RequestBody @Valid LinkUpdateDTO linkDTO
  ) {
    Link updatedLink = this.linkService.update(tripId, linkId, userEmail, linkDTO);
    LinkResponseDTO linkResponseDTO = new LinkResponseDTO(updatedLink);

    CustomResponseBody<LinkResponseDTO> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.OK);
    response.setMessage("Link atualizado com sucesso");
    response.setData(linkResponseDTO);
    return response;
  }

  @DeleteMapping("/{tripId}/links/{linkId}")
  @ResponseStatus(HttpStatus.OK)
  public CustomResponseBody<Map<String, LinkResponseDTO>> deleteLink(
    @RequestHeader("userEmail") String userEmail,
    @PathVariable UUID tripId,
    @PathVariable UUID linkId
  ) {
    Link deletedLink = this.linkService.delete(tripId, linkId, userEmail);
    LinkResponseDTO linkResponseDTO = new LinkResponseDTO(deletedLink);

    Map<String, LinkResponseDTO> deletedLinkMap = new HashMap<>(1);
    deletedLinkMap.put("deletedLink", linkResponseDTO);

    CustomResponseBody<Map<String, LinkResponseDTO>> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.OK);
    response.setMessage("Link excluído com sucesso");
    response.setData(deletedLinkMap);
    return response;
  }
}

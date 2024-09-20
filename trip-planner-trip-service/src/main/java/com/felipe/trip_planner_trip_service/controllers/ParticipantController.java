package com.felipe.trip_planner_trip_service.controllers;

import com.felipe.trip_planner_trip_service.dtos.participant.AddParticipantDTO;
import com.felipe.trip_planner_trip_service.dtos.participant.AddParticipantResponseDTO;
import com.felipe.trip_planner_trip_service.dtos.participant.ParticipantResponseInfoDTO;
import com.felipe.trip_planner_trip_service.dtos.participant.ParticipantResponseTripInfoDTO;
import com.felipe.trip_planner_trip_service.models.Participant;
import com.felipe.trip_planner_trip_service.services.ParticipantService;
import com.felipe.trip_planner_trip_service.utils.response.CustomResponseBody;
import com.felipe.trip_planner_trip_service.utils.response.ResponseConditionStatus;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/participants")
public class ParticipantController {

  private final ParticipantService participantService;

  public ParticipantController(ParticipantService participantService) {
    this.participantService = participantService;
  }

  @PostMapping("/confirm")
  @ResponseStatus(HttpStatus.OK)
  public CustomResponseBody<AddParticipantResponseDTO> addParticipant(
    @RequestHeader("userEmail") String participantEmail,
    @RequestBody AddParticipantDTO participantDTO
  ) {
    Participant addedParticipant = this.participantService.addParticipant(participantDTO, participantEmail);
    ParticipantResponseInfoDTO participantInfoDTO = new ParticipantResponseInfoDTO(addedParticipant);
    ParticipantResponseTripInfoDTO participantTripInfoDTO = new ParticipantResponseTripInfoDTO(addedParticipant.getTrip());
    AddParticipantResponseDTO participantResponseDTO = new AddParticipantResponseDTO(participantInfoDTO, participantTripInfoDTO);

    CustomResponseBody<AddParticipantResponseDTO> response = new CustomResponseBody<>();
    response.setStatus(ResponseConditionStatus.SUCCESS);
    response.setCode(HttpStatus.OK);
    response.setMessage("Participante adicionado com sucesso");
    response.setData(participantResponseDTO);
    return response;
  }
}

package com.felipe.trip_planner_trip_service.dtos.participant.mapper;

import com.felipe.trip_planner_trip_service.dtos.participant.ParticipantResponseDTO;
import com.felipe.trip_planner_trip_service.dtos.participant.ParticipantResponsePageDTO;
import com.felipe.trip_planner_trip_service.models.Participant;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ParticipantMapper {
  public ParticipantResponsePageDTO toParticipantResponsePageDTO(Page<Participant> participants) {
    List<ParticipantResponseDTO> participantResponseDTOs = participants.getContent()
      .stream()
      .map(ParticipantResponseDTO::new)
      .toList();

    return new ParticipantResponsePageDTO(
      participantResponseDTOs,
      participants.getTotalElements(),
      participants.getTotalPages()
    );
  }
}

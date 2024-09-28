package com.felipe.trip_planner_trip_service.dtos.link.mapper;

import com.felipe.trip_planner_trip_service.dtos.link.LinkResponseDTO;
import com.felipe.trip_planner_trip_service.dtos.link.LinkResponsePageDTO;
import com.felipe.trip_planner_trip_service.models.Link;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LinkMapper {
  public LinkResponsePageDTO toLinkResponsePageDTO(Page<Link> links) {
    List<LinkResponseDTO> linkResponseDTOs = links.getContent().stream().map(LinkResponseDTO::new).toList();
    return new LinkResponsePageDTO(linkResponseDTOs, links.getTotalElements(), links.getTotalPages());
  }
}

package com.felipe.trip_planner_trip_service.dtos.activity.mapper;

import com.felipe.trip_planner_trip_service.dtos.activity.ActivityResponseDTO;
import com.felipe.trip_planner_trip_service.dtos.activity.ActivityResponsePageDTO;
import com.felipe.trip_planner_trip_service.models.Activity;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ActivityMapper {
  public ActivityResponsePageDTO toActivityResponsePageDTO(Page<Activity> activities) {
    List<ActivityResponseDTO> activityDTOs = activities.getContent().stream().map(ActivityResponseDTO::new).toList();
    return new ActivityResponsePageDTO(activityDTOs, activities.getTotalElements(), activities.getTotalPages());
  }
}

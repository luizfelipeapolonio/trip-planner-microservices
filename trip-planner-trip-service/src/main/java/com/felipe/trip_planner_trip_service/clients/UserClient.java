package com.felipe.trip_planner_trip_service.clients;

import com.felipe.trip_planner_trip_service.dtos.UserClientDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "TRIP-PLANNER-USER-SERVICE", path = "/api/users")
public interface UserClient {

  @GetMapping("/{email}")
  UserClientDTO getProfile(@PathVariable String email);
}

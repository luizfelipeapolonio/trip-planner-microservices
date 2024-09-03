package com.felipe.trip_planner_trip_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class TripPlannerTripServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(TripPlannerTripServiceApplication.class, args);
	}

}

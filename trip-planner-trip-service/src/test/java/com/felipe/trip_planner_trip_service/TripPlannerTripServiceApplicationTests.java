package com.felipe.trip_planner_trip_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles(value = "test")
@EnableAutoConfiguration(exclude = {KafkaAutoConfiguration.class})
class TripPlannerTripServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}

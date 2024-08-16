package com.felipe.trip_planner_user_service.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.felipe.trip_planner_user_service.dtos.UserRegisterDTO;
import com.felipe.trip_planner_user_service.dtos.UserResponseDTO;
import com.felipe.trip_planner_user_service.exceptions.UserAlreadyExistsException;
import com.felipe.trip_planner_user_service.models.User;
import com.felipe.trip_planner_user_service.services.UserService;
import com.felipe.trip_planner_user_service.utils.response.ResponseConditionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles(value = "test")
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

  @Autowired
  MockMvc mockMvc;

  @Autowired
  ObjectMapper objectMapper;

  @MockBean
  UserService userService;

  private User user;
  private final String BASE_URL = "/api/auth";

  @BeforeEach
  void setUp() {
    LocalDateTime mockDateTime = LocalDateTime.parse("2024-01-01T12:00:00.123456");

    User user = new User();
    user.setId(UUID.fromString("62dac895-a1f0-4140-b52b-4c12cb82c6ff"));
    user.setName("User 1");
    user.setEmail("user1@email.com");
    user.setPassword("Encoded password");
    user.setCreatedAt(mockDateTime);
    user.setUpdatedAt(mockDateTime);

    this.user = user;
  }

  @Test
  @DisplayName("register - Should return a success response with created status code and the created user")
  void registerUserSuccess() throws Exception {
    UserRegisterDTO userRegisterDTO = new UserRegisterDTO("User 1", "user1@email.com", "password");
    UserResponseDTO userResponseDTO = new UserResponseDTO(this.user);
    String jsonBody = this.objectMapper.writeValueAsString(userRegisterDTO);

    when(this.userService.register(userRegisterDTO)).thenReturn(this.user);

    this.mockMvc.perform(post(BASE_URL + "/register")
      .contentType(MediaType.APPLICATION_JSON).content(jsonBody)
      .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.SUCCESS.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.CREATED.value()))
      .andExpect(jsonPath("$.message").value("Usuário criado com sucesso"))
      .andExpect(jsonPath("$.data.id").value(userResponseDTO.id()))
      .andExpect(jsonPath("$.data.name").value(userResponseDTO.name()))
      .andExpect(jsonPath("$.data.email").value(userResponseDTO.email()))
      .andExpect(jsonPath("$.data.password").doesNotExist())
      .andExpect(jsonPath("$.data.createdAt").value(userResponseDTO.createdAt().toString()))
      .andExpect(jsonPath("$.data.updatedAt").value(userResponseDTO.updatedAt().toString()));

    verify(this.userService, times(1)).register(userRegisterDTO);
  }

  @Test
  @DisplayName("register - Should return an error response with conflict status code")
  void registerUserFailsByExistingUser() throws Exception {
    UserRegisterDTO userRegisterDTO = new UserRegisterDTO("User 1", "user1@email.com", "password");
    String jsonBody = this.objectMapper.writeValueAsString(userRegisterDTO);

    when(this.userService.register(userRegisterDTO))
      .thenThrow(new UserAlreadyExistsException(userRegisterDTO.email()));

    this.mockMvc.perform(post(BASE_URL + "/register")
      .contentType(MediaType.APPLICATION_JSON).content(jsonBody)
      .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isConflict())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.ERROR.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.CONFLICT.value()))
      .andExpect(jsonPath("$.message").value("Usuário de e-mail 'user1@email.com' já cadastrado"))
      .andExpect(jsonPath("$.data").doesNotExist());

    verify(this.userService, times(1)).register(userRegisterDTO);
  }
}

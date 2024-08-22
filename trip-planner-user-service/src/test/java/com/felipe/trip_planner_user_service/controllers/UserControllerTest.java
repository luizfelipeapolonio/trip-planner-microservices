package com.felipe.trip_planner_user_service.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.felipe.trip_planner_user_service.dtos.UserUpdateDTO;
import com.felipe.trip_planner_user_service.exceptions.RecordNotFoundException;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles(value = "test")
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

  @Autowired
  MockMvc mockMvc;

  @Autowired
  ObjectMapper objectMapper;

  @MockBean
  UserService userService;

  private User user;
  private final String BASE_URL = "/api/users";

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
  @DisplayName("getAuthenticatedUser - Should return a success response with ok status code and the authenticated user")
  void getAuthenticatedUserProfileSuccess() throws Exception {
    when(this.userService.getAuthenticatedUserProfile()).thenReturn(this.user);

    this.mockMvc.perform(get(BASE_URL + "/me").accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.SUCCESS.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.OK.value()))
      .andExpect(jsonPath("$.message").value("Usuário autenticado"))
      .andExpect(jsonPath("$.data.id").value(this.user.getId().toString()))
      .andExpect(jsonPath("$.data.name").value(this.user.getName()))
      .andExpect(jsonPath("$.data.email").value(this.user.getEmail()))
      .andExpect(jsonPath("$.data.password").doesNotExist())
      .andExpect(jsonPath("$.data.createdAt").value(this.user.getCreatedAt().toString()))
      .andExpect(jsonPath("$.data.updatedAt").value(this.user.getUpdatedAt().toString()));

    verify(this.userService, times(1)).getAuthenticatedUserProfile();
  }

  @Test
  @DisplayName("update - Should return a success response with ok status code and the updated user")
  void updateSuccess() throws Exception {
    UserUpdateDTO updateDTO = new UserUpdateDTO("Updated name", "updated password");
    String jsonBody = this.objectMapper.writeValueAsString(updateDTO);
    UUID userId = this.user.getId();
    this.user.setName(updateDTO.name());

    when(this.userService.update(userId, updateDTO)).thenReturn(this.user);

    this.mockMvc.perform(put(BASE_URL + "/62dac895-a1f0-4140-b52b-4c12cb82c6ff")
      .contentType(MediaType.APPLICATION_JSON).content(jsonBody)
      .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.SUCCESS.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.OK.value()))
      .andExpect(jsonPath("$.message").value("Usuário atualizado com sucesso"))
      .andExpect(jsonPath("$.data.id").value(this.user.getId().toString()))
      .andExpect(jsonPath("$.data.name").value(updateDTO.name()))
      .andExpect(jsonPath("$.data.email").value(this.user.getEmail()))
      .andExpect(jsonPath("$.data.password").doesNotExist())
      .andExpect(jsonPath("$.data.createdAt").value(this.user.getCreatedAt().toString()))
      .andExpect(jsonPath("$.data.updatedAt").value(this.user.getUpdatedAt().toString()));

    verify(this.userService, times(1)).update(userId, updateDTO);
  }

  @Test
  @DisplayName("update - Should return an error response with forbidden status code")
  void updateFailsByAccessDenied() throws Exception {
    UserUpdateDTO updateDTO = new UserUpdateDTO("Updated name", "updated password");
    String jsonBody = this.objectMapper.writeValueAsString(updateDTO);
    UUID userId = this.user.getId();

    when(this.userService.update(userId, updateDTO))
      .thenThrow(new AccessDeniedException("Acesso negado: Você não tem permissão para alterar este recurso"));

    this.mockMvc.perform(put(BASE_URL + "/62dac895-a1f0-4140-b52b-4c12cb82c6ff")
      .contentType(MediaType.APPLICATION_JSON).content(jsonBody)
      .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isForbidden())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.ERROR.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.FORBIDDEN.value()))
      .andExpect(jsonPath("$.message").value("Acesso negado: Você não tem permissão para alterar este recurso"))
      .andExpect(jsonPath("$.data").doesNotExist());

    verify(this.userService, times(1)).update(userId, updateDTO);
  }

  @Test
  @DisplayName("update - Should return an error response with not found status code")
  void updateFailsByUserNotFound() throws Exception {
    UserUpdateDTO updateDTO = new UserUpdateDTO("Updated name", "updated password");
    String jsonBody = this.objectMapper.writeValueAsString(updateDTO);
    UUID userId = this.user.getId();

    when(this.userService.update(userId, updateDTO))
      .thenThrow(new RecordNotFoundException("Usuário de id: '" + userId + "' não encontrado"));

    this.mockMvc.perform(put(BASE_URL + "/62dac895-a1f0-4140-b52b-4c12cb82c6ff")
      .contentType(MediaType.APPLICATION_JSON).content(jsonBody)
      .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.ERROR.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.NOT_FOUND.value()))
      .andExpect(jsonPath("$.message").value("Usuário de id: '" + userId + "' não encontrado"))
      .andExpect(jsonPath("$.data").doesNotExist());

    verify(this.userService, times(1)).update(userId, updateDTO);
  }

  @Test
  @DisplayName("deleteAuthenticatedUserProfile - Should return a success response with ok status code and the deleted user")
  void deleteAuthenticatedUserProfileSuccess() throws Exception {
    when(this.userService.deleteAuthenticatedUserProfile()).thenReturn(this.user);

    this.mockMvc.perform(delete(BASE_URL + "/me").accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.SUCCESS.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.OK.value()))
      .andExpect(jsonPath("$.message").value("Usuário excluído com sucesso"))
      .andExpect(jsonPath("$.data.deletedUser.id").value(this.user.getId().toString()))
      .andExpect(jsonPath("$.data.deletedUser.name").value(this.user.getName()))
      .andExpect(jsonPath("$.data.deletedUser.email").value(this.user.getEmail()))
      .andExpect(jsonPath("$.data.deletedUser.password").doesNotExist())
      .andExpect(jsonPath("$.data.deletedUser.createdAt").value(this.user.getCreatedAt().toString()))
      .andExpect(jsonPath("$.data.deletedUser.updatedAt").value(this.user.getUpdatedAt().toString()));

    verify(this.userService, times(1)).deleteAuthenticatedUserProfile();
  }

  @Test
  @DisplayName("getProfile - Should return a success response with ok status code and the user profile")
  void getProfileSuccess() throws Exception {
    UUID userId = this.user.getId();

    when(this.userService.getProfile(userId)).thenReturn(this.user);

    this.mockMvc.perform(get(BASE_URL + "/62dac895-a1f0-4140-b52b-4c12cb82c6ff")
      .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.id").value(this.user.getId().toString()))
      .andExpect(jsonPath("$.name").value(this.user.getName()))
      .andExpect(jsonPath("$.email").value(this.user.getEmail()))
      .andExpect(jsonPath("$.password").doesNotExist())
      .andExpect(jsonPath("$.createdAt").value(this.user.getCreatedAt().toString()))
      .andExpect(jsonPath("$.updatedAt").value(this.user.getUpdatedAt().toString()));

    verify(this.userService, times(1)).getProfile(userId);
  }

  @Test
  @DisplayName("getProfile - Should return an error response with not found status code")
  void getProfileFailsByUserNotFound() throws Exception {
    UUID userId = this.user.getId();

    when(this.userService.getProfile(userId))
      .thenThrow(new RecordNotFoundException("Usuário de id: '" + userId + "' não encontrado"));

    this.mockMvc.perform(get(BASE_URL + "/62dac895-a1f0-4140-b52b-4c12cb82c6ff")
      .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.status").value(ResponseConditionStatus.ERROR.getValue()))
      .andExpect(jsonPath("$.code").value(HttpStatus.NOT_FOUND.value()))
      .andExpect(jsonPath("$.message").value("Usuário de id: '62dac895-a1f0-4140-b52b-4c12cb82c6ff' não encontrado"))
      .andExpect(jsonPath("$.data").doesNotExist());

    verify(this.userService, times(1)).getProfile(userId);
  }
}

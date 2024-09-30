package com.felipe.trip_planner_trip_service.dtos.link;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

public record LinkCreateDTO(
  @NotNull(message = "O título do link é obrigatório")
  @NotBlank(message = "O título do link não deve estar em branco")
  @Length(max = 255, message = "O título do link deve ter no máximo 255 caracteres")
  String title,

  @NotNull(message = "A url é obrigatória")
  @NotBlank(message = "A url não deve estar em branco")
  @Length(max = 255, message = "A url deve ter no máximo 255 caracteres ")
  String url
) {}

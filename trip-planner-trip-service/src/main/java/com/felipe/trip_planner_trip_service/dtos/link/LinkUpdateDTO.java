package com.felipe.trip_planner_trip_service.dtos.link;

import jakarta.annotation.Nullable;
import org.hibernate.validator.constraints.Length;

public record LinkUpdateDTO(
  @Nullable
  @Length(max = 255, message = "O título do link deve ter no máximo 255 caracteres")
  String title,

  @Nullable
  @Length(max = 255, message = "A url deve ter no máximo 255 caracteres")
  String url
) {}

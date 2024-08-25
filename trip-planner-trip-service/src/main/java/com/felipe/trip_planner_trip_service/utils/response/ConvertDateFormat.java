package com.felipe.trip_planner_trip_service.utils.response;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ConvertDateFormat {
  private static final String OUTPUT_FORMAT_DATE = "dd-MM-yyyy";

  public static String convertDateToFormattedString(LocalDate date) {
    return date.format(DateTimeFormatter.ofPattern(OUTPUT_FORMAT_DATE));
  }
}

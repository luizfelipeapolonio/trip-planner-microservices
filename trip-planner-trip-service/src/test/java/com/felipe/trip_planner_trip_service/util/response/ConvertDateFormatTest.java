package com.felipe.trip_planner_trip_service.util.response;

import com.felipe.trip_planner_trip_service.utils.response.ConvertDateFormat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

public class ConvertDateFormatTest {

  @Test
  @DisplayName("convertDateToFormattedString - Should successfully convert a LocalDate object to a String in the format \"dd-MM-yyyy\"")
  void convertDateToFormattedStringSuccess() {
    DateTimeFormatter inputFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    LocalDate date = LocalDate.parse("2024-01-01", inputFormat);

    String convertedDate = ConvertDateFormat.convertDateToFormattedString(date);

    assertThat(convertedDate).isEqualTo("01-01-2024");
  }
}

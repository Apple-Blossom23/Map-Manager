package com.workshop.dto.checkin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckinStatusResponse {
    private Boolean checkedInToday;
    private LocalDate lastCheckinDate;
    private Boolean canCheckin;
}

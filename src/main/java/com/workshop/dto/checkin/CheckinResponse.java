package com.workshop.dto.checkin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckinResponse {
    private Integer drops;
    private Integer totalDrops;
    private Boolean checkedInToday;
    private LocalDate lastCheckinDate;
}

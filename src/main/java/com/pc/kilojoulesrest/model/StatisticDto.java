package com.pc.kilojoulesrest.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@RequiredArgsConstructor
public class StatisticDto {

    LocalDate startDate;
    LocalDate endDate;

    JournalTotalsDTO totalAndAverageValues;
    List<List<TopTenDTO>> simpleBreakdown;
    List<List<TopTenDTO>> advancedStatistics;

}

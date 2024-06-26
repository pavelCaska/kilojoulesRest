package com.pc.kilojoulesrest.model;


import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@RequiredArgsConstructor
public class JournalEntryWithTotalsDto {

    private LocalDate date;

    private BigDecimal totalQuantity = BigDecimal.ZERO;
    private BigDecimal totalKiloJoules = BigDecimal.ZERO;
    private BigDecimal totalProteins = BigDecimal.ZERO;
    private BigDecimal totalCarbohydrates = BigDecimal.ZERO;
    private BigDecimal totalFiber = BigDecimal.ZERO;
    private BigDecimal totalFat = BigDecimal.ZERO;

    List<JournalEntryDTO> entries;

}

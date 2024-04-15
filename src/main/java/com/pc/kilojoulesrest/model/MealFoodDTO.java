package com.pc.kilojoulesrest.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.pc.kilojoulesrest.util.DecimalJsonSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class MealFoodDTO {
    private Long id;
    private Long foodId;
    private String foodName;

    @JsonSerialize(using = DecimalJsonSerializer.class)
    private BigDecimal quantity;
    @JsonSerialize(using = DecimalJsonSerializer.class)
    private BigDecimal adjustedKiloJoules;
    @JsonSerialize(using = DecimalJsonSerializer.class)
    private BigDecimal adjustedProteins;
    @JsonSerialize(using = DecimalJsonSerializer.class)
    private BigDecimal adjustedCarbohydrates;
    @JsonSerialize(using = DecimalJsonSerializer.class)
    private BigDecimal adjustedFiber;
    @JsonSerialize(using = DecimalJsonSerializer.class)
    private BigDecimal adjustedFat;
}
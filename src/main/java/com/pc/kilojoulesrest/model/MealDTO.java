package com.pc.kilojoulesrest.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.pc.kilojoulesrest.util.DecimalJsonSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class MealDTO {
    private String mealName;
    private Long mealId;
    private List<MealFoodDTO> foods;

    @JsonSerialize(using = DecimalJsonSerializer.class)
    private BigDecimal sumQuantity;
    @JsonSerialize(using = DecimalJsonSerializer.class)
    private BigDecimal sumAdjustedKiloJoules;
    @JsonSerialize(using = DecimalJsonSerializer.class)
    private BigDecimal sumAdjustedProteins;
    @JsonSerialize(using = DecimalJsonSerializer.class)
    private BigDecimal sumAdjustedCarbohydrates;
    @JsonSerialize(using = DecimalJsonSerializer.class)
    private BigDecimal sumAdjustedFiber;
    @JsonSerialize(using = DecimalJsonSerializer.class)
    private BigDecimal sumAdjustedFat;

}
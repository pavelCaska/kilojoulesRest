package com.pc.kilojoulesrest.model;

import com.pc.kilojoulesrest.entity.Food;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Value;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DTO for {@link Food}
 */
@Value
public class FoodCreateDto implements Serializable {
    @Size(max = 255)
    @NotBlank(message = "Name cannot be empty.")
    String name;
//    @PositiveOrZero
    BigDecimal quantity;
    @NotNull
    @PositiveOrZero
    BigDecimal kiloJoules;
    @NotNull
    @PositiveOrZero
    BigDecimal proteins;
    @NotNull
    @PositiveOrZero
    BigDecimal carbohydrates;
    @PositiveOrZero
    BigDecimal fiber;
    @PositiveOrZero
    BigDecimal sugar;
    @NotNull
    @PositiveOrZero
    BigDecimal fat;
    @PositiveOrZero
    BigDecimal safa;
    @PositiveOrZero
    BigDecimal tfa;
    @PositiveOrZero
    BigDecimal cholesterol;
    @PositiveOrZero
    BigDecimal sodium;
    @PositiveOrZero
    BigDecimal calcium;
    @PositiveOrZero
    BigDecimal phe;
}
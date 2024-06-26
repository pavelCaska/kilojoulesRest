package com.pc.kilojoulesrest.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.pc.kilojoulesrest.entity.Meal;
import com.pc.kilojoulesrest.util.DecimalJsonSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    public static MealDTO fromEntity(Meal meal) {
        if (meal == null) return null;

        MealDTO mealDTO = new MealDTO();
        mealDTO.setMealId(meal.getId());
        mealDTO.setMealName(meal.getMealName());

        List<MealFoodDTO> mealFoodsDTO = calculateAndReturnAdjustedMealFoods(meal);
        mealDTO.setFoods(mealFoodsDTO);

        sumUpMealFoods(mealDTO, mealFoodsDTO);

        return mealDTO;
    }

    private static List<MealFoodDTO> calculateAndReturnAdjustedMealFoods(Meal meal) {
        return meal.getMealFoods().stream().map(mealFood -> {
            MealFoodDTO dto = new MealFoodDTO();
            dto.setId(mealFood.getId());
            dto.setFoodId(mealFood.getFood().getId());
            dto.setFoodName(mealFood.getFood().getName());

            BigDecimal quantity = mealFood.getQuantity();
            BigDecimal divisor = mealFood.getFood().getQuantity();

            dto.setQuantity(quantity);
            dto.setAdjustedKiloJoules(mealFood.getFood().getKiloJoules().multiply(quantity).divide(divisor, RoundingMode.HALF_UP));
            dto.setAdjustedProteins(mealFood.getFood().getProteins().multiply(quantity).divide(divisor, RoundingMode.HALF_UP));
            dto.setAdjustedCarbohydrates(mealFood.getFood().getCarbohydrates().multiply(quantity).divide(divisor, RoundingMode.HALF_UP));
            dto.setAdjustedFiber(mealFood.getFood().getFiber().multiply(quantity).divide(divisor, RoundingMode.HALF_UP));
            dto.setAdjustedFat(mealFood.getFood().getFat().multiply(quantity).divide(divisor, RoundingMode.HALF_UP));

            return dto;
        }).collect(Collectors.toList());
    }
    private static void sumUpMealFoods(MealDTO mealDTO, List<MealFoodDTO> mealFoodsDTO) {
        mealDTO.setSumQuantity(mealFoodsDTO.stream().map(MealFoodDTO::getQuantity).reduce(BigDecimal.ZERO,BigDecimal::add));
        mealDTO.setSumAdjustedKiloJoules(mealFoodsDTO.stream().map(MealFoodDTO::getAdjustedKiloJoules).reduce(BigDecimal.ZERO,BigDecimal::add));
        mealDTO.setSumAdjustedProteins(mealFoodsDTO.stream().map(MealFoodDTO::getAdjustedProteins).reduce(BigDecimal.ZERO,BigDecimal::add));
        mealDTO.setSumAdjustedCarbohydrates(mealFoodsDTO.stream().map(MealFoodDTO::getAdjustedCarbohydrates).reduce(BigDecimal.ZERO,BigDecimal::add));
        mealDTO.setSumAdjustedFiber(mealFoodsDTO.stream().map(MealFoodDTO::getAdjustedFiber).reduce(BigDecimal.ZERO,BigDecimal::add));
        mealDTO.setSumAdjustedFat(mealFoodsDTO.stream().map(MealFoodDTO::getAdjustedFat).reduce(BigDecimal.ZERO,BigDecimal::add));
    }

}
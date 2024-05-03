package com.pc.kilojoulesrest.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.pc.kilojoulesrest.entity.Food;
import com.pc.kilojoulesrest.util.DecimalJsonSerializer;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.pc.kilojoulesrest.constant.Constant.ONE_HUNDRED;

/**
 * DTO for {@link Food}
 */
@Data
@RequiredArgsConstructor
public class FoodDto {
    Long id;

    @Size(max = 255)
    @NotBlank(message = "Name cannot be empty.")
    String name;

    @NotNull
    @JsonSerialize(using = DecimalJsonSerializer.class)
    BigDecimal quantity = ONE_HUNDRED;

    @NotNull
    @JsonSerialize(using = DecimalJsonSerializer.class)
    BigDecimal kiloJoules;

    @NotNull
    @JsonSerialize(using = DecimalJsonSerializer.class)
    BigDecimal proteins;

    @NotNull
    @JsonSerialize(using = DecimalJsonSerializer.class)
    BigDecimal carbohydrates;

    @NotNull
    @JsonSerialize(using = DecimalJsonSerializer.class)
    BigDecimal fiber;

    @NotNull
    @JsonSerialize(using = DecimalJsonSerializer.class)
    BigDecimal sugar;

    @NotNull
    @JsonSerialize(using = DecimalJsonSerializer.class)
    BigDecimal fat;

    @NotNull
    @JsonSerialize(using = DecimalJsonSerializer.class)
    BigDecimal safa;

    @NotNull
    @JsonSerialize(using = DecimalJsonSerializer.class)
    BigDecimal tfa;

    @NotNull
    @JsonSerialize(using = DecimalJsonSerializer.class)
    BigDecimal cholesterol;

    @NotNull
    @JsonSerialize(using = DecimalJsonSerializer.class)
    BigDecimal sodium;

    @NotNull
    @JsonSerialize(using = DecimalJsonSerializer.class)
    BigDecimal calcium;

    @NotNull
    @JsonSerialize(using = DecimalJsonSerializer.class)
    BigDecimal phe;

    @JsonFormat(pattern = "yyyy-MM-dd")
    Date createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd")
    Date updatedAt;

    private List<PortionResponseDTO> portions = new ArrayList<>();

    public static FoodDto fromEntity(Food food) {
        if (food == null) {
            return null;
        }

        FoodDto foodDto = new FoodDto();
        foodDto.setId(food.getId());
        foodDto.setName(food.getName());
        foodDto.setQuantity(food.getQuantity());
        foodDto.setKiloJoules(food.getKiloJoules());
        foodDto.setProteins(food.getProteins());
        foodDto.setCarbohydrates(food.getCarbohydrates());
        foodDto.setFiber(food.getFiber());
        foodDto.setSugar(food.getSugar());
        foodDto.setFat(food.getFat());
        foodDto.setSafa(food.getSafa());
        foodDto.setTfa(food.getTfa());
        foodDto.setCholesterol(food.getCholesterol());
        foodDto.setSodium(food.getSodium());
        foodDto.setCalcium(food.getCalcium());
        foodDto.setPhe(food.getPhe());
        foodDto.setCreatedAt(food.getCreatedAt());
        foodDto.setUpdatedAt(food.getUpdatedAt());

        List<PortionResponseDTO> portionDtos = food.getPortions().stream().map(PortionResponseDTO::fromEntity).collect(Collectors.toList());
        foodDto.setPortions(portionDtos);

        return foodDto;
    }
}
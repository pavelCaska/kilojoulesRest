package com.pc.kilojoulesrest.model;

import com.pc.kilojoulesrest.entity.JournalMealFood;
import com.pc.kilojoulesrest.entity.JournalMealFoodPortion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO for {@link JournalMealFood}
 */
@Data
@RequiredArgsConstructor
public class JournalMealFoodDto implements Serializable {
    Long id;
    String name;
    BigDecimal quantity;
    BigDecimal kiloJoules;
    BigDecimal proteins;
    BigDecimal carbohydrates;
    BigDecimal fiber;
    BigDecimal sugar;
    BigDecimal fat;
    BigDecimal safa;
    BigDecimal tfa;
    BigDecimal cholesterol;
    BigDecimal sodium;
    BigDecimal calcium;
    BigDecimal phe;
    List<JournalMealFoodPortionDto> portions;

    /**
     * DTO for {@link JournalMealFoodPortion}
     */
    @Data
    @RequiredArgsConstructor
    public static class JournalMealFoodPortionDto implements Serializable {
        Long id;
        String portionName;
        BigDecimal portionSize;
    }

    public static JournalMealFoodDto fromEntity(JournalMealFood journalMealFood) {
        if (journalMealFood == null) return null;

        JournalMealFoodDto dto = new JournalMealFoodDto();
        dto.setId(journalMealFood.getId());
        dto.setName(journalMealFood.getName());
        dto.setQuantity(journalMealFood.getQuantity());
        dto.setKiloJoules(journalMealFood.getKiloJoules());
        dto.setProteins(journalMealFood.getProteins());
        dto.setCarbohydrates(journalMealFood.getCarbohydrates());
        dto.setFiber(journalMealFood.getFiber());
        dto.setSugar(journalMealFood.getSugar());
        dto.setFat(journalMealFood.getFat());
        dto.setSafa(journalMealFood.getSafa());
        dto.setTfa(journalMealFood.getTfa());
        dto.setCholesterol(journalMealFood.getCholesterol());
        dto.setSodium(journalMealFood.getSodium());
        dto.setCalcium(journalMealFood.getCalcium());
        dto.setPhe(journalMealFood.getPhe());
        dto.setPortions(journalMealFood.getPortions().stream()
                .map(portion -> {
                    JournalMealFoodDto.JournalMealFoodPortionDto portionDto = new JournalMealFoodDto.JournalMealFoodPortionDto();
                    portionDto.setId(portion.getId());
                    portionDto.setPortionName(portion.getPortionName());
                    portionDto.setPortionSize(portion.getPortionSize());
                    return portionDto;
                })
                .collect(Collectors.toList()));
        return dto;
    }
}
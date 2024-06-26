package com.pc.kilojoulesrest.model;

import com.pc.kilojoulesrest.entity.Journal;
import com.pc.kilojoulesrest.entity.JournalMeal;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@RequiredArgsConstructor
public class JournalEntryMealDto {

    private Long id;
    private String consumedAt;
    private String mealType;

    private Long journalMealId;
    private String name;
    private BigDecimal quantity;
    private BigDecimal kiloJoules;
    private BigDecimal proteins;
    private BigDecimal carbohydrates;
    private BigDecimal fiber;
    private BigDecimal sugar;
    private BigDecimal fat;
    private BigDecimal safa;
    private BigDecimal tfa;
    private BigDecimal cholesterol;
    private BigDecimal sodium;
    private BigDecimal calcium;
    private BigDecimal phe;

    /**
     * foods represents the set of foods that a meal entry in the journal consists of
     * foods is also the name of json item
     */
    private Set<JournalMealFoodDto> foods = new HashSet<>();

    public static JournalEntryMealDto fromEntity(Journal journal, JournalMeal jm) {
        if(journal == null) return null;
        
        JournalEntryMealDto dto = new JournalEntryMealDto();
        dto.setJournalMealId(jm.getId());
        dto.setName(jm.getMealName());
        dto.setQuantity(jm.getQuantity());
        dto.setKiloJoules(jm.getKiloJoules());
        dto.setProteins(jm.getProteins());
        dto.setCarbohydrates(jm.getCarbohydrates());
        dto.setFiber(jm.getFiber());
        dto.setSugar(jm.getSugar());
        dto.setFat(jm.getFat());
        dto.setSafa(jm.getSafa());
        dto.setTfa(jm.getTfa());
        dto.setCholesterol(jm.getCholesterol());
        dto.setSodium(jm.getSodium());
        dto.setCalcium(jm.getCalcium());
        dto.setPhe(jm.getPhe());

        Set<JournalMealFoodDto> journalMealFoodDtoSet = new HashSet<>();
        journalMealFoodDtoSet = jm.getJournalMealFoods().stream().map(JournalMealFoodDto::fromEntity).collect(Collectors.toSet());
        dto.setFoods(journalMealFoodDtoSet);
        
        dto.setId(journal.getId());
        dto.setConsumedAt(journal.getConsumedAt().toString());
        dto.setMealType(journal.getMealType().toString());
        return dto;
        
    }
}

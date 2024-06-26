package com.pc.kilojoulesrest.model;

import com.pc.kilojoulesrest.entity.Journal;
import com.pc.kilojoulesrest.entity.JournalFood;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@RequiredArgsConstructor
public class JournalEntryFoodDto {

    private Long id;
    private String consumedAt;
    private String mealType;

    private Long journalFoodId;
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

    private List<PortionResponseDTO> portions = new ArrayList<>();

    public static JournalEntryFoodDto fromEntity(Journal journal, JournalFood jf) {
        if (journal == null) return null;

        JournalEntryFoodDto dto = new JournalEntryFoodDto();
        dto.setJournalFoodId(jf.getId());
        dto.setName(jf.getName());
        dto.setQuantity(jf.getQuantity());
        dto.setKiloJoules(jf.getKiloJoules());
        dto.setProteins(jf.getProteins());
        dto.setCarbohydrates(jf.getCarbohydrates());
        dto.setFiber(jf.getFiber());
        dto.setSugar(jf.getSugar());
        dto.setFat(jf.getFat());
        dto.setSafa(jf.getSafa());
        dto.setTfa(jf.getTfa());
        dto.setCholesterol(jf.getCholesterol());
        dto.setSodium(jf.getSodium());
        dto.setCalcium(jf.getCalcium());
        dto.setPhe(jf.getPhe());
        dto.setPortions(jf.getPortions().stream().map(PortionResponseDTO::fromEntity).collect(Collectors.toList()));

        dto.setId(journal.getId());
        dto.setConsumedAt(journal.getConsumedAt().toString());
        dto.setMealType(journal.getMealType().toString());

        return dto;

    }

}

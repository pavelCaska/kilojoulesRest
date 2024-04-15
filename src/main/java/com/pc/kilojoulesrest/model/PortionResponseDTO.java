package com.pc.kilojoulesrest.model;

import com.pc.kilojoulesrest.entity.Portion;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class PortionResponseDTO {

    private Long id;
    private Long foodId;
    private String portionName;
    private BigDecimal portionSize;


    public static PortionResponseDTO fromEntity(Portion portion) {
        if (portion == null) {
            return null;
        }

        PortionResponseDTO dto = new PortionResponseDTO();
        dto.setId(portion.getId());
        dto.setFoodId(portion.getFood().getId());
        dto.setPortionName(portion.getPortionName());
        dto.setPortionSize(portion.getPortionSize());

        return dto;
    }
}

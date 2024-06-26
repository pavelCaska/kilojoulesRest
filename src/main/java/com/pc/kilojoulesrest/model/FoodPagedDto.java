package com.pc.kilojoulesrest.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FoodPagedDto {
    private int pageNumber;
    private int totalPages;
    private List<FoodDto> foodDtoList;

}

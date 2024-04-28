package com.pc.kilojoulesrest.service;

import com.pc.kilojoulesrest.entity.Food;
import com.pc.kilojoulesrest.model.FoodCreateDto;
import com.pc.kilojoulesrest.model.FoodDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.BindingResult;

import java.util.List;
import java.util.Map;

public interface FoodService {

    List<Food> fetchAllFoods();

    Page<Food> getFoodsByPage(int page);

    Food getFoodById(Long id);

    FoodDto fetchFoodDtoById(Long id);

    FoodDto convertFoodToFoodDto(Food food);

    Food createFoodFromDto(FoodCreateDto dto);

    Map<String, String> buildErrorResponseForFood(BindingResult bindingResult);

    void saveFood(Food food);

    Food updateFood(FoodDto foodDto);

    Food addPortionsToFood(Food food);

    Food deleteFoodById(Long id);

    Page<Food> searchFood(String query, Pageable pageable);

}

package com.pc.kilojoulesrest.service;

import com.pc.kilojoulesrest.entity.MealFood;
import com.pc.kilojoulesrest.entity.User;
import com.pc.kilojoulesrest.model.MealFormDTO;
import jakarta.transaction.Transactional;

public interface MealFoodService {

    MealFood save(MealFood mealFood);

    void deleteMealFood(MealFood mealFood);

    MealFood getMealFoodById(Long id);

    @Transactional
    MealFood deleteMealFoodById(Long id, User user);

    boolean existsMealFoodByMealIdAndId(Long mealId, Long foodId);

//    MealFood updateMealFood(Long mealId, Long foodId, MealFormDTO mealFormDTO);

    @Transactional
    MealFood updateMealFood(Long mealFoodId, MealFormDTO mealFormDTO, User user);
}

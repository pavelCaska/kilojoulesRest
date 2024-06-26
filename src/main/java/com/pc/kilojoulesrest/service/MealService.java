package com.pc.kilojoulesrest.service;

import com.pc.kilojoulesrest.entity.Meal;
import com.pc.kilojoulesrest.entity.User;
import com.pc.kilojoulesrest.model.MealDTO;
import com.pc.kilojoulesrest.model.MealFoodDTO;
import com.pc.kilojoulesrest.model.MealFormDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.BindingResult;

import java.util.List;
import java.util.Map;

public interface MealService {
    Meal saveMeal(Meal meal);

    Page<Meal> fetchMealsPaged(int page, User user);

    Meal getMealById(Long id);

    Meal getMealByIdAndUser(Long id, User user);

    Meal createMeal(User user, MealFormDTO mealFormDTO, List<Long> foods);

    Meal addFoodToMeal(User user, Long id, MealFormDTO mealFormDTO, List<Long> foods);

    Meal updateMealName(Long id, String mealName, User user);

    List<MealDTO> calculateAndReturnMealDtoList(List<Meal> meals);

    void sumUpMealFoods(MealDTO mealDTO, List<MealFoodDTO> mealFoodsDTO);

    MealDTO calculateAndReturnMealDto(Meal meal);

    List<MealFoodDTO> calculateAndReturnAdjustedMealFoods(Meal meal);

    Meal deleteMealById(Long id, User user);

    Map buildErrorResponseForMeal(BindingResult bindingResult);

    Page<Meal> searchMeal(User user, String query, Pageable pageable);

    boolean existsMealByIdAndUser(Long mealId, User user);
}

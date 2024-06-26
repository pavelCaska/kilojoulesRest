package com.pc.kilojoulesrest.service;

import com.pc.kilojoulesrest.entity.Food;
import com.pc.kilojoulesrest.entity.JournalMeal;
import com.pc.kilojoulesrest.entity.JournalMealFood;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

public interface JournalMealFoodService {
    JournalMealFood saveJournalMealFood(JournalMealFood journalMealFood);

    JournalMealFood createJournalMealFoodFromFood(JournalMeal jm, Food food, BigDecimal quantity);

    JournalMealFood  updateJournalMealFood(Long id, BigDecimal quantity, BigDecimal portionSize, String foodName);

    @Transactional
    void deleteJmfByIdAndMealId(Long id, Long mealId);
}

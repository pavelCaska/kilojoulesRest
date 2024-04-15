package com.pc.kilojoulesrest.repository;

import com.pc.kilojoulesrest.entity.MealFood;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MealFoodRepository extends JpaRepository<MealFood, Long> {
    Optional<MealFood> findMealFoodByMealIdAndId(Long mealId, Long id);
}
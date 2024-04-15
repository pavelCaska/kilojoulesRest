package com.pc.kilojoulesrest.repository;

import com.pc.kilojoulesrest.entity.JournalMeal;
import com.pc.kilojoulesrest.entity.JournalMealFood;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JournalMealFoodRepository extends JpaRepository<JournalMealFood, Long> {

    List<JournalMealFood> findAllByJournalMeal(JournalMeal journalMeal);

}
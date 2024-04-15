package com.pc.kilojoulesrest.repository;

import com.pc.kilojoulesrest.entity.JournalMeal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface JournalMealRepository extends JpaRepository<JournalMeal, Long> {

    @Query("SELECT jm FROM JournalMeal jm JOIN jm.journalMealFoods jmf WHERE jm.id = :mealId AND jmf.id = :foodId")
    Optional<JournalMeal> checkEntitiesExistence(@Param("mealId") Long mealId, @Param("foodId") Long foodId);

//    Quartz Scheduler
    void deleteAllBySavedFalse();

    boolean existsBySavedFalse();
}

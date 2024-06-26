package com.pc.kilojoulesrest.repository;

import com.pc.kilojoulesrest.entity.Journal;
import com.pc.kilojoulesrest.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface JournalRepository extends JpaRepository<Journal, Long> {

    @Query("select j from Journal j where j.consumedAt = ?1 and j.user =?2 order by j.mealType")
    List<Journal> findByConsumedAtAndUserOrderByMealTypeAsc(LocalDate consumedAt, User user);

    List<Journal> findAllByConsumedAtAndUser(LocalDate date, User user);

    Optional<Journal> findJournalByIdAndUser(Long journalId, User user);
    Optional<Journal> findJournalByIdAndJournalFoodId(Long journalId, Long journalFoodId);
    Optional<Journal> findJournalByIdAndJournalMealId(Long journalId, Long journalMealId);

    @Query("SELECT j FROM Journal j JOIN j.journalMeal jm JOIN jm.journalMealFoods jmf WHERE j.id = :journalId AND jm.id = :mealId AND jmf.id = :foodId")
    Optional<Journal> checkAllEntitiesExistence(@Param("journalId") Long journalId, @Param("mealId") Long mealId, @Param("foodId") Long foodId);

    List<Journal> findAllByUserAndConsumedAtBetween(User user, LocalDate startDate, LocalDate endDate);

    @Query("SELECT j FROM Journal j WHERE j.user = :user AND j.journalFood IS NOT NULL AND j.consumedAt BETWEEN :startDate AND :endDate ORDER BY j.journalFood.kiloJoules DESC LIMIT 10")
    List<Journal> findTop10ByJournalFoodKiloJoulesAndCreatedAtBetweenOrderByJournalFoodKjDesc(@Param("user") User user, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    @Query("SELECT j FROM Journal j WHERE j.user = :user AND j.journalMeal IS NOT NULL AND j.consumedAt BETWEEN :startDate AND :endDate ORDER BY j.journalMeal.kiloJoules DESC LIMIT 10")
    List<Journal> findTop10ByJournalMealKiloJoulesAndCreatedAtBetweenOrderByJournalMealKjDesc(@Param("user") User user, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT j FROM Journal j WHERE j.user = :user AND j.journalFood IS NOT NULL AND j.consumedAt BETWEEN :startDate AND :endDate ORDER BY j.journalFood.proteins DESC LIMIT 10")
    List<Journal> findTop10ByJournalFoodProteinsAndCreatedAtBetweenOrderByJournalFoodProteinsDesc(@Param("user") User user, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    @Query("SELECT j FROM Journal j WHERE j.user = :user AND j.journalMeal IS NOT NULL AND j.consumedAt BETWEEN :startDate AND :endDate ORDER BY j.journalMeal.proteins DESC LIMIT 10")
    List<Journal> findTop10ByJournalMealProteinsAndCreatedAtBetweenOrderByJournalMealProteinsDesc(@Param("user") User user, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT j FROM Journal j WHERE j.user = :user AND j.journalFood IS NOT NULL AND j.consumedAt BETWEEN :startDate AND :endDate ORDER BY j.journalFood.carbohydrates DESC LIMIT 10")
    List<Journal> findTop10ByJournalFoodCarbsAndCreatedAtBetweenOrderByJournalFoodCarbsDesc(@Param("user") User user, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    @Query("SELECT j FROM Journal j WHERE j.user = :user AND j.journalMeal IS NOT NULL AND j.consumedAt BETWEEN :startDate AND :endDate ORDER BY j.journalMeal.carbohydrates DESC LIMIT 10")
    List<Journal> findTop10ByJournalMealCarbsAndCreatedAtBetweenOrderByJournalMealCarbsDesc(@Param("user") User user, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT j FROM Journal j WHERE j.user = :user AND j.journalFood IS NOT NULL AND j.consumedAt BETWEEN :startDate AND :endDate ORDER BY j.journalFood.fiber DESC LIMIT 10")
    List<Journal> findTop10ByJournalFoodFiberAndCreatedAtBetweenOrderByJournalFoodFiberDesc(@Param("user") User user, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    @Query("SELECT j FROM Journal j WHERE j.user = :user AND j.journalMeal IS NOT NULL AND j.consumedAt BETWEEN :startDate AND :endDate ORDER BY j.journalMeal.fiber DESC LIMIT 10")
    List<Journal> findTop10ByJournalMealFiberAndCreatedAtBetweenOrderByJournalMealFiberDesc(@Param("user") User user, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT j FROM Journal j WHERE j.user = :user AND j.journalFood IS NOT NULL AND j.consumedAt BETWEEN :startDate AND :endDate ORDER BY j.journalFood.fat DESC LIMIT 10")
    List<Journal> findTop10ByJournalFoodFatAndCreatedAtBetweenOrderByJournalFoodFatDesc(@Param("user") User user, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    @Query("SELECT j FROM Journal j WHERE j.user = :user AND j.journalMeal IS NOT NULL AND j.consumedAt BETWEEN :startDate AND :endDate ORDER BY j.journalMeal.fat DESC LIMIT 10")
    List<Journal> findTop10ByJournalMealFatAndCreatedAtBetweenOrderByJournalMealFatDesc(@Param("user") User user, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    boolean existsJournalByIdAndUser(Long journalId, User user);

    boolean existsJournalById(Long journalId);
}
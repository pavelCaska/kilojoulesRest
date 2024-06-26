package com.pc.kilojoulesrest.repository;

import com.pc.kilojoulesrest.entity.Meal;
import com.pc.kilojoulesrest.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface MealRepository extends JpaRepository<Meal, Long>, PagingAndSortingRepository<Meal, Long> {

    Page<Meal> findAllByUser(User user, Pageable pageable);
    Page<Meal> findAllByUserAndMealNameContainsIgnoreCase(User user, String name, Pageable pageable);
    boolean existsMealByIdAndUser(Long id, User user);

}
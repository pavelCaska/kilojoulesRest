package com.pc.kilojoulesrest.repository;

import com.pc.kilojoulesrest.entity.Food;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface FoodRepository extends JpaRepository<Food, Long>, PagingAndSortingRepository<Food, Long> {

    Page<Food> findAllByNameContainsIgnoreCase(String query, Pageable pageable);
}
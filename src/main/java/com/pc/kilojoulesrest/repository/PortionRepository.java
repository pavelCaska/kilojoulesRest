package com.pc.kilojoulesrest.repository;

import com.pc.kilojoulesrest.entity.Food;
import com.pc.kilojoulesrest.entity.Portion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PortionRepository extends JpaRepository<Portion, Long> {

    Optional<Portion> findPortionByIdAndFoodId(Long portionId, Long foodId);

    int countPortionByFood(Food food);
}

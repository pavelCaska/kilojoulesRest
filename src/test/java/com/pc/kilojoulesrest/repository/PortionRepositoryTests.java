package com.pc.kilojoulesrest.repository;

import com.pc.kilojoulesrest.entity.Food;
import com.pc.kilojoulesrest.entity.Portion;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@DataJpaTest
class PortionRepositoryTests {

    @Autowired
    private PortionRepository portionRepository;
    @Autowired
    private FoodRepository foodRepository;

    @Test
    @DisplayName("JUnit test for save operation")
    public void givenPortionObject_whenSave_thenReturnSavedObject() {

        Food food = Food.builder()
                .name("Apple")
                .kiloJoules(BigDecimal.TEN)
                .proteins(BigDecimal.TEN)
                .carbohydrates(BigDecimal.TEN)
                .fat(BigDecimal.TEN)
                .build();
        foodRepository.save(food);

        Portion portion = Portion.builder()
                .portionName("1 g")
                .portionSize(BigDecimal.ONE)
                .food(food)
                .build();

        Portion savedPortion = portionRepository.save(portion);

        assertThat(savedPortion).isNotNull();
        assertThat(savedPortion.getId()).isGreaterThan(0);
        assertThat(savedPortion.getPortionName()).isEqualTo("1 g");
        assertThat(savedPortion.getPortionSize()).isEqualTo(BigDecimal.ONE);
        assertThat(savedPortion.getFood()).isEqualTo(food);
    }

    @Test
    @DisplayName("JUnit test for findById operation")
    public void givenPortionObject_whenFindById_thenReturnPortionObject() {

        Food food = Food.builder()
                .name("Apple")
                .kiloJoules(BigDecimal.TEN)
                .proteins(BigDecimal.TEN)
                .carbohydrates(BigDecimal.TEN)
                .fat(BigDecimal.TEN)
                .build();
        foodRepository.save(food);

        Portion portion = Portion.builder()
                .portionName("1 g")
                .portionSize(BigDecimal.ONE)
                .food(food)
                .build();

        Portion savedPortion = portionRepository.save(portion);

        Optional<Portion> foundPortion = portionRepository.findById(savedPortion.getId());

        assertThat(foundPortion).isPresent();
        assertThat(foundPortion.get()).isEqualTo(savedPortion);
    }

    @Test
    @DisplayName("JUnit test for delete operation")
    public void givenPortionObject_whenDelete_thenRemovePortionObject() {
        Food food = Food.builder()

                .name("Apple")
                .kiloJoules(BigDecimal.TEN)
                .proteins(BigDecimal.TEN)
                .carbohydrates(BigDecimal.TEN)
                .fat(BigDecimal.TEN)
                .build();
        foodRepository.save(food);

        Portion portion = Portion.builder()
                .portionName("1 g")
                .portionSize(BigDecimal.ONE)
                .food(food)
                .build();

        portionRepository.save(portion);

        portionRepository.delete(portion);

        assertThat(portionRepository.findById(portion.getId())).isEmpty();
    }

    @Test
    @DisplayName("JUnit test for countPortionByFood")
    public void givenPortionList_whenCountPortionByFood_thenReturnCount() {

        Food food = Food.builder()
                .name("Apple")
                .kiloJoules(BigDecimal.TEN)
                .proteins(BigDecimal.TEN)
                .carbohydrates(BigDecimal.TEN)
                .fat(BigDecimal.TEN)
                .build();
        foodRepository.save(food);

        Portion portion1 = Portion.builder()
                .portionName("1 g")
                .portionSize(BigDecimal.ONE)
                .food(food)
                .build();
        portionRepository.save(portion1);

        Portion portion2 = Portion.builder()
                .portionName("2 g")
                .portionSize(BigDecimal.valueOf(2))
                .food(food)
                .build();
        portionRepository.save(portion2);

        Portion portion3 = Portion.builder()
                .portionName("3 g")
                .portionSize(BigDecimal.valueOf(3))
                .food(food)
                .build();
        portionRepository.save(portion3);

        List<Portion> portions = new ArrayList<>();
        portions.add(portion1);
        portions.add(portion2);
        portions.add(portion3);
        food.setPortions(portions);
        foodRepository.save(food);

        int count = portionRepository.countPortionByFood(food);
        assertThat(count).isEqualTo(3);
    }

    @Test
    @DisplayName("JUnit test for findPortionByIdAndFoodId")
    public void givenPortionAndFoodObjects_whenFind_thenReturnPortionObject() {
        // given - precondition or setup
        Food food = Food.builder()
                .name("Apple")
                .kiloJoules(BigDecimal.TEN)
                .proteins(BigDecimal.TEN)
                .carbohydrates(BigDecimal.TEN)
                .fat(BigDecimal.TEN)
                .build();
        foodRepository.save(food);

        Portion portion = Portion.builder()
                .portionName("1 g")
                .portionSize(BigDecimal.ONE)
                .food(food)
                .build();
        portionRepository.save(portion);

        Portion portion2 = Portion.builder()
                .portionName("2 g")
                .portionSize(BigDecimal.valueOf(2))
                .food(food)
                .build();
        portionRepository.save(portion2);

        List<Portion> portions = new ArrayList<>();
        portions.add(portion);
        portions.add(portion2);

        food.setPortions(portions);
        foodRepository.save(food);

        Optional<Portion> foundPortion = portionRepository.findPortionByIdAndFoodId(portion.getId(), food.getId());

        assertThat(foundPortion).isPresent();
        assertThat(foundPortion.get()).isEqualTo(portion);

    }
}
package com.pc.kilojoulesrest.repository;

import com.pc.kilojoulesrest.entity.Food;
import com.pc.kilojoulesrest.entity.Portion;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
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

    private Food food;
    private Portion portion;

    @BeforeEach
    void setUp() {
        food = Food.builder()
                .name("Apple")
                .kiloJoules(BigDecimal.TEN)
                .proteins(BigDecimal.TEN)
                .carbohydrates(BigDecimal.TEN)
                .fat(BigDecimal.TEN)
                .build();
        foodRepository.save(food);

        portion = Portion.builder()
                .portionName("1 g")
                .portionSize(BigDecimal.ONE)
                .food(food)
                .build();
        portionRepository.save(portion);
    }

    @Test
    @DisplayName("JUnit test for save operation")
    void givenPortionObject_whenSave_thenReturnSavedObject() {

        Portion testPortion = Portion.builder()
                .portionName("1 g")
                .portionSize(BigDecimal.ONE)
                .food(food)
                .build();

        Portion savedPortion = portionRepository.save(testPortion);

        assertThat(savedPortion).isNotNull();
        assertThat(savedPortion.getId()).isGreaterThan(0);
        assertThat(savedPortion.getPortionName()).isEqualTo("1 g");
        assertThat(savedPortion.getPortionSize()).isEqualTo(BigDecimal.ONE);
        assertThat(savedPortion.getFood()).isEqualTo(food);
    }

    @Test
    @DisplayName("JUnit test for findById operation")
    void givenPortionObject_whenFindById_thenReturnPortionObject() {

        Long portionId = portion.getId();

        Optional<Portion> foundPortion = portionRepository.findById(portionId);

        assertThat(foundPortion).isPresent();
        assertThat(foundPortion.get()).isEqualTo(portion);
    }

    @Test
    @DisplayName("JUnit test for delete operation")
    void givenPortionObject_whenDelete_thenRemovePortionObject() {

        portionRepository.delete(portion);

        assertThat(portionRepository.findById(portion.getId())).isEmpty();
    }

    @Test
    @DisplayName("JUnit test for countPortionByFood")
    void givenPortionList_whenCountPortionByFood_thenReturnCount() {

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
    void givenPortionAndFoodObjects_whenFind_thenReturnPortionObject() {
        Food apple = Food.builder()
                .name("Apple")
                .kiloJoules(BigDecimal.TEN)
                .proteins(BigDecimal.TEN)
                .carbohydrates(BigDecimal.TEN)
                .fat(BigDecimal.TEN)
                .build();
        foodRepository.save(apple);

        Portion portion1 = Portion.builder()
                .portionName("1 g")
                .portionSize(BigDecimal.ONE)
                .food(apple)
                .build();
        portionRepository.save(portion1);

        Portion portion2 = Portion.builder()
                .portionName("2 g")
                .portionSize(BigDecimal.valueOf(2))
                .food(apple)
                .build();
        portionRepository.save(portion2);

        List<Portion> portions = new ArrayList<>();
        portions.add(portion1);
        portions.add(portion2);

        apple.setPortions(portions);
        foodRepository.save(apple);

        Long portionId = portion1.getId();
        Long foodId = apple.getId();

        Optional<Portion> foundPortion = portionRepository.findPortionByIdAndFoodId(portionId, foodId);

        assertThat(foundPortion).isPresent();
        assertThat(foundPortion.get()).isEqualTo(portion1);
    }
}
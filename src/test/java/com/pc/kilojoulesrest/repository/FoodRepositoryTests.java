package com.pc.kilojoulesrest.repository;

import com.pc.kilojoulesrest.entity.Food;
import com.pc.kilojoulesrest.entity.Portion;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@DataJpaTest
class FoodRepositoryTests {

    @Autowired
    private FoodRepository foodRepository;

    @Test
    @DisplayName("JUnit test save food operation")
    public void givenFoodObject_whenSave_thenReturnSavedFood() {

        Food food = Food.builder()
                .name("Apple")
                .kiloJoules(BigDecimal.TEN)
                .proteins(BigDecimal.TEN)
                .carbohydrates(BigDecimal.TEN)
                .fat(BigDecimal.TEN)
                .build();

        Portion portion = Portion.builder()
                .portionName("1 g")
                .portionSize(BigDecimal.ONE)
                .food(food)
                .build();

        List<Portion> portions = new ArrayList<>();
        portions.add(portion);

        food.setPortions(portions);
        Food savedFood = foodRepository.save(food);

        assertThat(savedFood).isNotNull();
        assertThat(savedFood.getId()).isGreaterThan(0);
        assertThat(savedFood.getName()).isEqualTo("Apple");
        assertThat(savedFood.getQuantity()).isEqualTo(BigDecimal.valueOf(100));
        assertThat(savedFood.getKiloJoules()).isEqualTo(BigDecimal.TEN);
        assertThat(savedFood.getProteins()).isEqualTo(BigDecimal.TEN);
        assertThat(savedFood.getCarbohydrates()).isEqualTo(BigDecimal.TEN);
        assertThat(savedFood.getFiber()).isEqualTo(BigDecimal.ZERO);
        assertThat(savedFood.getSugar()).isEqualTo(BigDecimal.ZERO);
        assertThat(savedFood.getFat()).isEqualTo(BigDecimal.TEN);
        assertThat(savedFood.getSafa()).isEqualTo(BigDecimal.ZERO);
        assertThat(savedFood.getTfa()).isEqualTo(BigDecimal.ZERO);
        assertThat(savedFood.getCholesterol()).isEqualTo(BigDecimal.ZERO);
        assertThat(savedFood.getSodium()).isEqualTo(BigDecimal.ZERO);
        assertThat(savedFood.getCalcium()).isEqualTo(BigDecimal.ZERO);
        assertThat(savedFood.getPhe()).isEqualTo(BigDecimal.ZERO);
        assertThat(savedFood.getCreatedAt()).isNotNull();
        assertThat(savedFood.getUpdatedAt()).isNotNull();
        assertThat(savedFood.getPortions()).isNotEmpty();
    }

    @Test
    @DisplayName("JUnit test for findAll operation")
    public void givenFoodList_whenFindAll_thenReturnListOfFoods() {

        Food apple = Food.builder()
                .name("Apple")
                .kiloJoules(BigDecimal.TEN)
                .proteins(BigDecimal.TEN)
                .carbohydrates(BigDecimal.TEN)
                .fat(BigDecimal.TEN)
                .build();

        Portion portion = Portion.builder()
                .portionName("1 g")
                .portionSize(BigDecimal.ONE)
                .food(apple)
                .build();

        List<Portion> portions = new ArrayList<>();
        portions.add(portion);

        apple.setPortions(portions);
        foodRepository.save(apple);

        Food mango = Food.builder()
                .name("Mango")
                .kiloJoules(BigDecimal.TEN)
                .proteins(BigDecimal.TEN)
                .carbohydrates(BigDecimal.TEN)
                .fat(BigDecimal.TEN)
                .build();

        Portion portion2 = Portion.builder()
                .portionName("1 g")
                .portionSize(BigDecimal.ONE)
                .food(mango)
                .build();

        List<Portion> portions2 = new ArrayList<>();
        portions2.add(portion2);

        mango.setPortions(portions2);
        foodRepository.save(mango);

        List<Food> foods = foodRepository.findAll();

        assertThat(foods).isNotEmpty();
        assertThat(foods).hasSize(2);
        assertThat(foods).contains(apple, mango);
    }

    @Test
    @DisplayName("JUnit test for findById operation")
    public void givenFoodObject_whenFindById_thenReturnFood() {

        Food apple = Food.builder()
                .name("Apple")
                .kiloJoules(BigDecimal.TEN)
                .proteins(BigDecimal.TEN)
                .carbohydrates(BigDecimal.TEN)
                .fat(BigDecimal.TEN)
                .build();

        Portion portion = Portion.builder()
                .portionName("1 g")
                .portionSize(BigDecimal.ONE)
                .food(apple)
                .build();

        List<Portion> portions = new ArrayList<>();
        portions.add(portion);

        apple.setPortions(portions);
        foodRepository.save(apple);

        Optional<Food> optionalFood = foodRepository.findById(apple.getId());

        assertThat(optionalFood).isPresent();
        assertThat(optionalFood.get().getId()).isEqualTo(apple.getId());
        assertThat(optionalFood.get().getPortions()).isEqualTo(apple.getPortions());
    }

    @Test
    @DisplayName("JUnit test for delete operation")
    public void givenFoodObject_whenDelete_thenRemoveFood() {

        Food apple = Food.builder()
                .name("Apple")
                .kiloJoules(BigDecimal.TEN)
                .proteins(BigDecimal.TEN)
                .carbohydrates(BigDecimal.TEN)
                .fat(BigDecimal.TEN)
                .build();

        Portion portion = Portion.builder()
                .portionName("1 g")
                .portionSize(BigDecimal.ONE)
                .food(apple)
                .build();

        List<Portion> portions = new ArrayList<>();
        portions.add(portion);

        apple.setPortions(portions);
        foodRepository.save(apple);

        foodRepository.delete(apple);

        assertThat(foodRepository.findById(apple.getId())).isEmpty();
    }

    @Test
    @DisplayName("JUnit test for findAllByNameContains")
    public void givenString_whenFindAllByName_thenReturnPage() {
        Food apple = Food.builder()
                .name("Apple")
                .kiloJoules(BigDecimal.TEN)
                .proteins(BigDecimal.TEN)
                .carbohydrates(BigDecimal.TEN)
                .fat(BigDecimal.TEN)
                .build();

        Portion portion = Portion.builder()
                .portionName("1 g")
                .portionSize(BigDecimal.ONE)
                .food(apple)
                .build();

        List<Portion> portions = new ArrayList<>();
        portions.add(portion);

        apple.setPortions(portions);
        foodRepository.save(apple);

        Food mango = Food.builder()
                .name("Mango")
                .kiloJoules(BigDecimal.TEN)
                .proteins(BigDecimal.TEN)
                .carbohydrates(BigDecimal.TEN)
                .fat(BigDecimal.TEN)
                .build();

        Portion portion2 = Portion.builder()
                .portionName("1 g")
                .portionSize(BigDecimal.ONE)
                .food(mango)
                .build();

        List<Portion> portions2 = new ArrayList<>();
        portions2.add(portion2);

        mango.setPortions(portions2);
        foodRepository.save(mango);

        Pageable pageable = PageRequest.of(0, 25, Sort.Direction.ASC, "name");
        Page<Food> foods = foodRepository.findAllByNameContainsIgnoreCase("man", pageable);

        assertThat(foods).isNotEmpty();
        assertThat(foods).hasSize(1);
        assertThat(foods.getContent().get(0).getName()).isEqualTo("Mango");
    }
}

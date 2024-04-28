package com.pc.kilojoulesrest.repository;

import com.pc.kilojoulesrest.entity.*;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@DataJpaTest
@Import(BCryptPasswordEncoder.class)
class MealFoodRepositoryTest {

    @Autowired
    private MealFoodRepository mealFoodRepository;

    @Autowired
    private FoodRepository foodRepository;

    @Autowired
    private MealRepository mealRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Food food;
    private Meal meal;
    private MealFood mealFood;

    @BeforeEach
    void setUp() {
        food = Food.builder()
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
        foodRepository.save(food);

        User user = User.builder()
                .username("testUser")
                .password(passwordEncoder.encode("testPassword"))
                .roles("ROLE_USER")
                .build();
        userRepository.save(user);

        meal = Meal.builder()
                .mealName("Good meal")
                .createdAt(new Date())
                .updatedAt(new Date())
                .user(user)
                .build();

        mealRepository.save(meal);

        mealFood = MealFood.builder()
                .food(food)
                .meal(meal)
                .quantity(BigDecimal.TEN)
                .build();
        mealFoodRepository.save(mealFood);
    }

    @Test
    @DisplayName("JUnit test for save operation")
    void givenMealFood_whenSave_thenReturnSavedObject() {

        MealFood testMealFood = MealFood.builder()
                .food(food)
                .meal(meal)
                .quantity(BigDecimal.ONE)
                .build();

        MealFood savedMealFood = mealFoodRepository.save(testMealFood);

        assertThat(savedMealFood).isNotNull();
        assertThat(savedMealFood.getId()).isNotNull();
        assertThat(savedMealFood.getFood()).isEqualTo(food);
        assertThat(savedMealFood.getMeal()).isEqualTo(meal);
        assertThat(savedMealFood.getQuantity()).isEqualTo(BigDecimal.ONE);
    }

    @Test
    @DisplayName("JUnit test for findById operation")
    void givenMealFoodSaved_whenFindById_thenMealFoodIsReturned() {

        Long mealFoodId = mealFood.getId();

        Optional<MealFood> optionalMealFood = mealFoodRepository.findById(mealFoodId);

        assertThat(optionalMealFood).isPresent();
        assertThat(optionalMealFood.get()).isEqualTo(mealFood);
    }

    @Test
    @DisplayName("JUnit test for delete operation")
    void givenMealFood_whenDelete_thenRemoveMealFood() {

        mealFoodRepository.delete(mealFood);
        Optional<MealFood> deletedMealFood = mealFoodRepository.findById(mealFood.getId());

        assertThat(deletedMealFood).isEmpty();
    }

    @Test
    @DisplayName("JUnit test for findMealFoodByMealIdAndId operation")
    void givenValidParam_whenFindMealFoodByMealIdAndId_thenReturnsMealFoodObject() {

        Long mealId = meal.getId();
        Long mealFoodId = mealFood.getId();

        Optional<MealFood> foundMealFood = mealFoodRepository.findMealFoodByMealIdAndId(mealId, mealFoodId);

        assertThat(foundMealFood).isPresent();
        assertThat(foundMealFood.get()).isEqualTo(mealFood);
    }
}
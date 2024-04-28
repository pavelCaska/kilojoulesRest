package com.pc.kilojoulesrest.repository;

import com.pc.kilojoulesrest.entity.*;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.*;

@DataJpaTest
@Import(BCryptPasswordEncoder.class)
class MealRepositoryTest {

    @Autowired
    private MealRepository mealRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private FoodRepository foodRepository;
    @Autowired
    private MealFoodRepository mealFoodRepository;

    private Meal meal;

    @BeforeEach
    void setUp() {
        User user = userRepository.save(User.builder()
                .username("testUser")
                .password(passwordEncoder.encode("testPassword"))
                .roles("ROLE_USER")
                .build());
        userRepository.save(user);

        meal = Meal.builder()
                .mealName("Good meal")
                .createdAt(new Date())
                .updatedAt(new Date())
                .user(user)
                .build();
        mealRepository.save(meal);
    }

    @Test
    @DisplayName("JUnit test for save operation")
    void givenMealObject_whenSave_thenReturnSavedMeal() {

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
        foodRepository.save(food);

        MealFood mealFood = MealFood.builder()
                .food(food)
                .meal(meal)
                .quantity(BigDecimal.TEN)
                .build();

        mealFoodRepository.save(mealFood);

        Set<MealFood> mealFoodSet = new HashSet<>();
        mealFoodSet.add(mealFood);
        meal.setMealFoods(mealFoodSet);

        Meal savedMeal = mealRepository.save(meal);

        assertThat(savedMeal).isNotNull();
        assertThat(savedMeal.getMealName()).isEqualTo(meal.getMealName());
        assertThat(savedMeal.getCreatedAt()).isEqualTo(meal.getCreatedAt());
        assertThat(savedMeal.getUpdatedAt()).isEqualTo(meal.getUpdatedAt());
        assertThat(savedMeal.getUser()).isEqualTo(meal.getUser());
        assertThat(savedMeal.getMealFoods()).hasSize(1).containsExactly(mealFood);
    }

    @Test
    @DisplayName("JUnit test for findById operation")
    void givenMealObject_whenFindById_thenReturnMealObject() {

        Meal savedMeal = mealRepository.save(meal);

        Optional<Meal> foundMeal = mealRepository.findById(savedMeal.getId());

        assertThat(foundMeal).isPresent();
        assertThat(foundMeal.get().getMealName()).isEqualTo(meal.getMealName());

    }

    @Test
    @DisplayName("JUnit test for delete operation")
    void givenMealObject_whenDelete_thenRemoveMealObject() {

        mealRepository.delete(meal);

        assertThat(mealRepository.findById(meal.getId())).isEmpty();
    }

    @Test
    @DisplayName("JUnit test for findAllByUser operation")
    void givenMultipleMeals_whenFindAllByUser_thenReturnCorrectMeals() {
        User user1 = User.builder()
                .username("testUser1")
                .password(passwordEncoder.encode("testPassword"))
                .roles("ROLE_USER")
                .build();
        userRepository.save(user1);

        User user2 = User.builder()
                .username("testUser2")
                .password(passwordEncoder.encode("testPassword"))
                .roles("ROLE_USER")
                .build();
        userRepository.save(user2);

        Meal meal1 = Meal.builder()
                .mealName("Good meal")
                .createdAt(new Date())
                .updatedAt(new Date())
                .user(user2)
                .build();
        mealRepository.save(meal1);

        Meal meal2 = Meal.builder()
                .mealName("Better meal")
                .createdAt(new Date())
                .updatedAt(new Date())
                .user(user2)
                .build();
        mealRepository.save(meal2);

        Meal meal3 = Meal.builder()
                .mealName("Best meal")
                .createdAt(new Date())
                .updatedAt(new Date())
                .user(user1)
                .build();
        mealRepository.save(meal3);

        Pageable pageable = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "updatedAt"));
        Page<Meal> meals = mealRepository.findAllByUser(user1, pageable);

        assertThat(meals).isNotNull();
        assertThat(meals).hasSize(1);
        assertThat(meals.getContent().get(0).getMealName()).isEqualTo("Best meal");
    }
}
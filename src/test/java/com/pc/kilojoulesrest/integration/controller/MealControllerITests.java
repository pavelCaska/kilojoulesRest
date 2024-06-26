package com.pc.kilojoulesrest.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pc.kilojoulesrest.entity.*;
import com.pc.kilojoulesrest.exception.RecordNotFoundException;
import com.pc.kilojoulesrest.model.MealDTO;
import com.pc.kilojoulesrest.model.MealFormDTO;
import com.pc.kilojoulesrest.service.*;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.util.*;

import static com.pc.kilojoulesrest.constant.Constant.ONE_HUNDRED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest
@ActiveProfiles("test")
public class MealControllerITests {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserService userService;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private MealService mealService;
    @Autowired
    private FoodService foodService;
    @Autowired
    private PortionService portionService;
    @Autowired
    private MealFoodService mealFoodService;
    @Autowired
    private ObjectMapper om;

    private Food food;
    private String authorizedUser;
    private Meal meal;
    private User user;

    @BeforeEach
    void setUp() {
        food = Food.builder()
                .name("Apple")
                .kiloJoules(BigDecimal.TEN)
                .proteins(BigDecimal.TEN)
                .carbohydrates(BigDecimal.TEN)
                .fat(BigDecimal.TEN)
                .build();
        foodService.saveFood(food);
        Portion portion1 = Portion.builder()
                .portionName("1 g")
                .portionSize(BigDecimal.ONE)
                .food(food)
                .build();
        portionService.savePortion(portion1);

        Portion portion100 = Portion.builder()
                .portionName("100 g")
                .portionSize(ONE_HUNDRED)
                .food(food)
                .build();
        portionService.savePortion(portion100);

        List<Portion> portions = new ArrayList<>();
        portions.add(portion1);
        portions.add(portion100);
        food.setPortions(portions);
        foodService.saveFood(food);

        user = User.builder()
                .username("user for MealController integration test")
                .password(userService.encodePassword("user1pwd"))
                .roles("ROLE_USER")
                .build();
        userService.saveUser(user);

        authorizedUser = "Bearer " + jwtService.generateToken("user for MealController integration test");

        meal = Meal.builder()
                .user(user)
                .mealName("Fruit")
                .build();
        mealService.saveMeal(meal);
        MealFood mealFood = MealFood.builder()
                .quantity(ONE_HUNDRED)
                .food(food)
                .meal(meal)
                .build();

        Set<MealFood> mealFoods = new HashSet<>();
        mealFoods.add(mealFood);
        meal.setMealFoods(mealFoods);
        mealService.saveMeal(meal);

    }

    @Test
    @Transactional
    @DisplayName("Integration test positive case for meal pagination operation")
    void givenNoParam_whenFetchMealsPaged_thenReturnsPage() throws Exception {
        Food kiwi = Food.builder()
                .name("Kiwi")
                .kiloJoules(BigDecimal.TEN)
                .proteins(BigDecimal.TEN)
                .carbohydrates(BigDecimal.TEN)
                .fat(BigDecimal.TEN)
                .build();
        foodService.saveFood(kiwi);
        Portion portion1 = Portion.builder()
                .portionName("1 g")
                .portionSize(BigDecimal.ONE)
                .food(kiwi)
                .build();
        portionService.savePortion(portion1);

        Portion portion100 = Portion.builder()
                .portionName("100 g")
                .portionSize(ONE_HUNDRED)
                .food(kiwi)
                .build();
        portionService.savePortion(portion100);

        List<Portion> portions = new ArrayList<>();
        portions.add(portion1);
        portions.add(portion100);
        kiwi.setPortions(portions);
        foodService.saveFood(kiwi);

        Meal mealKiwi = Meal.builder()
                .user(user)
                .mealName("Kiwi")
                .build();
        mealService.saveMeal(mealKiwi);
        MealFood mealFoodKiwi = MealFood.builder()
                .quantity(ONE_HUNDRED)
                .food(food)
                .meal(mealKiwi)
                .build();

        Set<MealFood> mealFoods = new HashSet<>();
        mealFoods.add(mealFoodKiwi);
        mealKiwi.setMealFoods(mealFoods);
        mealService.saveMeal(mealKiwi);

        ResultActions response = mockMvc.perform(get("/api/meal")
                .header("Authorization", authorizedUser)
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.pageNumber").value(1))
                .andExpect(jsonPath("$.totalPages").isNotEmpty())
                .andExpect(jsonPath("$.meals", hasSize(2)))
                .andExpect(jsonPath("$.meals[*].mealId", hasItems(meal.getId().intValue(), mealKiwi.getId().intValue())))
                .andExpect(jsonPath("$.meals[0].foods", hasSize(1)))
                .andExpect(jsonPath("$.meals[1].foods", hasSize(1)));
    }

    @Test
    @Transactional
    @DisplayName("Integration test positive case for create meal operation")
    void givenValidInput_whenCreateMeal_thenMealCreatedAndMealDtoReturned() throws Exception {
        MealFormDTO mealFormDTO = new MealFormDTO();
        mealFormDTO.setMealName("Breakfast");
        mealFormDTO.setQuantity(BigDecimal.ONE);
        mealFormDTO.setPortionSize(ONE_HUNDRED);

        List<Long> foods = List.of(food.getId());

        ResultActions resultActions = mockMvc.perform(post("/api/meal", mealFormDTO)
            .param("foods", foods.stream().map(Object::toString).toArray(String[]::new))
                .header("Authorization", authorizedUser)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(mealFormDTO)));

        MvcResult mvcResult = resultActions.andReturn();
        String responseBody = mvcResult.getResponse().getContentAsString();
        MealDTO mealDTO = om.readValue(responseBody, MealDTO.class);

        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.mealName").value("Breakfast"))
                .andExpect(jsonPath("$.foods[0].foodId").value(food.getId()))
                .andExpect(jsonPath("$.foods[0].foodName").value(food.getName()))
                .andExpect(jsonPath("$.foods[0].quantity").value(100.00))
                .andExpect(jsonPath("$.foods[0].adjustedKiloJoules").value(10.00))
                .andExpect(jsonPath("$.foods[0].adjustedFiber").value(0.00))
                .andExpect(jsonPath("$.sumQuantity").value(100.00))
                .andExpect(jsonPath("$.sumAdjustedKiloJoules").value(10.00))
                .andExpect(jsonPath("$.sumAdjustedFiber").value(0.00))
                .andExpect(header().string("Location", "/api/meal/" + mealDTO.getMealId()));
    }

    @Test
    @Transactional
    @DisplayName("Integration test negative case for create meal operation - missing mealName and portionSize")
    void givenInvalidDto_whenCreateMeal_thenBindingResult() throws Exception {
        MealFormDTO mealFormDTO = new MealFormDTO();
        mealFormDTO.setMealName("");
        mealFormDTO.setQuantity(BigDecimal.ONE);
//        mealFormDTO.setPortionSize(ONE_HUNDRED);

        List<Long> foods = List.of(food.getId());

        ResultActions response = mockMvc.perform(post("/api/meal", mealFormDTO)
            .param("foods", foods.stream().map(Object::toString).toArray(String[]::new))
                .header("Authorization", authorizedUser)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(mealFormDTO)));

        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mealName").value("Name cannot be empty."))
                .andExpect(jsonPath("$.portionSize").value("must not be null"));
    }

    @Test
    @Transactional
    @DisplayName("Integration test negative case for create meal operation - invalid foodId")
    void givenInvalidParam_whenCreateMeal_thenThrowsException() throws Exception {
        MealFormDTO mealFormDTO = new MealFormDTO();
        mealFormDTO.setMealName("Breakfast");
        mealFormDTO.setQuantity(BigDecimal.ONE);
        mealFormDTO.setPortionSize(ONE_HUNDRED);

        List<Long> foods = List.of(food.getId() + 1);

        ResultActions response = mockMvc.perform(post("/api/meal", mealFormDTO)
            .param("foods", foods.stream().map(Object::toString).toArray(String[]::new))
                .header("Authorization", authorizedUser)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(mealFormDTO)));

        response.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Food record with id " + foods.toArray()[0] + " does not exist!"));
    }

    @Test
    @Transactional
    @DisplayName("Integration test positive case for add food to meal operation")
    void givenValidInput_whenAddFoodToMeal_thenMealFoodAddedAndMealDtoReturned() throws Exception {
        Food kiwi = Food.builder()
                .name("Kiwi")
                .kiloJoules(BigDecimal.ONE)
                .proteins(BigDecimal.ONE)
                .carbohydrates(BigDecimal.ONE)
                .fat(BigDecimal.ONE)
                .build();
        foodService.saveFood(kiwi);
        Portion portion1 = Portion.builder()
                .portionName("1 g")
                .portionSize(BigDecimal.ONE)
                .food(kiwi)
                .build();
        portionService.savePortion(portion1);

        Portion portion100 = Portion.builder()
                .portionName("100 g")
                .portionSize(ONE_HUNDRED)
                .food(kiwi)
                .build();
        portionService.savePortion(portion100);

        List<Portion> portions = new ArrayList<>();
        portions.add(portion1);
        portions.add(portion100);
        kiwi.setPortions(portions);
        foodService.saveFood(kiwi);

        MealFormDTO mealFormDTO = new MealFormDTO();
        mealFormDTO.setMealName("Fruits");
        mealFormDTO.setQuantity(BigDecimal.ONE);
        mealFormDTO.setPortionSize(ONE_HUNDRED);

        List<Long> foods = List.of(kiwi.getId());

        Long mealId = meal.getId();

        ResultActions response = mockMvc.perform(post("/api/meal/{id}/add-food", mealId, mealFormDTO)
                .param("foods", foods.stream().map(Object::toString).toArray(String[]::new))
                .header("Authorization", authorizedUser)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(mealFormDTO)));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.mealName").value("Fruits"))
                .andExpect(jsonPath("$.mealId").value(mealId))
                .andExpect(jsonPath("$.sumQuantity").value(200.00))
                .andExpect(jsonPath("$.sumAdjustedKiloJoules").value(11.00))
                .andExpect(jsonPath("$.sumAdjustedFiber").value(0.00))
                .andExpect(jsonPath("$.foods", hasSize(2)))
                .andExpect(jsonPath("$.foods[*].foodId", hasItems(food.getId().intValue(), kiwi.getId().intValue())))
                .andExpect(jsonPath("$.foods[*].foodName", hasItems(food.getName(), kiwi.getName())))
                .andExpect(jsonPath("$.foods[*].quantity", hasItems(100.00, 100.00)))
                .andExpect(jsonPath("$.foods[*].adjustedKiloJoules", hasItems(10.00, 1.00)))
                .andExpect(jsonPath("$.foods[*].adjustedFiber", hasItems(0.00, 0.00)));
    }

    @Test
    @Transactional
    @DisplayName("Integration test negative case for add food to meal operation - invalid mealId")
    void givenInvalidInput_whenAddFoodToMeal_thenThrowsRecordNotFoundException() throws Exception {
        Food kiwi = Food.builder()
                .name("Kiwi")
                .kiloJoules(BigDecimal.ONE)
                .proteins(BigDecimal.ONE)
                .carbohydrates(BigDecimal.ONE)
                .fat(BigDecimal.ONE)
                .build();
        foodService.saveFood(kiwi);
        Portion portion1 = Portion.builder()
                .portionName("1 g")
                .portionSize(BigDecimal.ONE)
                .food(kiwi)
                .build();
        portionService.savePortion(portion1);

        Portion portion100 = Portion.builder()
                .portionName("100 g")
                .portionSize(ONE_HUNDRED)
                .food(kiwi)
                .build();
        portionService.savePortion(portion100);

        List<Portion> portions = new ArrayList<>();
        portions.add(portion1);
        portions.add(portion100);
        kiwi.setPortions(portions);
        foodService.saveFood(kiwi);

        MealFormDTO mealFormDTO = new MealFormDTO();
        mealFormDTO.setMealName("Fruits");
        mealFormDTO.setQuantity(BigDecimal.ONE);
        mealFormDTO.setPortionSize(ONE_HUNDRED);

        List<Long> foods = List.of(kiwi.getId());

        Long mealId = meal.getId() + 1;

        ResultActions response = mockMvc.perform(post("/api/meal/{id}/add-food", mealId, mealFormDTO)
                .param("foods", foods.stream().map(Object::toString).toArray(String[]::new))
                .header("Authorization", authorizedUser)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(mealFormDTO)));

        response.andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("The meal doesn't belong to the current user."));
    }

    @Test
    @Transactional
    @DisplayName("Integration test positive case for update meal name operation")
    void givenValidParam_whenUpdateMealName_thenReturnsMealDto() throws Exception {
        String mealName = "Breakfast";
        Long mealId = meal.getId();

        ResultActions response = mockMvc.perform(patch("/api/meal/{id}/update-name", mealId)
                .param("mealName", mealName)
                .header("Authorization", authorizedUser)
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.mealName").value("Breakfast"))
                .andExpect(jsonPath("$.mealId").value(mealId))
                .andExpect(jsonPath("$.foods[0].foodId").value(food.getId()))
                .andExpect(jsonPath("$.foods[0].foodName").value(food.getName()))
                .andExpect(jsonPath("$.foods[0].quantity").value(100.00))
                .andExpect(jsonPath("$.foods[0].adjustedKiloJoules").value(10.00))
                .andExpect(jsonPath("$.foods[0].adjustedFiber").value(0.00))
                .andExpect(jsonPath("$.sumQuantity").value(100.00))
                .andExpect(jsonPath("$.sumAdjustedKiloJoules").value(10.00))
                .andExpect(jsonPath("$.sumAdjustedFiber").value(0.00));
    }

    @Test
    @Transactional
    @DisplayName("Integration test negative case for update meal name operation - empty mealName")
    void givenInvalidParam_whenUpdateMealName_thenThrowsException() throws Exception {
        String mealName = "";
        Long mealId = meal.getId();

        ResultActions response = mockMvc.perform(patch("/api/meal/{id}/update-name", mealId)
                .param("mealName", mealName)
                .header("Authorization", authorizedUser)
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Provided mealName is either null, empty, has only whitespaces or its length is greater than 75 characters."));
    }

    @Test
    @Transactional
    @DisplayName("Integration test positive case for delete meal operation")
    void givenValidInput_whenDeleteMealById_thenMealDeletedAndHttpStatusNoContent() throws Exception {
        Long mealId = meal.getId();

        ResultActions response = mockMvc.perform(delete("/api/meal/{id}", mealId)
                        .header("Authorization", authorizedUser)
                        .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(status().isNoContent());

        RecordNotFoundException exception = assertThrows(RecordNotFoundException.class, () -> mealService.getMealById(mealId));
        assertThat(exception.getMessage()).isEqualTo("Meal record with id " + mealId + " does not exist!");
    }

    @Test
    @Transactional
    @DisplayName("Integration test negative case for delete meal operation - unauthorized user")
    void givenUnauthorizedUser_whenDeleteMealById_thenThrowsIllegalArgumentException() throws Exception {
        Long mealId = meal.getId();
        User noDataUser = new User();
        noDataUser.setUsername("noDataUser for MealControllerITests");
        noDataUser.setPassword(userService.encodePassword("noDataUser2Pwd"));
        noDataUser.setRoles("ROLE_USER");
        userService.saveUser(noDataUser);
        String authorizedNoDataUser = "Bearer " + jwtService.generateToken("noDataUser for MealControllerITests");

        ResultActions response = mockMvc.perform(delete("/api/meal/{id}", mealId)
                        .header("Authorization", authorizedNoDataUser)
                        .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("User does not have permission to delete this meal!"));

        assertThat(mealService.getMealById(mealId)).isNotNull();
    }

    @Test
    @Transactional
    @DisplayName("Integration test positive case for delete food from meal operation")
    void givenValidInput_whenDeleteMealFoodById_thenMealFoodDeletedAndHttpStatusNoContent() throws Exception {
        Long mealId = meal.getId();
        Long mealFoodId = meal.getMealFoods().stream().map(MealFood::getId).findFirst().orElse(null);

        mockMvc.perform(delete("/api/meal/{mealId}/food/{foodId}", mealId, mealFoodId)
                        .header("Authorization", authorizedUser)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        RecordNotFoundException exception = assertThrows(RecordNotFoundException.class, () -> mealFoodService.getMealFoodById(mealFoodId));
        assertThat(exception.getMessage()).isEqualTo("MealFood record with id " + mealFoodId + " does not exist!");
    }

    @Test
    @Transactional
    @DisplayName("Integration test negative case for delete food from meal operation - id mismatch")
    void givenIdMismatch_whenDeleteMealFoodById_thenThrowsExceptionAndReturnsErrorDto() throws Exception {
        Long mealId = meal.getId() + 1;
        Long mealFoodId = meal.getMealFoods().stream().map(MealFood::getId).findFirst().orElse(null);

        ResultActions response = mockMvc.perform(delete("/api/meal/{mealId}/food/{mealFoodId}", mealId, mealFoodId)
                        .header("Authorization", authorizedUser)
                        .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Food record with id " + mealFoodId + " is not associated with Meal record with id " + mealId));

        Meal mealAfterDelete = mealService.getMealById(meal.getId());
        assertThat(mealAfterDelete.getMealFoods()).hasSize(1);
    }

    @Test
    @Transactional
    @DisplayName("Integration test positive case for update food assigned to meal operation")
    void givenValidInput_whenUpdateMealFood_thenMealFoodUpdatedAndMealDtoReturned() throws Exception {
        Long mealId = meal.getId();
        Long mealFoodId = meal.getMealFoods().stream().map(MealFood::getId).findFirst().orElse(null);

        MealFormDTO mealFormDTO = new MealFormDTO();
        mealFormDTO.setMealName("Breakfast");
        mealFormDTO.setQuantity(BigDecimal.TEN);
        mealFormDTO.setPortionSize(ONE_HUNDRED);

        ResultActions response = mockMvc.perform(put("/api/meal/{mealId}/food/{foodId}", mealId, mealFoodId)
                .header("Authorization", authorizedUser)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(mealFormDTO)));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.mealName").value("Breakfast"))
                .andExpect(jsonPath("$.mealId").value(mealId))
                .andExpect(jsonPath("$.foods[0].foodId").value(food.getId()))
                .andExpect(jsonPath("$.foods[0].foodName").value(food.getName()))
                .andExpect(jsonPath("$.foods[0].quantity").value(1000.00))
                .andExpect(jsonPath("$.foods[0].adjustedKiloJoules").value(100.00))
                .andExpect(jsonPath("$.foods[0].adjustedFiber").value(0.00))
                .andExpect(jsonPath("$.sumQuantity").value(1000.00))
                .andExpect(jsonPath("$.sumAdjustedKiloJoules").value(100.00))
                .andExpect(jsonPath("$.sumAdjustedFiber").value(0.00));
    }

    @Test
    @Transactional
    @DisplayName("Integration test negative case for update food assigned to meal operation - invalid mealFormDTO")
    void givenInvalidInput_whenUpdateMealFood_thenBindingResultError() throws Exception {
        Long mealId = meal.getId();
        Long mealFoodId = meal.getMealFoods().stream().map(MealFood::getId).findFirst().orElse(null);

        MealFormDTO mealFormDTO = new MealFormDTO();
        mealFormDTO.setMealName("Breakfast");
//        mealFormDTO.setQuantity(BigDecimal.TEN);
        mealFormDTO.setPortionSize(ONE_HUNDRED);

        ResultActions response = mockMvc.perform(put("/api/meal/{mealId}/food/{foodId}", mealId, mealFoodId)
                .header("Authorization", authorizedUser)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(mealFormDTO)));

        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.quantity").value("must not be null"));
    }

    @Test
    @Transactional
    @DisplayName("Integration test positive case for fetch meal by id operation")
    void givenValidInput_whenFetchMealById_thenReturnsMealDto() throws Exception {
        Long mealId = meal.getId();

        ResultActions response = mockMvc.perform(get("/api/meal/{mealId}", mealId)
                .header("Authorization", authorizedUser)
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.mealName").value("Fruit"))
                .andExpect(jsonPath("$.mealId").value(mealId))
                .andExpect(jsonPath("$.foods[0].foodId").value(food.getId()))
                .andExpect(jsonPath("$.foods[0].foodName").value(food.getName()))
                .andExpect(jsonPath("$.foods[0].quantity").value(100.00))
                .andExpect(jsonPath("$.foods[0].adjustedKiloJoules").value(10.00))
                .andExpect(jsonPath("$.foods[0].adjustedFiber").value(0.00))
                .andExpect(jsonPath("$.sumQuantity").value(100.00))
                .andExpect(jsonPath("$.sumAdjustedKiloJoules").value(10.00))
                .andExpect(jsonPath("$.sumAdjustedFiber").value(0.00));
    }

    @Test
    @Transactional
    @DisplayName("Integration test negative case for fetch meal by id operation - invalid mealId")
    void givenInvalidInput_whenFetchMealById_thenThrowsExceptionAndReturnsErrorDto() throws Exception {
        Long mealId = meal.getId() + 1;

        ResultActions response = mockMvc.perform(get("/api/meal/{mealId}", mealId)
                .header("Authorization", authorizedUser)
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Meal record with id " + mealId + " does not exist!"));
    }
}

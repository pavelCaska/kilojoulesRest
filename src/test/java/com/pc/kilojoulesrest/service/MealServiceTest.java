package com.pc.kilojoulesrest.service;

import com.pc.kilojoulesrest.entity.*;
import com.pc.kilojoulesrest.exception.RecordNotFoundException;
import com.pc.kilojoulesrest.model.MealDTO;
import com.pc.kilojoulesrest.model.MealFoodDTO;
import com.pc.kilojoulesrest.model.MealFormDTO;
import com.pc.kilojoulesrest.repository.MealRepository;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.*;

import static com.pc.kilojoulesrest.constant.Constant.ONE_HUNDRED;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MealServiceTest {

    @Mock
    private MealRepository mealRepository;
    @InjectMocks
    private MealServiceImpl mealService;

    @Mock
    private MealFoodServiceImpl mealFoodService;
    @Mock
    private FoodServiceImpl foodService;

    Food food;
    Portion portion1;
    Portion portion100;
    User user;
    MealFood mealFood;
    Meal meal;

    @BeforeEach
    void setUp() {
        food = Food.builder()
                .id(2L)
                .name("Apple")
                .quantity(ONE_HUNDRED)
                .kiloJoules(BigDecimal.TEN)
                .proteins(BigDecimal.TEN)
                .carbohydrates(BigDecimal.TEN)
                .fat(BigDecimal.TEN)
                .build();
        portion1 = Portion.builder()
                .id(1L)
                .portionName("1 g")
                .portionSize(BigDecimal.ONE)
                .food(food)
                .build();
        portion100 = Portion.builder()
                .id(2L)
                .portionName("100 g")
                .portionSize(ONE_HUNDRED)
                .food(food)
                .build();
        List<Portion> portions = new ArrayList<>();
        portions.add(portion1);
        portions.add(portion100);
        food.setPortions(portions);

        user = User.builder()
                .id(1L)
                .username("user1")
                .password("user1pwd")
                .roles("ROLE_USER")
                .build();

        meal = Meal.builder()
                .id(1L)
                .user(user)
                .mealName("Fruit")
                .build();

        mealFood = MealFood.builder()
                .id(1L)
                .quantity(ONE_HUNDRED)
                .food(food)
                .meal(meal)
                .build();

        Set<MealFood> mealFoods = new HashSet<>();
        mealFoods.add(mealFood);
        meal.setMealFoods(mealFoods);
    }

    @Test
    @DisplayName("JUNit test for save operation - positive case")
    void givenMealObject_whenSaveMeal_thenReturnsSavedMeal() {
        given(mealRepository.save(meal)).willReturn(meal);

        Meal savedMeal = mealService.saveMeal(meal);

        assertThat(savedMeal).isNotNull();
        assertThat(savedMeal).isEqualTo(meal);
        verify(mealRepository, times(1)).save(meal);
    }

    @Test
    @DisplayName("JUNit test for fetch Meal by Id operation - positive case")
    void givenValidMealId_whenGetMealById_thenReturnsMeal() {
        Long mealId = meal.getId();
        given(mealRepository.findById(mealId)).willReturn(Optional.ofNullable(meal));

        Meal retrievedMeal = mealService.getMealById(mealId);

        assertThat(retrievedMeal).isNotNull();
        assertThat(retrievedMeal).isEqualTo(meal);
        verify(mealRepository, times(1)).findById(mealId);
    }

    @Test
    @DisplayName("JUNit test for fetch Meal by Id operation - negative case")
    void givenInvalidMealId_whenGetMealById_thenThrowException() {
        Long mealId = 2L;
        given(mealRepository.findById(mealId)).willReturn(Optional.empty());


        RecordNotFoundException exception = assertThrows(RecordNotFoundException.class, () -> mealService.getMealById(mealId));
        assertThat(exception.getMessage()).isEqualTo("Meal record with id " + mealId + " does not exist!");

//        assertThatThrownBy(() -> mealService.getMealById(mealId))
//                .isInstanceOf(RecordNotFoundException.class)
//                .hasMessage("Meal record with id " + mealId + " does not exist!");

        verify(mealRepository, times(1)).findById(mealId);
    }

    @Test
    @DisplayName("JUNit test for delete operation - positive case")
    void givenValidMealId_whenDeleteMealById_thenDeletionIsCalledAndReturnsDeletedMeal() {
        Long mealId = meal.getId();

        given(mealRepository.findById(meal.getId())).willReturn(Optional.ofNullable(meal));
        willDoNothing().given(mealRepository).delete(meal);
        willDoNothing().given(mealFoodService).deleteMealFood(mealFood);

        Meal deletedMeal = mealService.deleteMealById(mealId, user);

        assertThat(deletedMeal).isNotNull();
        assertThat(deletedMeal.getId()).isEqualTo(mealId);
        verify(mealRepository, times(1)).delete(meal);
        verify(mealFoodService, times(1)).deleteMealFood(mealFood);
    }

    @Test
    @DisplayName("JUNit test for delete operation - negative case")
    void givenInvalidMealId_whenDeleteMealById_thenThrowException() {
        Long mealId = meal.getId() + 1;

        RecordNotFoundException exception = assertThrows(RecordNotFoundException.class, () -> mealService.deleteMealById(mealId, user));

        assertThat(exception.getMessage()).isEqualTo("Meal record with id " + mealId + " does not exist!");

        verify(mealRepository, times(0)).delete(meal);
        verify(mealFoodService, times(0)).deleteMealFood(mealFood);
    }

    @Test
    @DisplayName("JUNit test for delete operation - negative case 2")
    void givenUnauthorizedUser_whenDeleteMealById_thenThrowException() {
        Long mealId = meal.getId();
        User unauthorizedUser = User.builder()
                .id(2L)
                .username("unauthorizedUser")
                .roles("ROLE_USER")
                .build();

        given(mealRepository.findById(mealId)).willReturn(Optional.ofNullable(meal));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> mealService.deleteMealById(mealId, unauthorizedUser));

        assertThat(exception.getMessage()).isEqualTo("User does not have permission to delete this meal!");

        verify(mealRepository, times(0)).delete(meal);
        verify(mealFoodService, times(0)).deleteMealFood(mealFood);
    }

    @Test
    @DisplayName("JUNit test for paging operation - positive case")
    void givenValidParam_whenFetchMealsPaged_thenReturnsMealPage() {
        Food kiwi = Food.builder()
                .id(3L)
                .name("Kiwi")
                .quantity(ONE_HUNDRED)
                .kiloJoules(BigDecimal.TEN)
                .proteins(BigDecimal.TEN)
                .carbohydrates(BigDecimal.TEN)
                .fat(BigDecimal.TEN)
                .build();
        List<Portion> portionsKiwi = new ArrayList<>();
        portionsKiwi.add(portion1);
        portionsKiwi.add(portion100);
        kiwi.setPortions(portionsKiwi);

        Meal mealKiwi = Meal.builder()
                .id(2L)
                .user(user)
                .mealName("kiwi")
                .build();

        MealFood mealFoodKiwi = MealFood.builder()
                .id(2L)
                .quantity(BigDecimal.TEN)
                .food(kiwi)
                .meal(mealKiwi)
                .build();

        Set<MealFood> setKiwi = new HashSet<>();
        setKiwi.add(mealFoodKiwi);
        mealKiwi.setMealFoods(setKiwi);

        Food banana = Food.builder()
                .id(4L)
                .name("Banana")
                .quantity(ONE_HUNDRED)
                .kiloJoules(BigDecimal.TEN)
                .proteins(BigDecimal.TEN)
                .carbohydrates(BigDecimal.TEN)
                .fat(BigDecimal.TEN)
                .build();
        List<Portion> portionsBanana = new ArrayList<>();
        portionsBanana.add(portion1);
        portionsBanana.add(portion100);
        banana.setPortions(portionsBanana);

        Meal mealBanana = Meal.builder()
                .id(3L)
                .user(user)
                .mealName("banana")
                .build();

        MealFood mealFoodBanana = MealFood.builder()
                .id(3L)
                .quantity(BigDecimal.TEN)
                .food(banana)
                .meal(mealBanana)
                .build();

        Set<MealFood> setBanana = new HashSet<>();
        setBanana.add(mealFoodBanana);
        mealBanana.setMealFoods(setBanana);

        int page = 0;
        Pageable pageable = PageRequest.of(page, 3, Sort.by(Sort.Direction.DESC, "updatedAt"));
        Page<Meal> mealPage = new PageImpl<>(List.of(meal, mealKiwi, mealBanana));

        given(mealRepository.findAllByUser(user, pageable)).willReturn(mealPage);

        Page<Meal> mealsPaged = mealService.fetchMealsPaged(page, user);

        assertThat(mealsPaged).isNotNull();
        assertThat(mealsPaged.getSize()).isEqualTo(3);
        assertThat(mealsPaged.getContent()).isEqualTo(List.of(meal, mealKiwi, mealBanana));
        verify(mealRepository, times(1)).findAllByUser(user, pageable);
    }

    @Test
    @DisplayName("JUNit test for paging operation - negative case")
    void givenEmptyList_whenFetchMealsPaged_thenReturnsEmptyList() {

        int page = 0;

        given(mealRepository.findAllByUser(any(User.class), any(Pageable.class))).willReturn(Page.empty());

        Page<Meal> mealsPaged = mealService.fetchMealsPaged(page, user);

        assertThat(mealsPaged).isNotNull();
        assertThat(mealsPaged).isEmpty();
        assertThat(mealsPaged.getSize()).isEqualTo(0);
        verify(mealRepository, times(1)).findAllByUser(any(User.class), any(Pageable.class));
    }

    @Test
    @DisplayName("JUNit test for fetch Meal by Id and User operation - positive case")
    void givenValidParam_whenGetMealByIdAndUser_thenReturnsMealObject() {
        Long mealId = meal.getId();
        given(mealRepository.findById(mealId)).willReturn(Optional.ofNullable(meal));

        Meal retrievedMeal = mealService.getMealByIdAndUser(mealId, user);

        assertThat(retrievedMeal).isNotNull();
        assertThat(retrievedMeal).isEqualTo(meal);
        assertThat(retrievedMeal.getUser()).isEqualTo(user);
        verify(mealRepository, times(1)).findById(mealId);
    }

    @Test
    @DisplayName("JUNit test for fetch Meal by Id and User operation - negative case")
    void givenUnauthorizedUser_whenGetMealByIdAndUser_thenThrowsException() {
        Long mealId = meal.getId();
        User unauthorizedUser = User.builder()
                .id(2L)
                .username("unauthorizedUser")
                .roles("ROLE_USER")
                .build();

        given(mealRepository.findById(mealId)).willReturn(Optional.ofNullable(meal));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> mealService.getMealByIdAndUser(mealId, unauthorizedUser));

//        assertThatThrownBy(() -> mealService.getMealByIdAndUser(mealId, unauthorizedUser))
//        .isInstanceOf(IllegalArgumentException.class)
//        .hasMessage("Meal record with " + mealId + " doesn't belong to user " + unauthorizedUser.getUsername() + ".");

        assertThat(exception.getMessage()).isEqualTo("Meal record with " + mealId + " doesn't belong to user " + unauthorizedUser.getUsername() + ".");
        verify(mealRepository, times(1)).findById(mealId);
    }

    @Test
    @DisplayName("JUNit test for create Meal operation - positive case")
    void givenValidParam_whenCreateMeal_thenReturnsCreatedMeal() {
        MealFormDTO mealFormDTO = new MealFormDTO();
        mealFormDTO.setMealName("Breakfast");
        mealFormDTO.setQuantity(BigDecimal.ONE);
        mealFormDTO.setPortionSize(ONE_HUNDRED);

        List<Long> foods = List.of(2L);
        Meal testMeal = Meal.builder()
                .id(2L)
                .mealName(mealFormDTO.getMealName())
                .user(user)
                .build();

        given(foodService.getFoodById(any(Long.class))).willReturn(food);
        given(mealFoodService.saveMealFood(any(MealFood.class))).willAnswer(invocation -> (MealFood) invocation.getArgument(0));
        given(mealRepository.save(any(Meal.class))).willAnswer(invocation -> (Meal) invocation.getArgument(0));

        Meal createdMeal = mealService.createMeal(user, mealFormDTO, foods);

        assertThat(createdMeal).isNotNull();
        assertThat(createdMeal.getMealName()).isEqualTo(testMeal.getMealName());
        assertThat(createdMeal.getMealFoods()).hasSize(1);
        assertThat(createdMeal.getMealFoods())
                .extracting(MealFood::getFood)
                .extracting(Food::getName)
                .contains("Apple");

        verify(mealRepository, times(2)).save(any(Meal.class));
    }
    @Test
    @DisplayName("JUNit test for create Meal operation - negative case")
    void givenEmptyList_whenCreateMeal_thenThrowsException() {
        MealFormDTO mealFormDTO = new MealFormDTO();
        mealFormDTO.setMealName("Breakfast");
        mealFormDTO.setQuantity(BigDecimal.ONE);
        mealFormDTO.setPortionSize(ONE_HUNDRED);

        List<Long> foods = new ArrayList<>();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> mealService.createMeal(user, mealFormDTO, foods));

//        assertThatThrownBy(() -> mealService.createMeal(user, mealFormDTO, foods))
//        .isInstanceOf(IllegalArgumentException.class)
//        .hasMessage("Must provide at least one food!");

        assertThat(exception.getMessage()).isEqualTo("Must provide at least one food!");
        verify(mealRepository, times(0)).save(any(Meal.class));
    }

    @Test
    @DisplayName("JUNit test for add Food to Meal operation - positive case")
    void givenValidParam_whenAddFoodToMeal_thenReturnsMealWithUpdatedSet() {
        Long mealId = meal.getId();

        MealFormDTO mealFormDTO = new MealFormDTO();
        mealFormDTO.setMealName("Breakfast");
        mealFormDTO.setQuantity(BigDecimal.ONE);
        mealFormDTO.setPortionSize(ONE_HUNDRED);

        Food kiwi = Food.builder()
                .id(3L)
                .name("Kiwi")
                .quantity(ONE_HUNDRED)
                .kiloJoules(BigDecimal.TEN)
                .proteins(BigDecimal.TEN)
                .carbohydrates(BigDecimal.TEN)
                .fat(BigDecimal.TEN)
                .build();
        List<Portion> portionsKiwi = new ArrayList<>();
        portionsKiwi.add(portion1);
        portionsKiwi.add(portion100);
        kiwi.setPortions(portionsKiwi);

        List<Long> foods = List.of(3L);

        given(mealRepository.findById(mealId)).willReturn(Optional.ofNullable(meal));
        given(foodService.getFoodById(any(Long.class))).willReturn(kiwi);
        given(mealFoodService.saveMealFood(any(MealFood.class))).willAnswer(invocation -> (MealFood) invocation.getArgument(0));
        given(mealRepository.save(any(Meal.class))).willAnswer(invocation -> (Meal) invocation.getArgument(0));

        Meal updatedMeal = mealService.addFoodToMeal(user, mealId, mealFormDTO, foods);

        assertThat(updatedMeal).isNotNull();
        assertThat(updatedMeal.getMealFoods()).hasSize(2);
        assertThat(updatedMeal.getMealFoods())
                .extracting(MealFood::getFood)
                .extracting(Food::getName)
                .contains("Kiwi");
        verify(mealRepository, times(1)).findById(mealId);
        verify(mealRepository, times(1)).save(any(Meal.class));
    }

    @Test
    @DisplayName("JUNit test for add Food to Meal operation - negative case")
    void givenUnauthorizedUser_whenAddFoodToMeal_thenThrowsException() {
        Long mealId = meal.getId();

        User unauthorizedUser = User.builder()
                .id(2L)
                .username("unauthorizedUser")
                .roles("ROLE_USER")
                .build();

        MealFormDTO mealFormDTO = new MealFormDTO();
        mealFormDTO.setMealName("Breakfast");
        mealFormDTO.setQuantity(BigDecimal.ONE);
        mealFormDTO.setPortionSize(ONE_HUNDRED);

        List<Long> foods = List.of(3L);

        given(mealRepository.findById(mealId)).willReturn(Optional.ofNullable(meal));
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> mealService.addFoodToMeal(unauthorizedUser, mealId, mealFormDTO, foods));

//        assertThatThrownBy(() -> mealService.addFoodToMeal(unauthorizedUser, mealId, mealFormDTO, foods))
//        .isInstanceOf(IllegalArgumentException.class)
//        .hasMessage("User does not have permission to modify this meal!");

        assertThat(exception.getMessage()).isEqualTo("Meal record with 1 doesn't belong to user unauthorizedUser.");
        verify(mealRepository, times(1)).findById(mealId);
        verify(mealRepository, times(0)).save(any(Meal.class));
    }

    @Test
    @DisplayName("JUNit test for add Food to Meal operation - negative case 2")
    void givenEmptyList_whenAddFoodToMeal_thenThrowsException() {
        Long mealId = meal.getId();

        MealFormDTO mealFormDTO = new MealFormDTO();
        mealFormDTO.setMealName("Breakfast");
        mealFormDTO.setQuantity(BigDecimal.ONE);
        mealFormDTO.setPortionSize(ONE_HUNDRED);

        List<Long> foods = new ArrayList<>();

        given(mealRepository.findById(mealId)).willReturn(Optional.ofNullable(meal));
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> mealService.addFoodToMeal(user, mealId, mealFormDTO, foods));

//        assertThatThrownBy(() -> mealService.addFoodToMeal(user, mealId, mealFormDTO, foods))
//        .isInstanceOf(IllegalArgumentException.class)
//        .hasMessage("Must provide at least one food!");

        assertThat(exception.getMessage()).isEqualTo("Must provide at least one food!");
        verify(mealRepository, times(1)).findById(mealId);
        verify(mealRepository, times(0)).save(any(Meal.class));
    }

    @Test
    @DisplayName("JUNit test for update MealName operation - positive case")
    void givenValidParam_whenUpdateMealName_thenReturnsUpdatedMeal() {
        Long mealId = meal.getId();
        String mealName = "Kiwi";

        given(mealRepository.findById(mealId)).willReturn(Optional.ofNullable(meal));
        given(mealRepository.save(any(Meal.class))).willAnswer(invocation -> (Meal) invocation.getArgument(0));

        Meal updatedMeal = mealService.updateMealName(mealId, mealName, user);

        assertThat(updatedMeal).isNotNull();
        assertThat(updatedMeal.getMealName()).isEqualTo(mealName);
        verify(mealRepository, times(1)).findById(mealId);
        verify(mealRepository, times(1)).save(updatedMeal);
    }

    @Test
    @DisplayName("JUNit test for update MealName operation - negative case")
    void givenEmptyString_whenUpdateMealName_thenThrowsException() {
        Long mealId = meal.getId();
        String mealName = "";

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> mealService.updateMealName(mealId, mealName, user));

        assertThat(exception.getMessage()).isEqualTo("Provided mealName is either null, empty, has only whitespaces or its length is greater than 75 characters.");
        verify(mealRepository, times(0)).findById(mealId);
        verify(mealRepository, times(0)).save(any(Meal.class));
    }

    @Test
    @DisplayName("JUNit test for update MealName operation - negative case 2")
    void givenUnauthorizedUser_whenUpdateMealName_thenThrowsException() {
        Long mealId = meal.getId();
        String mealName = "Kiwi";

        User unauthorizedUser = User.builder()
                .id(2L)
                .username("unauthorizedUser")
                .roles("ROLE_USER")
                .build();

        given(mealRepository.findById(mealId)).willReturn(Optional.ofNullable(meal));
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> mealService.updateMealName(mealId, mealName, unauthorizedUser));

        assertThat(exception.getMessage()).isEqualTo("User does not have permission to modify this meal!");
        verify(mealRepository, times(1)).findById(mealId);
        verify(mealRepository, times(0)).save(any(Meal.class));
    }

    @Test
    @DisplayName("JUNit test for updating MealDTO with totals operation - positive case")
    void givenValidParam_whenSumUpMealFoods_thenExpectedValuesCalculated() {
        Food kiwi = Food.builder()
                .id(3L)
                .name("Kiwi")
                .quantity(ONE_HUNDRED)
                .kiloJoules(BigDecimal.TEN)
                .proteins(BigDecimal.TEN)
                .carbohydrates(BigDecimal.TEN)
                .fat(BigDecimal.TEN)
                .build();
        List<Portion> portionsKiwi = new ArrayList<>();
        portionsKiwi.add(portion1);
        portionsKiwi.add(portion100);
        kiwi.setPortions(portionsKiwi);

        MealFood mealFoodKiwi = MealFood.builder()
                .id(2L)
                .quantity(BigDecimal.TEN)
                .food(kiwi)
                .meal(meal)
                .build();

        meal.getMealFoods().add(mealFoodKiwi);

        List<MealFoodDTO> mealFoodDtoList = mealService.calculateAndReturnAdjustedMealFoods(meal);

        MealDTO mealDTO = new MealDTO();

        mealService.sumUpMealFoods(mealDTO, mealFoodDtoList);

        assertAll(
                () -> assertThat(mealDTO.getSumQuantity()).isEqualTo(BigDecimal.valueOf(110)),
                () -> assertThat(mealDTO.getSumAdjustedKiloJoules()).isEqualTo(BigDecimal.valueOf(11)),
                () -> assertThat(mealDTO.getSumAdjustedProteins()).isEqualTo(BigDecimal.valueOf(11)),
                () -> assertThat(mealDTO.getSumAdjustedCarbohydrates()).isEqualTo(BigDecimal.valueOf(11)),
                () -> assertThat(mealDTO.getSumAdjustedFiber()).isEqualTo(BigDecimal.ZERO),
                () -> assertThat(mealDTO.getSumAdjustedFat()).isEqualTo(BigDecimal.valueOf(11))
        );
    }

    @Test
    @DisplayName("JUNit test for complete creation of MealDTO  - positive case")
    void givenValidParam_whenCalculateAndReturnMealDto_thenReturnsDto() {
        Food kiwi = Food.builder()
                .id(3L)
                .name("Kiwi")
                .quantity(ONE_HUNDRED)
                .kiloJoules(BigDecimal.TEN)
                .proteins(BigDecimal.TEN)
                .carbohydrates(BigDecimal.TEN)
                .fat(BigDecimal.TEN)
                .build();
        List<Portion> portionsKiwi = new ArrayList<>();
        portionsKiwi.add(portion1);
        portionsKiwi.add(portion100);
        kiwi.setPortions(portionsKiwi);

        MealFood mealFoodKiwi = MealFood.builder()
                .id(2L)
                .quantity(BigDecimal.TEN)
                .food(kiwi)
                .meal(meal)
                .build();

        meal.getMealFoods().add(mealFoodKiwi);

        MealDTO testMealDTO = mealService.calculateAndReturnMealDto(meal);

        assertThat(testMealDTO).isNotNull();
        assertThat(testMealDTO.getMealName()).isEqualTo("Fruit");
        assertThat(testMealDTO.getMealId()).isEqualTo(1L);
        assertThat(testMealDTO.getFoods()).hasSize(2);
        assertAll(
                () -> assertThat(testMealDTO.getSumQuantity()).isEqualTo(BigDecimal.valueOf(110)),
                () -> assertThat(testMealDTO.getSumAdjustedKiloJoules()).isEqualTo(BigDecimal.valueOf(11)),
                () -> assertThat(testMealDTO.getSumAdjustedProteins()).isEqualTo(BigDecimal.valueOf(11)),
                () -> assertThat(testMealDTO.getSumAdjustedCarbohydrates()).isEqualTo(BigDecimal.valueOf(11)),
                () -> assertThat(testMealDTO.getSumAdjustedFiber()).isEqualTo(BigDecimal.ZERO),
                () -> assertThat(testMealDTO.getSumAdjustedFat()).isEqualTo(BigDecimal.valueOf(11))
        );
    }

    @Test
    @DisplayName("JUNit test for create MealFoodDTOList operation - positive case")
    void givenValidParam_whenCalculateAndReturnAdjustedMealFoods_thenReturnsMealDtoList() {
        Food kiwi = Food.builder()
                .id(3L)
                .name("Kiwi")
                .quantity(ONE_HUNDRED)
                .kiloJoules(BigDecimal.TEN)
                .proteins(BigDecimal.TEN)
                .carbohydrates(BigDecimal.TEN)
                .fat(BigDecimal.TEN)
                .build();
        List<Portion> portionsKiwi = new ArrayList<>();
        portionsKiwi.add(portion1);
        portionsKiwi.add(portion100);
        kiwi.setPortions(portionsKiwi);

        MealFood mealFoodKiwi = MealFood.builder()
                .id(2L)
                .quantity(BigDecimal.TEN)
                .food(kiwi)
                .meal(meal)
                .build();

        meal.getMealFoods().add(mealFoodKiwi);

        List<MealFoodDTO> mealFoodDtoList = mealService.calculateAndReturnAdjustedMealFoods(meal);

        assertThat(mealFoodDtoList).isNotNull();
        assertThat(mealFoodDtoList).hasSize(2);
        assertContainsFood(mealFoodDtoList, "Apple");
        assertContainsFood(mealFoodDtoList, "Kiwi");
    }

    private void assertContainsFood(List<MealFoodDTO> mealFoodDtoList, String foodName) {
        Optional<MealFoodDTO> optionalMealFoodDTO = mealFoodDtoList.stream()
                .filter(mealFoodDTO -> mealFoodDTO.getFoodName().equals(foodName))
                .findFirst();

            assertThat(optionalMealFoodDTO).isPresent();

        if (optionalMealFoodDTO.isPresent()) {
            MealFoodDTO mealFoodDTO = optionalMealFoodDTO.get();
            if(mealFoodDTO.getFoodName().equals("Apple")) {
                assertThat(mealFoodDTO.getQuantity()).isEqualTo(ONE_HUNDRED);
                assertThat(mealFoodDTO.getAdjustedKiloJoules()).isEqualTo(BigDecimal.TEN);
                assertThat(mealFoodDTO.getAdjustedProteins()).isEqualTo(BigDecimal.TEN);
                assertThat(mealFoodDTO.getAdjustedCarbohydrates()).isEqualTo(BigDecimal.TEN);
                assertThat(mealFoodDTO.getAdjustedFiber()).isEqualTo(BigDecimal.ZERO);
                assertThat(mealFoodDTO.getAdjustedFat()).isEqualTo(BigDecimal.TEN);
            }
            if(mealFoodDTO.getFoodName().equals("Kiwi")) {
                assertThat(mealFoodDTO.getQuantity()).isEqualTo(BigDecimal.TEN);
                assertThat(mealFoodDTO.getAdjustedKiloJoules()).isEqualTo(BigDecimal.ONE);
                assertThat(mealFoodDTO.getAdjustedProteins()).isEqualTo(BigDecimal.ONE);
                assertThat(mealFoodDTO.getAdjustedCarbohydrates()).isEqualTo(BigDecimal.ONE);
                assertThat(mealFoodDTO.getAdjustedFiber()).isEqualTo(BigDecimal.ZERO);
                assertThat(mealFoodDTO.getAdjustedFat()).isEqualTo(BigDecimal.ONE);
            }
        }
    }
}
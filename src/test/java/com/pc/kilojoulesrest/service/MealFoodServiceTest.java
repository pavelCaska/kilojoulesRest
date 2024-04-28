package com.pc.kilojoulesrest.service;

import com.pc.kilojoulesrest.entity.*;
import com.pc.kilojoulesrest.exception.RecordNotFoundException;
import com.pc.kilojoulesrest.model.MealFormDTO;
import com.pc.kilojoulesrest.repository.MealFoodRepository;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static com.pc.kilojoulesrest.constant.Constant.ONE_HUNDRED;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MealFoodServiceTest {

    @Mock
    private MealFoodRepository mealFoodRepository;
    @InjectMocks
    private MealFoodServiceImpl mealFoodService;

    @Mock
    private MealServiceImpl mealService;

    private User user;
    private Meal meal;
    private MealFood mealFood;

    @BeforeEach
    void setUp() {
        Food food = Food.builder()
                .id(2L)
                .name("Apple")
                .quantity(ONE_HUNDRED)
                .kiloJoules(BigDecimal.TEN)
                .proteins(BigDecimal.TEN)
                .carbohydrates(BigDecimal.TEN)
                .fat(BigDecimal.TEN)
                .build();
        Portion portion1 = Portion.builder()
                .id(1L)
                .portionName("1 g")
                .portionSize(BigDecimal.ONE)
                .food(food)
                .build();
        Portion portion100 = Portion.builder()
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
    void givenMealFoodObject_whenSaveMealFood_thenReturnsMealFood() {
        given(mealFoodRepository.save(mealFood)).willReturn(mealFood);

        MealFood savedMealFood = mealFoodService.saveMealFood(mealFood);

        assertThat(savedMealFood).isNotNull();
        assertThat(savedMealFood).isEqualTo(mealFood);
        assertThat(savedMealFood.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("JUNit test for delete operation - positive case")
    void givenMealFood_whenDeleteMealFood_thenRepositoryMethodInvokedOnce() {
        willDoNothing().given(mealFoodRepository).delete(mealFood);

        mealFoodService.deleteMealFood(mealFood);

        verify(mealFoodRepository, times(1)).delete(mealFood);
    }

    @Test
    @DisplayName("JUNit test for find MealFood by Id operation - positive case")
    void givenValidMealFoodIdParam_getMealFoodById_thenReturnsMealFoodObject() {
        Long mealFoodId = mealFood.getId();
        given(mealFoodRepository.findById(mealFoodId)).willReturn(Optional.ofNullable(mealFood));

        MealFood retrievedMealFood = mealFoodService.getMealFoodById(mealFoodId);

        assertThat(retrievedMealFood).isNotNull();
        assertThat(retrievedMealFood).isEqualTo(mealFood);
        assertThat(retrievedMealFood.getId()).isEqualTo(mealFoodId);
    }

    @Test
    @DisplayName("JUNit test for find MealFood by Id operation - negative case")
    void givenInvalidId_whenGetMealFoodById_thenThrowException() {
        Long mealFoodId = mealFood.getId() + 1;
        given(mealFoodRepository.findById(mealFoodId)).willReturn(Optional.empty());

        RecordNotFoundException exception = assertThrows(RecordNotFoundException.class, () -> mealFoodService.getMealFoodById(mealFoodId));

        assertThat(exception.getMessage()).isEqualTo("MealFood record with id " + mealFoodId + " does not exist!");

//        assertThatThrownBy(() -> mealFoodService.getMealFoodById(mealFoodId))
//                .isInstanceOf(RecordNotFoundException.class)
//                .hasMessage("MealFood record with id " + mealFoodId + " does not exist!");

        verify(mealFoodRepository, times(1)).findById(mealFoodId);
    }

    @Test
    @DisplayName("JUNit test for delete operation - positive case")
    void givenValidId_whenDeleteMealFoodById_thenMealFoodDeletedAndReturned() {
        Long mealFoodId = mealFood.getId();
        given(mealFoodRepository.findById(mealFoodId)).willReturn(Optional.ofNullable(mealFood));

        MealFood returnedMealFood = mealFoodService.deleteMealFoodById(mealFoodId, user);

        assertThat(returnedMealFood).isNotNull();
        assertThat(returnedMealFood).isEqualTo(mealFood);
        assertThat(returnedMealFood.getId()).isEqualTo(mealFoodId);
        assertThat(returnedMealFood.getMeal().getMealFoods()).hasSize(0);

        verify(mealFoodRepository, times(1)).findById(mealFoodId);
        verify(mealFoodRepository, times(1)).delete(mealFood);
    }

    @Test
    @DisplayName("JUNit test for delete operation - positive case 2")
    void givenUserRoleAdmin_whenDeleteMealFoodById_thenMealFoodDeletedAndReturned() {
        Long mealFoodId = mealFood.getId();
        User admin = new User();
        admin.setId(2L);
        admin.setUsername("admin");
        admin.setPassword("admin1pwd");
        admin.setRoles("ROLE_ADMIN");
        given(mealFoodRepository.findById(mealFoodId)).willReturn(Optional.ofNullable(mealFood));

        MealFood returnedMealFood = mealFoodService.deleteMealFoodById(mealFoodId, admin);

        assertThat(returnedMealFood).isNotNull();
        assertThat(returnedMealFood).isEqualTo(mealFood);
        assertThat(returnedMealFood.getId()).isEqualTo(mealFoodId);
        assertThat(returnedMealFood.getMeal().getMealFoods()).hasSize(0);

        verify(mealFoodRepository, times(1)).findById(mealFoodId);
        verify(mealFoodRepository, times(1)).delete(mealFood);
    }

    @Test
    @DisplayName("JUNit test for delete operation - negative case")
    void givenInvalidId_whenDeleteMealFoodById_thenThrowsException() {
        Long mealFoodId = mealFood.getId() + 1;
        given(mealFoodRepository.findById(mealFoodId)).willReturn(Optional.empty());

        RecordNotFoundException exception = assertThrows(RecordNotFoundException.class, () -> mealFoodService.deleteMealFoodById(mealFoodId, user));

        assertThat(exception.getMessage()).isEqualTo("MealFood record with id " + mealFoodId + " does not exist!");

//        assertThatThrownBy(() -> mealFoodService.deleteMealFoodById(mealFoodId, user))
//                .isInstanceOf(RecordNotFoundException.class)
//                .hasMessage("MealFood record with id " + mealFoodId + " does not exist!");

        verify(mealFoodRepository, times(1)).findById(mealFoodId);
        verify(mealFoodRepository, times(0)).delete(mealFood);
    }

    @Test
    @DisplayName("JUNit test for delete operation - negative case 2")
    void givenUnauthorizedUser_whenDeleteMealFoodById_thenThrowsException() {
        Long mealFoodId = mealFood.getId();
        User unauthorizedUser = new User();
        unauthorizedUser.setId(2L);
        unauthorizedUser.setUsername("unauthorizedUser");
        unauthorizedUser.setPassword("unauthorizedUserPwd");
        unauthorizedUser.setRoles("ROLE_USER");

        given(mealFoodRepository.findById(mealFoodId)).willReturn(Optional.ofNullable(mealFood));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> mealFoodService.deleteMealFoodById(mealFoodId, unauthorizedUser));

        assertThat(exception.getMessage()).isEqualTo("User does not have permission to modify this meal!");

//        assertThatThrownBy(() -> mealFoodService.deleteMealFoodById(mealFoodId, unauthorizedUser))
//                .isInstanceOf(IllegalArgumentException.class)
//                .hasMessage("User does not have permission to modify this meal!");

        verify(mealFoodRepository, times(1)).findById(mealFoodId);
        verify(mealFoodRepository, times(0)).delete(mealFood);
    }

    @Test
    @DisplayName("JUNit test for existsMealFoodByMealIdAndId operation - positive case")
    void givenValidMealIdAndMealFoodId_whenExistsMealFoodByMealIdAndId_thenMealFoodExistsTrue() {
        Long mealId = meal.getId();
        Long mealFoodId = mealFood.getId();
        given(mealFoodRepository.findMealFoodByMealIdAndId(mealId, mealFoodId)).willReturn(Optional.ofNullable(mealFood));

        boolean exists = mealFoodService.existsMealFoodByMealIdAndId(mealId, mealFoodId);

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("JUNit test for existsMealFoodByMealIdAndId operation - positive case")
    void givenInvalidMealId_whenExistsMealFoodByMealIdAndId_thenMealFoodExistsFalse() {
        Long mealId = meal.getId() + 1;
        Long mealFoodId = mealFood.getId();
        given(mealFoodRepository.findMealFoodByMealIdAndId(mealId, mealFoodId)).willReturn(Optional.empty());

        boolean exists = mealFoodService.existsMealFoodByMealIdAndId(mealId, mealFoodId);

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("JUNit test for update operation - positive case")
    void givenValidParam_whenUpdateMealFood_thenMealFoodUpdated() {
        Long mealFoodId = mealFood.getId();

        MealFormDTO mealFormDTO = new MealFormDTO();
        mealFormDTO.setMealName("Breakfast");
        mealFormDTO.setQuantity(BigDecimal.ONE);
        mealFormDTO.setPortionSize(BigDecimal.valueOf(150));

        given(mealFoodRepository.findById(mealFoodId)).willReturn(Optional.ofNullable(mealFood));

        MealFood updatedMealFood = mealFoodService.updateMealFood(mealFoodId, mealFormDTO, user);

        assertThat(updatedMealFood).isNotNull();
        assertThat(updatedMealFood.getMeal().getMealName()).isEqualTo(mealFormDTO.getMealName());
        assertThat(updatedMealFood.getQuantity()).isEqualTo(BigDecimal.valueOf(150));
        assertThat(updatedMealFood.getMeal().getMealFoods()).hasSize(1);
        verify(mealFoodRepository, times(1)).findById(mealFood.getId());
        verify(mealFoodRepository, times(1)).save(updatedMealFood);
    }

    @Test
    @DisplayName("JUNit test for update operation - negative case")
    void givenInvalidMealFoodId_whenUpdateMealFood_thenThrowsException() {
        Long mealFoodId = mealFood.getId() + 1;

        MealFormDTO mealFormDTO = new MealFormDTO();
        mealFormDTO.setMealName("Breakfast");
        mealFormDTO.setQuantity(BigDecimal.ONE);
        mealFormDTO.setPortionSize(BigDecimal.valueOf(150));

        RecordNotFoundException exception = assertThrows(RecordNotFoundException.class, () -> mealFoodService.updateMealFood(mealFoodId, mealFormDTO, user));

        assertThat(exception.getMessage()).isEqualTo("MealFood record with id " + mealFoodId + " does not exist!");
        verify(mealFoodRepository, times(1)).findById(mealFoodId);
        verify(mealFoodRepository, times(0)).save(any(MealFood.class));
    }

    @Test
    @DisplayName("JUNit test for update operation - negative case 2")
    void givenUnauthorizedUser_whenUpdateMealFood_thenThrowsException() {
        Long mealFoodId = mealFood.getId();

        User unauthorizedUser = new User();
        unauthorizedUser.setId(2L);
        unauthorizedUser.setUsername("unauthorizedUser");
        unauthorizedUser.setPassword("unauthorizedUserPwd");
        unauthorizedUser.setRoles("ROLE_USER");

        MealFormDTO mealFormDTO = new MealFormDTO();
        mealFormDTO.setMealName("Breakfast");
        mealFormDTO.setQuantity(BigDecimal.ONE);
        mealFormDTO.setPortionSize(BigDecimal.valueOf(150));

        given(mealFoodRepository.findById(mealFoodId)).willReturn(Optional.ofNullable(mealFood));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> mealFoodService.updateMealFood(mealFoodId, mealFormDTO, unauthorizedUser));

        assertThat(exception.getMessage()).isEqualTo("User does not have permission to modify this meal!");
        verify(mealFoodRepository, times(1)).findById(mealFoodId);
        verify(mealFoodRepository, times(0)).save(any(MealFood.class));
    }
}
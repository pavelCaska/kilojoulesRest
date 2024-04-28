package com.pc.kilojoulesrest.service;

import com.pc.kilojoulesrest.entity.Food;
import com.pc.kilojoulesrest.entity.Portion;
import com.pc.kilojoulesrest.exception.RecordNotFoundException;
import com.pc.kilojoulesrest.model.FoodCreateDto;
import com.pc.kilojoulesrest.model.FoodDto;
import com.pc.kilojoulesrest.model.PortionResponseDTO;
import com.pc.kilojoulesrest.repository.FoodRepository;

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

import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.pc.kilojoulesrest.constant.Constant.ONE_HUNDRED;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FoodServiceTest {

    @Mock
    private FoodRepository foodRepository;
    @InjectMocks
    private FoodServiceImpl foodService;
    @Mock
    private MealFoodService mealFoodService;

    private Portion portion1;
    private Portion portion100;
    private Food food;

    @BeforeEach
    void setUp() {
        food = Food.builder()
                .id(2L)
                .name("Apple")
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
    }

    @Test
    @DisplayName("JUNit test for save operation")
    void givenFoodObject_whenSaveFood_thenRepositorySaveIsCalled() {

        foodService.saveFood(food);

        verify(foodRepository, times(1)).save(food);
    }

    @Test
    @DisplayName("JUNit test for findById operation - positive case")
    void givenFoodId_whenGetFoodById_thenReturnFood() {
        Long id = food.getId();
        given(foodRepository.findById(id)).willReturn(Optional.ofNullable(food));

        Food returnedFood = foodService.getFoodById(id);

        assertThat(returnedFood).isEqualTo(food);
        verify(foodRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("JUNit test for findById operation - negative case")
    void givenNonExistingFoodId_whenGetFoodById_thenThrowException() {
        Long id = food.getId() + 1;
        given(foodRepository.findById(id)).willReturn(Optional.empty());

        RecordNotFoundException exception = assertThrows(RecordNotFoundException.class, () -> foodService.getFoodById(id));
        assertThat(exception.getMessage()).isEqualTo("Food record with id " + id + " does not exist!");

//        assertThatThrownBy(() -> foodService.getFoodById(id))
//                .isInstanceOf(RecordNotFoundException.class)
//                .hasMessage("Food record with id " + id + " does not exist!");
        verify(foodRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("JUNit test for delete operation - positive case")
    void givenFoodId_whenDeleteFoodById_thenDeletionIsCalledAndReturnDeletedFood() {
        Long id = food.getId();
        given(foodRepository.findById(id)).willReturn(Optional.ofNullable(food));

        Food deletedFood = foodService.deleteFoodById(id);

        assertThat(deletedFood).isEqualTo(food);
        verify(foodRepository, times(1)).findById(id);
        verify(foodRepository, times(1)).delete(food);
    }

    @Test
    @DisplayName("JUNit test for delete operation - negative case")
    void givenInvalidFoodId_whenDeleteFoodById_thenThrowsException() {
        Long id = food.getId() + 1;

        RecordNotFoundException exception = assertThrows(RecordNotFoundException.class, () -> foodService.deleteFoodById(id));

//        assertThatThrownBy(() -> foodService.deleteFoodById(id))
//            .isInstanceOf(RecordNotFoundException.class)
//            .hasMessage("Food record with id " + id + " does not exist!");

        assertThat(exception.getMessage()).isEqualTo("Food record with id " + id + " does not exist!");
        verify(foodRepository, times(1)).findById(id);
        verify(foodRepository, times(0)).delete(food);
    }

    @Test
    @DisplayName("JUNit test for update operation - positive case")
    void givenFoodDto_whenUpdateFood_thenUpdatedFieldsOfFoodAreVerified() {
        FoodDto foodDto = new FoodDto();
        foodDto.setId(2L);
        foodDto.setName("Kiwi");
        foodDto.setKiloJoules(BigDecimal.TEN);
        foodDto.setProteins(BigDecimal.ONE);
        foodDto.setCarbohydrates(BigDecimal.ONE);
        foodDto.setFat(BigDecimal.ONE);

        given(foodRepository.findById(foodDto.getId())).willReturn(Optional.ofNullable(food));
        given(foodRepository.save(any(Food.class))).willAnswer(invocation -> invocation.getArgument(0));

        Food updatedFood = foodService.updateFood(foodDto);

        assertAll("Verify all properties",
                () -> assertThat(updatedFood).isNotNull(),
                () -> assertThat(updatedFood.getId()).isEqualTo(foodDto.getId()),
                () -> assertThat(updatedFood.getName()).isEqualTo(foodDto.getName()),
                () -> assertThat(updatedFood.getKiloJoules()).isEqualTo(foodDto.getKiloJoules()),
                () -> assertThat(updatedFood.getProteins()).isEqualTo(foodDto.getProteins()),
                () -> assertThat(updatedFood.getCarbohydrates()).isEqualTo(foodDto.getCarbohydrates()),
                () -> assertThat(updatedFood.getFat()).isEqualTo(foodDto.getFat())
        );

        verify(foodRepository, times(1)).save(Mockito.any(Food.class));
    }

    @Test
    @DisplayName("JUNit test for fetchAllFoods operation - positive case")
    void givenFoodList_whenFetchAllFoods_thenReturnFoodList() {
        Food kiwi = Food.builder()
                .id(3L)
                .name("Kiwi")
                .kiloJoules(BigDecimal.TEN)
                .proteins(BigDecimal.TEN)
                .carbohydrates(BigDecimal.TEN)
                .fat(BigDecimal.TEN)
                .build();
        List<Portion> portionsKiwi = new ArrayList<>();
        portionsKiwi.add(portion1);
        portionsKiwi.add(portion100);
        kiwi.setPortions(portionsKiwi);

        Food banana = Food.builder()
                .id(4L)
                .name("Banana")
                .kiloJoules(BigDecimal.TEN)
                .proteins(BigDecimal.TEN)
                .carbohydrates(BigDecimal.TEN)
                .fat(BigDecimal.TEN)
                .build();
        List<Portion> portionsBanana = new ArrayList<>();
        portionsBanana.add(portion1);
        portionsBanana.add(portion100);
        banana.setPortions(portionsBanana);

        given(foodRepository.findAll()).willReturn(List.of(food, kiwi, banana));

        List<Food> foodList = foodService.fetchAllFoods();

        verify(foodRepository, times(1)).findAll();
        assertThat(foodList).isNotNull();
        assertThat(foodList).hasSize(3);
        assertThat(foodList).containsExactly(food, kiwi, banana);
    }

    @Test
    @DisplayName("JUNit test for getFoodByPage operation - positive case")
    void givenPageableParam_whenGetFoodsByPage_thenReturnsPagedFoods() {
        Food kiwi = Food.builder()
                .id(3L)
                .name("Kiwi")
                .kiloJoules(BigDecimal.TEN)
                .proteins(BigDecimal.TEN)
                .carbohydrates(BigDecimal.TEN)
                .fat(BigDecimal.TEN)
                .build();
        List<Portion> portionsKiwi = new ArrayList<>();
        portionsKiwi.add(portion1);
        portionsKiwi.add(portion100);
        kiwi.setPortions(portionsKiwi);

        Food banana = Food.builder()
                .id(4L)
                .name("Banana")
                .kiloJoules(BigDecimal.TEN)
                .proteins(BigDecimal.TEN)
                .carbohydrates(BigDecimal.TEN)
                .fat(BigDecimal.TEN)
                .build();
        List<Portion> portionsBanana = new ArrayList<>();
        portionsBanana.add(portion1);
        portionsBanana.add(portion100);
        banana.setPortions(portionsBanana);

        int page = 0;
        Page<Food> foodPage = new PageImpl<>(List.of(food, kiwi, banana));
        given(foodRepository.findAll(any(Pageable.class))).willReturn(foodPage);

        Page<Food> foods = foodService.getFoodsByPage(page);

        assertThat(foods).isNotNull();
        assertThat(foods.getSize()).isEqualTo(3);
        assertThat(foods.getContent()).isEqualTo(List.of(food, kiwi, banana));
        verify(foodRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("JUNit test for getFoodByPage operation - negative case")
    void givenEmptyList_whenGetFoodsByPage_thenReturnsEmptyPage() {
        int page = 0;
        given(foodRepository.findAll(any(Pageable.class))).willReturn(Page.empty());

        Page<Food> foods = foodService.getFoodsByPage(page);

        assertThat(foods).isNotNull();
        assertThat(foods).isEmpty();
        assertThat(foods.getSize()).isEqualTo(0);
        verify(foodRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("JUNit test for find Food by Id and return FoodDto operation - positive case")
    void givenFoodDto_whenFetchFoodDtoById_thenReturnsFoodDto() {
        FoodDto foodDto = new FoodDto();
        foodDto.setId(food.getId());
        foodDto.setName(food.getName());
        foodDto.setQuantity(ONE_HUNDRED);
        foodDto.setKiloJoules(food.getKiloJoules());
        foodDto.setProteins(food.getProteins());
        foodDto.setCarbohydrates(food.getCarbohydrates());
        foodDto.setFiber(BigDecimal.ZERO);
        foodDto.setSugar(BigDecimal.ZERO);
        foodDto.setFat(food.getFat());
        foodDto.setSafa(BigDecimal.ZERO);
        foodDto.setTfa(BigDecimal.ZERO);
        foodDto.setCholesterol(BigDecimal.ZERO);
        foodDto.setSodium(BigDecimal.ZERO);
        foodDto.setCalcium(BigDecimal.ZERO);
        foodDto.setPhe(BigDecimal.ZERO);
        foodDto.setCreatedAt(null);
        foodDto.setUpdatedAt(null);
        List<PortionResponseDTO> portionDtos = food.getPortions().stream().map(PortionResponseDTO::fromEntity).collect(Collectors.toList());
        foodDto.setPortions(portionDtos);

        Long foodId = food.getId();

        given(foodRepository.findById(Mockito.anyLong())).willReturn(Optional.ofNullable(food));

        FoodDto returnedFoodDto = foodService.fetchFoodDtoById(foodId);

        assertThat(returnedFoodDto).isNotNull();
        assertThat(returnedFoodDto).isEqualTo(foodDto);
        verify(foodRepository, times(1)).findById(foodId);
    }

    @Test
    @DisplayName("JUNit test for convert Food to FoodDto operation - positive case")
    void givenFoodObject_whenConvertFoodToFoodDto_ReturnsFoodDto() {
        Food apple = new Food();
        apple.setId(1L);
        apple.setName("Apple");
        apple.setQuantity(ONE_HUNDRED);
        apple.setKiloJoules(BigDecimal.TEN);
        apple.setProteins(BigDecimal.TEN);
        apple.setCarbohydrates(BigDecimal.TEN);
        apple.setFiber(BigDecimal.ZERO);
        apple.setSugar(BigDecimal.ZERO);
        apple.setFat(BigDecimal.TEN);
        apple.setSafa(BigDecimal.ZERO);
        apple.setTfa(BigDecimal.ZERO);
        apple.setCholesterol(BigDecimal.ZERO);
        apple.setSodium(BigDecimal.ZERO);
        apple.setCalcium(BigDecimal.ZERO);
        apple.setPhe(BigDecimal.ZERO);
        apple.setCreatedAt(null);
        apple.setUpdatedAt(null);

        List<Portion> portions = new ArrayList<>();
        portions.add(portion1);
        portions.add(portion100);
        apple.setPortions(portions);

        FoodDto convertedFoodDto = foodService.convertFoodToFoodDto(apple);
        List<PortionResponseDTO> convertedPortions = convertedFoodDto.getPortions();
        PortionResponseDTO portionResponseDto1 = convertedPortions.get(0);
        PortionResponseDTO portionResponseDto100 = convertedPortions.get(1);

        assertAll("Verify converted properties",
                () -> assertThat(convertedFoodDto.getId()).isEqualTo(apple.getId()),
                () -> assertThat(convertedFoodDto.getName()).isEqualTo(apple.getName()),
                () -> assertThat(convertedFoodDto.getQuantity()).isEqualTo(apple.getQuantity()),
                () -> assertThat(convertedFoodDto.getKiloJoules()).isEqualTo(apple.getKiloJoules()),
                () -> assertThat(convertedFoodDto.getProteins()).isEqualTo(apple.getProteins()),
                () -> assertThat(convertedFoodDto.getCarbohydrates()).isEqualTo(apple.getCarbohydrates()),
                () -> assertThat(convertedFoodDto.getFiber()).isEqualTo(apple.getFiber()),
                () -> assertThat(convertedFoodDto.getSugar()).isEqualTo(apple.getSugar()),
                () -> assertThat(convertedFoodDto.getFat()).isEqualTo(apple.getFat()),
                () -> assertThat(convertedFoodDto.getSafa()).isEqualTo(apple.getSafa()),
                () -> assertThat(convertedFoodDto.getTfa()).isEqualTo(apple.getTfa()),
                () -> assertThat(convertedFoodDto.getCholesterol()).isEqualTo(apple.getCholesterol()),
                () -> assertThat(convertedFoodDto.getSodium()).isEqualTo(apple.getSodium()),
                () -> assertThat(convertedFoodDto.getCalcium()).isEqualTo(apple.getCalcium()),
                () -> assertThat(convertedFoodDto.getPhe()).isEqualTo(apple.getPhe()),
                () -> assertThat(convertedFoodDto.getCreatedAt()).isNull(),
                () -> assertThat(convertedFoodDto.getUpdatedAt()).isNull(),
                () -> assertThat(convertedFoodDto.getPortions()).hasSize(2)
        );

        assertAll("Verify first portion properties",
                () -> assertThat(portionResponseDto1 .getPortionName()).isEqualTo(portion1.getPortionName()),
                () -> assertThat(portionResponseDto1 .getPortionSize()).isEqualTo(portion1.getPortionSize())
        );
        assertAll("Verify second portion properties",
                () -> assertThat(portionResponseDto100 .getPortionName()).isEqualTo(portion100.getPortionName()),
                () -> assertThat(portionResponseDto100 .getPortionSize()).isEqualTo(portion100.getPortionSize())
        );
    }

    @Test
    @DisplayName("JUNit test for create new Food from FoodCreateDto operation - positive case")
    void givenFoodCreateDto_whenCreateFoodFromDto_thenReturnsFoodObject() {
        FoodCreateDto dto = new FoodCreateDto("Apple", ONE_HUNDRED, ONE_HUNDRED, BigDecimal.TEN, BigDecimal.TEN, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.TEN, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);

        given(foodRepository.save(any(Food.class))).willAnswer(invocation -> invocation.getArgument(0));

        Food convertedFood = foodService.createFoodFromDto(dto);

        assertAll("Verify all properties",
                () -> assertThat(convertedFood).isNotNull(),
                () -> assertThat(convertedFood.getName()).isEqualTo(dto.getName()),
                () -> assertThat(convertedFood.getQuantity()).isEqualTo(dto.getQuantity()),
                () -> assertThat(convertedFood.getKiloJoules()).isEqualTo(dto.getKiloJoules()),
                () -> assertThat(convertedFood.getProteins()).isEqualTo(dto.getProteins()),
                () -> assertThat(convertedFood.getCarbohydrates()).isEqualTo(dto.getCarbohydrates()),
                () -> assertThat(convertedFood.getFiber()).isEqualTo(dto.getFiber()),
                () -> assertThat(convertedFood.getSugar()).isEqualTo(dto.getSugar()),
                () -> assertThat(convertedFood.getFat()).isEqualTo(dto.getFat()),
                () -> assertThat(convertedFood.getSafa()).isEqualTo(dto.getSafa()),
                () -> assertThat(convertedFood.getTfa()).isEqualTo(dto.getTfa()),
                () -> assertThat(convertedFood.getCholesterol()).isEqualTo(dto.getCholesterol()),
                () -> assertThat(convertedFood.getSodium()).isEqualTo(dto.getSodium()),
                () -> assertThat(convertedFood.getCalcium()).isEqualTo(dto.getCalcium()),
                () -> assertThat(convertedFood.getPhe()).isEqualTo(dto.getPhe())
        );
    }

    @Test
    @DisplayName("JUNit test for add Portion to Food operation - positive case")
    void givenBuiltFoodObject_whenAddPortionsToFood_thenReturnsFoodWithPortionList() {
        Food portionlessFood = Food.builder()
                .id(2L)
                .name("Apple")
                .kiloJoules(BigDecimal.TEN)
                .proteins(BigDecimal.TEN)
                .carbohydrates(BigDecimal.TEN)
                .fat(BigDecimal.TEN)
                .build();

        given(foodRepository.save(any(Food.class))).willAnswer(invocation -> invocation.getArgument(0));

        Food returnedFood = foodService.addPortionsToFood(portionlessFood);

        assertThat(returnedFood).isNotNull();
        assertThat(returnedFood.getPortions()).hasSize(2);
        assertThat(returnedFood.getPortions().get(0))
                .extracting(Portion::getPortionName)
                .isEqualTo("1 g");
        assertThat(returnedFood.getPortions().get(0))
                .extracting(Portion::getPortionSize)
                .isEqualTo(BigDecimal.ONE);
        verify(foodRepository, times(1)).save(portionlessFood);
    }

    @Test
    @DisplayName("JUNit test for search Food by String query and return Page operation - positive case")
    void givenQuery_whenSearchFood_thenReturnsPage() {
        Food banana = Food.builder()
                .id(3L)
                .name("Banana")
                .kiloJoules(BigDecimal.TEN)
                .proteins(BigDecimal.TEN)
                .carbohydrates(BigDecimal.TEN)
                .fat(BigDecimal.TEN)
                .build();

        Portion portion = Portion.builder()
                .portionName("1 g")
                .portionSize(BigDecimal.ONE)
                .food(banana)
                .build();

        List<Portion> portions = new ArrayList<>();
        portions.add(portion);

        banana.setPortions(portions);

        Food mango = Food.builder()
                .id(4L)
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

        Pageable pageable = PageRequest.of(0, 25, Sort.Direction.ASC, "name");
        Page<Food> foodPage = new PageImpl<>(List.of(mango));
        String query = "man";

        given(foodRepository.findAllByNameContainsIgnoreCase(query, pageable)).willReturn(foodPage);

        Page<Food> result = foodService.searchFood(query, pageable);

        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Mango");
        verify(foodRepository, times(1)).findAllByNameContainsIgnoreCase(query, pageable);
    }
}
package com.pc.kilojoulesrest.service;

import com.pc.kilojoulesrest.entity.Food;
import com.pc.kilojoulesrest.entity.Portion;
import com.pc.kilojoulesrest.exception.RecordCountException;
import com.pc.kilojoulesrest.exception.RecordNameExistsException;
import com.pc.kilojoulesrest.exception.RecordNotDeletableException;
import com.pc.kilojoulesrest.exception.RecordNotFoundException;
import com.pc.kilojoulesrest.model.PortionRequestDTO;
import com.pc.kilojoulesrest.repository.PortionRepository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.pc.kilojoulesrest.constant.Constant.ONE_HUNDRED;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class PortionServiceTests {

    @Mock
    private PortionRepository portionRepository;
    @InjectMocks
    private PortionServiceImpl portionService;
    @Mock
    private FoodServiceImpl foodService;

    private Portion portion100;
    private Food food;

    @BeforeEach
    void setUp() {
        food = Food.builder()
                .id(1L)
                .name("Apple")
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
    @DisplayName("JUNit test for add Portion to List operation - positive case")
    void givenFoodAndPortion_whenAddPortionToList_thenReturnPortionToList() {
        Portion portion = Portion.builder()
                .portionName("150 g")
                .portionSize(BigDecimal.valueOf(150))
                .food(food)
                .build();

        given(portionRepository.save(portion)).willReturn(portion);
        willDoNothing().given(foodService).saveFood(food);

        Portion savedPortion = portionService.addPortionToList(food, portion);

        assertThat(savedPortion).isNotNull();
        assertThat(savedPortion.getPortionName()).isEqualTo(portion.getPortionName());
        assertThat(savedPortion.getPortionSize()).isEqualTo(portion.getPortionSize());
        assertThat(savedPortion.getFood().getPortions()).hasSize(3);
    }

    @Test
    @DisplayName("JUNit test for create Portion operation - positive case")
    void givenPortionDto_whenCreatePortionRest_thenPortionAddedToListAndReturned() {
        PortionRequestDTO portionRequestDTO = new PortionRequestDTO();
        portionRequestDTO.setPortionName("150 g");
        portionRequestDTO.setPortionSize(BigDecimal.valueOf(150));

        given(portionRepository.save(any(Portion.class))).willAnswer(invocation -> invocation.getArgument(0));
        willDoNothing().given(foodService).saveFood(food);

        Portion savedPortion = portionService.createPortionRest(food, portionRequestDTO);

        assertThat(savedPortion).isNotNull();
        assertThat(savedPortion.getPortionName()).isEqualTo("150 g");
        assertThat(savedPortion.getPortionSize()).isEqualTo(BigDecimal.valueOf(150));
        assertThat(savedPortion.getFood().getPortions()).hasSize(3);
    }

    @Test
    @DisplayName("JUNit test for add Portion to List operation - negative case")
    void givenTooManyPortions_whenAddPortionToList_thenThrowException() {

        PortionRequestDTO portionRequestDTO = new PortionRequestDTO();
        portionRequestDTO.setPortionName("150 g");
        portionRequestDTO.setPortionSize(BigDecimal.valueOf(150));

        given(portionRepository.countPortionByFood(food)).willReturn(9);

        assertThrows(RecordCountException.class, () -> portionService.createPortionRest(food, portionRequestDTO));

        verify(portionRepository, times(1)).countPortionByFood(food);
    }

    @Test
    @DisplayName("JUNit test for create Portion operation - negative case")
    void givenDuplicatedPortionName_whenCreatePortionRest_thenThrowException() {

        PortionRequestDTO portionRequestDTO = new PortionRequestDTO();
        portionRequestDTO.setPortionName("1 g");
        portionRequestDTO.setPortionSize(BigDecimal.valueOf(150));

        RecordNameExistsException exception = assertThrows(RecordNameExistsException.class, () -> portionService.createPortionRest(food, portionRequestDTO));

        assertThat(exception.getMessage()).isEqualTo("Portion name already exists for this food.");
    }

    @Test
    @DisplayName("JUNit test for delete operation - positive case")
    void givenPortionId_whenDeletePortionById_thenRemovePortion() {
        Portion portion = Portion.builder()
                .id(3L)
                .portionName("150 g")
                .portionSize(BigDecimal.valueOf(150))
                .food(food)
                .build();
        Long portionId = portion.getId();

        given(portionRepository.findById(portionId)).willReturn(Optional.ofNullable(portion));
        willDoNothing().given(foodService).saveFood(food);

        portionService.deletePortionById(portionId);

        verify(portionRepository, times(1)).findById(portionId);
        verify(portionRepository, times(1)).delete(any(Portion.class));
    }

    @Test
    @DisplayName("JUNit test for delete operation - negative case")
    void givenInvalidPortionId_whenDeletePortionById_thenThrowException() {
        Long id = 3L;
        given(portionRepository.findById(id)).willReturn(Optional.empty());

        RecordNotFoundException exception = assertThrows(RecordNotFoundException.class, () -> portionService.deletePortionById(id));

        assertThat(exception.getMessage()).isEqualTo("Portion record with id \" + id + \" does not exist!");
    }

    @Test
    @DisplayName("JUNit test for delete operation - negative case 2")
    void givenNonDeletablePortion_whenDeletePortionById_thenThrowException() {
        Long id = 2L;
        given(portionRepository.findById(id)).willReturn(Optional.ofNullable(portion100));

        RecordNotDeletableException exception = assertThrows(RecordNotDeletableException.class, () -> portionService.deletePortionById(id));

        assertThat(exception.getMessage()).isEqualTo("This record cannot be deleted.");
    }

    @Test
    @DisplayName("JUNit test for existsPortionByIdAndFoodId operation - positive case")
    void givenValidFoodIdAndPortionId_whenExistsPortionByIdAndFoodId_thenReturnsTrue() {
        Long foodId = 1L;
        Long portionId = 2L;

        given(portionRepository.findPortionByIdAndFoodId(portionId, foodId)).willReturn(Optional.ofNullable(portion100));

        boolean result = portionService.existsPortionByIdAndFoodId(portionId, foodId);
        assertTrue(result);
    }
}
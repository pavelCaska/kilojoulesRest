package com.pc.kilojoulesrest.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pc.kilojoulesrest.entity.Food;
import com.pc.kilojoulesrest.entity.Portion;
import com.pc.kilojoulesrest.entity.User;
import com.pc.kilojoulesrest.model.PortionRequestDTO;
import com.pc.kilojoulesrest.model.PortionResponseDTO;
import com.pc.kilojoulesrest.service.FoodService;
import com.pc.kilojoulesrest.service.JwtService;
import com.pc.kilojoulesrest.service.PortionService;
import com.pc.kilojoulesrest.service.UserService;
import jakarta.transaction.Transactional;
import static  org.assertj.core.api.Assertions.assertThat;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.pc.kilojoulesrest.constant.Constant.ONE_HUNDRED;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
@ActiveProfiles("test")
class PortionControllerITests {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserService userService;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private FoodService foodService;
    @Autowired
    private PortionService portionService;
    @Autowired
    private ObjectMapper om;

    private Food food;
    private String authorizedUser;

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

        User user = User.builder()
                .username("user for PortionController integration test")
                .password(userService.encodePassword("user1pwd"))
                .roles("ROLE_USER")
                .build();
        userService.saveUser(user);

        authorizedUser = "Bearer " + jwtService.generateToken("user for PortionController integration test");
    }


    @Test
    @Transactional
    @DisplayName("Integration test positive case for get portions by food operation")
    void givenValidFoodId_whenFetchPortionsByFoodId_thenReturnsDtoList() throws Exception {
        Long foodId = food.getId();

        ResultActions response = mockMvc.perform(get("/api/food/{foodId}/portion", foodId)
                        .header("Authorization", authorizedUser)
                        .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(food.getPortions().get(0).getId()))
                .andExpect(jsonPath("$[0].portionName").value("1 g"))
                .andExpect(jsonPath("$[0].portionSize").value(1.00))
                .andExpect(jsonPath("$[1].id").value(food.getPortions().get(1).getId()))
                .andExpect(jsonPath("$[1].portionName").value("100 g"))
                .andExpect(jsonPath("$[1].portionSize").value(100.00));
    }

    @Test
    @Transactional
    @DisplayName("Integration test positive case for get portions by food operation")
    void givenInvalidFoodId_whenFetchPortionsByFoodId_thenThrowsException() throws Exception {
        Long foodId = food.getId() + 1;

        ResultActions response = mockMvc.perform(get("/api/food/{foodId}/portion", foodId)
                        .header("Authorization", authorizedUser)
                        .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Food record with id " + foodId + " does not exist!"));
    }

    @Test
    @Transactional
    @DisplayName("Integration test positive case for create portion operation")
    void givenValidRequestDto_whenCreatePortion_thenPortionCreatedAndReturned() throws Exception {
        Long foodId = food.getId();
        PortionRequestDTO requestDTO = new PortionRequestDTO("větší porce 150 g", BigDecimal.valueOf(150));

        ResultActions resultActions = mockMvc.perform(post("/api/food/{foodId}/portion", foodId)
                .header("Authorization", authorizedUser)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(requestDTO)));

        MvcResult mvcResult = resultActions.andReturn();
        String responseBody = mvcResult.getResponse().getContentAsString();
        PortionResponseDTO responseDto = om.readValue(responseBody, PortionResponseDTO.class);

        resultActions.andExpect(status().isCreated())
                .andExpect(jsonPath("$.portionName").value(requestDTO.getPortionName()))
                .andExpect(jsonPath("$.portionSize").value(requestDTO.getPortionSize()))
                .andExpect(header().string("Location", "/api/food/" + foodId + "/portion/" + responseDto.getId()));
    }

    @Test
    @Transactional
    @DisplayName("Integration test negative case for create portion operation")
    void givenPortionCountExceedsLimit_whenCreatePortion_thenThrowsException() throws Exception {
        Portion portion3 = Portion.builder()
                .portionName("3 g")
                .portionSize(BigDecimal.valueOf(3))
                .food(food)
                .build();
        portionService.savePortion(portion3);
        Portion portion4 = Portion.builder()
                .portionName("4 g")
                .portionSize(BigDecimal.valueOf(4))
                .food(food)
                .build();
        portionService.savePortion(portion4);
        Portion portion5 = Portion.builder()
                .portionName("5 g")
                .portionSize(BigDecimal.valueOf(5))
                .food(food)
                .build();
        portionService.savePortion(portion5);
        Portion portion6 = Portion.builder()
                .portionName("6 g")
                .portionSize(BigDecimal.valueOf(6))
                .food(food)
                .build();
        portionService.savePortion(portion6);
        Portion portion7 = Portion.builder()
                .portionName("7 g")
                .portionSize(BigDecimal.valueOf(7))
                .food(food)
                .build();
        portionService.savePortion(portion7);
        Portion portion8 = Portion.builder()
                .portionName("8 g")
                .portionSize(BigDecimal.valueOf(8))
                .food(food)
                .build();
        portionService.savePortion(portion8);
        Portion portion9 = Portion.builder()
                .portionName("9 g")
                .portionSize(BigDecimal.valueOf(9))
                .food(food)
                .build();
        portionService.savePortion(portion9);
        List<Portion> portions = food.getPortions();
        portions.add(portion3);
        portions.add(portion4);
        portions.add(portion5);
        portions.add(portion6);
        portions.add(portion7);
        portions.add(portion8);
        portions.add(portion9);
        food.setPortions(portions);
        foodService.saveFood(food);

        Long foodId = food.getId();
        PortionRequestDTO requestDTO = new PortionRequestDTO("větší porce 150 g", BigDecimal.valueOf(150));

        ResultActions response = mockMvc.perform(post("/api/food/{foodId}/portion", foodId)
                .header("Authorization", authorizedUser)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(requestDTO)));

        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Maximum allowed portions (9) exceeded. Delete a portion before creating a new one."));
    }

    @Test
    @Transactional
    @DisplayName("Integration test positive case for delete portion operation")
    void givenValidParam_whenDeletePortionById_thenPortionDeletedAndStatusNoContent() throws Exception {
        Portion portion9 = Portion.builder()
                .portionName("9 g")
                .portionSize(BigDecimal.valueOf(9))
                .food(food)
                .build();
        portionService.savePortion(portion9);
        List<Portion> portions = food.getPortions();
        portions.add(portion9);
        food.setPortions(portions);
        foodService.saveFood(food);

        Long foodId = food.getId();
        Long portionId = food.getPortions().get(2).getId();

        ResultActions response = mockMvc.perform(delete("/api/food/{foodId}/portion/{portionId}", foodId, portionId)
                .header("Authorization", authorizedUser)
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(status().isNoContent());

        Optional<Portion> deletedPortion = portionService.fetchPortionById(portionId);
        assertThat(deletedPortion.isEmpty()).isTrue();
    }

    @Test
    @Transactional
    @DisplayName("Integration test negative case for delete portion operation")
    void givenInvalidParam_whenDeletePortionById_thenThrowsRecordNotDeletableException() throws Exception {
        Long foodId = food.getId();
        Long portionId = food.getPortions().get(0).getId();

        ResultActions response = mockMvc.perform(delete("/api/food/{foodId}/portion/{portionId}", foodId, portionId)
                .header("Authorization", authorizedUser)
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("This record cannot be deleted."));

        Optional<Portion> deletedPortion = portionService.fetchPortionById(portionId);
        assertThat(deletedPortion.isPresent()).isTrue();
    }

    @Test
    @Transactional
    @DisplayName("Integration test second negative case for delete portion operation")
    void givenInvalidParam_whenDeletePortionById_thenErrorDto() throws Exception {
        Long foodId = food.getId() + 1;
        Long portionId = food.getPortions().get(0).getId();

        ResultActions response = mockMvc.perform(delete("/api/food/{foodId}/portion/{portionId}", foodId, portionId)
                .header("Authorization", authorizedUser)
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Food record with id " + foodId + " is not associated with Portion record with id " + portionId));

        Optional<Portion> deletedPortion = portionService.fetchPortionById(portionId);
        assertThat(deletedPortion.isPresent()).isTrue();
    }
}
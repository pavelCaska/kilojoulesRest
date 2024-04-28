package com.pc.kilojoulesrest.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pc.kilojoulesrest.entity.Food;
import com.pc.kilojoulesrest.entity.Portion;
import com.pc.kilojoulesrest.entity.User;
import com.pc.kilojoulesrest.exception.RecordNotFoundException;
import com.pc.kilojoulesrest.model.FoodCreateDto;
import com.pc.kilojoulesrest.model.FoodDto;
import com.pc.kilojoulesrest.service.FoodService;
import com.pc.kilojoulesrest.service.JwtService;
import com.pc.kilojoulesrest.service.PortionService;
import com.pc.kilojoulesrest.service.UserService;
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
import java.util.ArrayList;
import java.util.List;

import static com.pc.kilojoulesrest.constant.Constant.ONE_HUNDRED;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest
@ActiveProfiles("test")
public class FoodControllerITests {

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
                .username("user for FoodController integration test")
                .password(userService.encodePassword("user1pwd"))
                .roles("ROLE_USER")
                .build();
        userService.saveUser(user);

        authorizedUser = "Bearer " + jwtService.generateToken("user for FoodController integration test");
    }

    @Test
    @Transactional
    @DisplayName("Integration test positive case for create food operation")
    public void givenValidDto_whenCreateFood_thenCreatesFoodAndReturnsDto() throws Exception {

        FoodCreateDto dto = new FoodCreateDto("Kiwi", ONE_HUNDRED, ONE_HUNDRED, BigDecimal.TEN, BigDecimal.TEN, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.TEN, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);

        ResultActions resultActions = mockMvc.perform(post("/api/food")
                .header("Authorization", authorizedUser)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto)));

        MvcResult mvcResult = resultActions.andReturn();
        String responseBody = mvcResult.getResponse().getContentAsString();
        FoodDto foodDto = om.readValue(responseBody, FoodDto.class);

        resultActions.andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Kiwi"))
                .andExpect(jsonPath("$.quantity").value(100.00))
                .andExpect(jsonPath("$.kiloJoules").value(100.00))
                .andExpect(jsonPath("$.proteins").value(10.00))
                .andExpect(jsonPath("$.carbohydrates").value(10.00))
                .andExpect(jsonPath("$.fiber").value(0.00))
                .andExpect(jsonPath("$.sugar").value(0.00))
                .andExpect(jsonPath("$.fat").value(10.00))
                .andExpect(jsonPath("$.safa").value(0.00))
                .andExpect(jsonPath("$.tfa").value(0.00))
                .andExpect(jsonPath("$.cholesterol").value(0.00))
                .andExpect(jsonPath("$.sodium").value(0.00))
                .andExpect(jsonPath("$.calcium").value(0.00))
                .andExpect(jsonPath("$.phe").value(0.00))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.updatedAt").isNotEmpty())
                .andExpect(jsonPath("$.portions", hasSize(2)))
                .andExpect(jsonPath("$.portions[0].portionName").value("1 g"))
                .andExpect(jsonPath("$.portions[1].portionName").value("100 g"))
                .andExpect(header().string("Location", "/api/food/" + foodDto.getId()))
        ;
    }

    @Test
    @Transactional
    @DisplayName("Integration test negative case for create food operation")
    public void givenInvalidDto_whenCreateFood_thenBindingResult() throws Exception {

        FoodCreateDto dto = new FoodCreateDto("", ONE_HUNDRED, ONE_HUNDRED, BigDecimal.TEN, BigDecimal.TEN, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.TEN, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);

        ResultActions response = mockMvc.perform(post("/api/food")
                .header("Authorization", authorizedUser)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto)));

        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Name cannot be empty."));
    }

    @Test
    @Transactional
    @DisplayName("Integration test positive case for get food by id operation")
    public void givenValidId_whenGetFoodById_thenReturnFoodDto() throws Exception {
        Long foodId = food.getId();

        ResultActions response = mockMvc.perform(get("/api/food/{id}", foodId)
                .header("Authorization", authorizedUser)
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(food.getId()))
                .andExpect(jsonPath("$.name").value(food.getName()))
                .andExpect(jsonPath("$.quantity").value(100.00))
                .andExpect(jsonPath("$.kiloJoules").value(10.00))
                .andExpect(jsonPath("$.proteins").value(10.00))
                .andExpect(jsonPath("$.carbohydrates").value(10.00))
                .andExpect(jsonPath("$.fiber").value(0.00))
                .andExpect(jsonPath("$.sugar").value(0.00))
                .andExpect(jsonPath("$.fat").value(10.00))
                .andExpect(jsonPath("$.safa").value(0.00))
                .andExpect(jsonPath("$.tfa").value(0.00))
                .andExpect(jsonPath("$.cholesterol").value(0.00))
                .andExpect(jsonPath("$.sodium").value(0.00))
                .andExpect(jsonPath("$.calcium").value(0.00))
                .andExpect(jsonPath("$.phe").value(0.00))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.updatedAt").isNotEmpty())
                .andExpect(jsonPath("$.portions", hasSize(2)))
                .andExpect(jsonPath("$.portions[0].portionName").value("1 g"))
                .andExpect(jsonPath("$.portions[1].portionName").value("100 g"));
    }

    @Test
    @Transactional
    @DisplayName("Integration test negative case for get food by id operation")
    public void givenInvalidId_whenGetFoodById_thenReturnFoodDto() throws Exception {
        Long foodId = food.getId() + 1;

        ResultActions response = mockMvc.perform(get("/api/food/{id}", foodId)
                .header("Authorization", authorizedUser)
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Food record with id " + foodId + " does not exist!"));
    }

    @Test
    @Transactional
    @DisplayName("Integration test positive case for update food operation")
    public void givenValidDto_whenUpdateFood_thenUpdateFoodAndReturnFoodDto() throws Exception {
        Long foodId = food.getId();

        FoodDto dto = new FoodDto();
        dto.setId(food.getId());
        dto.setName("Banana");
        dto.setQuantity(ONE_HUNDRED);
        dto.setKiloJoules(ONE_HUNDRED);
        dto.setProteins(BigDecimal.TEN);
        dto.setCarbohydrates(BigDecimal.TEN);
        dto.setFiber(BigDecimal.TEN);
        dto.setSugar(BigDecimal.ONE);
        dto.setFat(BigDecimal.TEN);
        dto.setSafa(BigDecimal.ZERO);
        dto.setTfa(BigDecimal.ZERO);
        dto.setCholesterol(BigDecimal.ZERO);
        dto.setSodium(BigDecimal.ZERO);
        dto.setCalcium(BigDecimal.ZERO);
        dto.setPhe(BigDecimal.ZERO);

        ResultActions response = mockMvc.perform(put("/api/food/{id}", foodId, dto)
                .header("Authorization", authorizedUser)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto)));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(dto.getId()))
                .andExpect(jsonPath("$.name").value(dto.getName()))
                .andExpect(jsonPath("$.quantity").value(100.00))
                .andExpect(jsonPath("$.kiloJoules").value(100.00))
                .andExpect(jsonPath("$.proteins").value(10.00))
                .andExpect(jsonPath("$.carbohydrates").value(10.00))
                .andExpect(jsonPath("$.fiber").value(10.00))
                .andExpect(jsonPath("$.sugar").value(1.00))
                .andExpect(jsonPath("$.fat").value(10.00))
                .andExpect(jsonPath("$.safa").value(0.00))
                .andExpect(jsonPath("$.tfa").value(0.00))
                .andExpect(jsonPath("$.cholesterol").value(0.00))
                .andExpect(jsonPath("$.sodium").value(0.00))
                .andExpect(jsonPath("$.calcium").value(0.00))
                .andExpect(jsonPath("$.phe").value(0.00))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.updatedAt").isNotEmpty())
                .andExpect(jsonPath("$.portions", hasSize(2)))
                .andExpect(jsonPath("$.portions[0].portionName").value("1 g"))
                .andExpect(jsonPath("$.portions[1].portionName").value("100 g"));
    }

    @Test
    @Transactional
    @DisplayName("Integration test negative case for update food operation")
    public void givenInvalidDto_whenUpdateFood_thenBindingResult() throws Exception {
        Long foodId = food.getId();

        FoodDto dto = new FoodDto();
        dto.setId(food.getId());
        dto.setName("");
        dto.setQuantity(ONE_HUNDRED);
        dto.setKiloJoules(ONE_HUNDRED);
//        dto.setProteins(BigDecimal.TEN);
        dto.setCarbohydrates(BigDecimal.TEN);
        dto.setFiber(BigDecimal.TEN);
        dto.setSugar(BigDecimal.ONE);
        dto.setFat(BigDecimal.TEN);
        dto.setSafa(BigDecimal.ZERO);
        dto.setTfa(BigDecimal.ZERO);
        dto.setCholesterol(BigDecimal.ZERO);
        dto.setSodium(BigDecimal.ZERO);
        dto.setCalcium(BigDecimal.ZERO);
        dto.setPhe(BigDecimal.ZERO);

        ResultActions response = mockMvc.perform(put("/api/food/{id}", foodId, dto)
                .header("Authorization", authorizedUser)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto)));

        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Name cannot be empty."))
                .andExpect(jsonPath("$.proteins").value("must not be null"));
    }

    @Test
    @Transactional
    @DisplayName("Integration test positive case for delete food operation")
    public void givenValidId_whenDeleteFood_thenDeletesFoodAndReturnsNoContent() throws Exception {
        Long foodId = food.getId();

        ResultActions response = mockMvc.perform(delete("/api/food/{id}", foodId)
                .header("Authorization", authorizedUser)
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(status().isNoContent());

        RecordNotFoundException exception = assertThrows(RecordNotFoundException.class, () -> foodService.getFoodById(foodId));
        assertThat(exception.getMessage()).isEqualTo("Food record with id " + foodId + " does not exist!");
    }

    @Test
    @Transactional
    @DisplayName("Integration test negative case for delete food operation")
    public void givenInvalidId_whenDeleteFood_thenDeletesFoodAndReturnsNoContent() throws Exception {
        Long foodId = food.getId() + 1;

        ResultActions response = mockMvc.perform(delete("/api/food/{id}", foodId)
                .header("Authorization", authorizedUser)
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Food record with id " + foodId + " does not exist!"));
    }

    @Test
    @Transactional
    @DisplayName("Integration test positive case for food pagination operation")
    public void givenNoParameters_whenFetchFoodsPaged_thenReturnsFoodPagedDto() throws Exception {

        ResultActions response = mockMvc.perform(get("/api/food")
                .header("Authorization", authorizedUser)
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.foodDtoList").isNotEmpty())
                .andExpect(jsonPath("$.pageNumber").value(1))
                .andExpect(jsonPath("$.totalPages").isNotEmpty())
                .andExpect(jsonPath("$.foodDtoList[0].id").value(food.getId()))
                .andExpect(jsonPath("$.foodDtoList[0].name").value(food.getName()))
                .andExpect(jsonPath("$.foodDtoList[0].quantity").value(100.00))
                .andExpect(jsonPath("$.foodDtoList[0].kiloJoules").value(10.00))
                .andExpect(jsonPath("$.foodDtoList[0].proteins").value(10.00))
                .andExpect(jsonPath("$.foodDtoList[0].carbohydrates").value(10.00))
                .andExpect(jsonPath("$.foodDtoList[0].fiber").value(0.00))
                .andExpect(jsonPath("$.foodDtoList[0].sugar").value(0.00))
                .andExpect(jsonPath("$.foodDtoList[0].fat").value(10.00))
                .andExpect(jsonPath("$.foodDtoList[0].safa").value(0.00))
                .andExpect(jsonPath("$.foodDtoList[0].tfa").value(0.00))
                .andExpect(jsonPath("$.foodDtoList[0].cholesterol").value(0.00))
                .andExpect(jsonPath("$.foodDtoList[0].sodium").value(0.00))
                .andExpect(jsonPath("$.foodDtoList[0].calcium").value(0.00))
                .andExpect(jsonPath("$.foodDtoList[0].phe").value(0.00))
                .andExpect(jsonPath("$.foodDtoList[0].createdAt").isNotEmpty())
                .andExpect(jsonPath("$.foodDtoList[0].updatedAt").isNotEmpty())
                .andExpect(jsonPath("$.foodDtoList[0].portions", hasSize(2)))
                .andExpect(jsonPath("$.foodDtoList[0].portions[0].id").value(food.getPortions().get(0).getId()))
                .andExpect(jsonPath("$.foodDtoList[0].portions[0].portionName").value("1 g"))
                .andExpect(jsonPath("$.foodDtoList[0].portions[1].id").value(food.getPortions().get(1).getId()))
                .andExpect(jsonPath("$.foodDtoList[0].portions[1].portionName").value("100 g"));
    }

    @Test
    @Transactional
    @DisplayName("Integration test positive case for food pagination operation")
    public void givenQuery_whenSearchFood_thenReturnsFoodPagedDto() throws Exception {
        Food banana = Food.builder()
                .name("Banana 123")
                .kiloJoules(BigDecimal.TEN)
                .proteins(BigDecimal.TEN)
                .carbohydrates(BigDecimal.TEN)
                .fat(BigDecimal.TEN)
                .build();
        foodService.saveFood(banana);
        Portion portion1Banana = Portion.builder()
                .portionName("1 g")
                .portionSize(BigDecimal.ONE)
                .food(banana)
                .build();
        portionService.savePortion(portion1Banana);

        Portion portion100Banana = Portion.builder()
                .portionName("100 g")
                .portionSize(ONE_HUNDRED)
                .food(banana)
                .build();
        portionService.savePortion(portion100Banana);

        List<Portion> portionsBanana = new ArrayList<>();
        portionsBanana.add(portion1Banana);
        portionsBanana.add(portion100Banana);
        banana.setPortions(portionsBanana);
        foodService.saveFood(banana);

        Food mango = Food.builder()
                .name("Mango 123")
                .kiloJoules(BigDecimal.TEN)
                .proteins(BigDecimal.TEN)
                .carbohydrates(BigDecimal.TEN)
                .fat(BigDecimal.TEN)
                .build();
        foodService.saveFood(mango);
        Portion portion1Mango = Portion.builder()
                .portionName("1 g")
                .portionSize(BigDecimal.ONE)
                .food(mango)
                .build();
        portionService.savePortion(portion1Mango);

        Portion portion100Mango = Portion.builder()
                .portionName("100 g")
                .portionSize(ONE_HUNDRED)
                .food(mango)
                .build();
        portionService.savePortion(portion100Mango);

        List<Portion> portionsMango = new ArrayList<>();
        portionsMango.add(portion1Mango);
        portionsMango.add(portion100Mango);
        mango.setPortions(portionsMango);
        foodService.saveFood(mango);

        String query = "123";

        ResultActions response = mockMvc.perform(get("/api/food/search")
            .param("query", query)
                .header("Authorization", authorizedUser)
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isNotEmpty())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id").value(banana.getId()))
                .andExpect(jsonPath("$.content[0].name").value(banana.getName()))
                .andExpect(jsonPath("$.content[0].quantity").value(100.00))
                .andExpect(jsonPath("$.content[0].proteins").value(10.00))
                .andExpect(jsonPath("$.content[0].portions", hasSize(2)))
                .andExpect(jsonPath("$.content[0].portions[0].id").value(portion1Banana.getId()))
                .andExpect(jsonPath("$.content[0].portions[1].id").value(portion100Banana.getId()))
                .andExpect(jsonPath("$.content[1].id").value(mango.getId()))
                .andExpect(jsonPath("$.content[1].name").value(mango.getName()))
                .andExpect(jsonPath("$.content[1].quantity").value(100.00))
                .andExpect(jsonPath("$.content[1].proteins").value(10.00))
                .andExpect(jsonPath("$.content[1].portions", hasSize(2)))
                .andExpect(jsonPath("$.content[1].portions[0].id").value(portion1Mango.getId()))
                .andExpect(jsonPath("$.content[1].portions[1].id").value(portion100Mango.getId()))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0))
                .andExpect(jsonPath("$.pageable.pageSize").value(25));
    }
}

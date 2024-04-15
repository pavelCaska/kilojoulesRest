package com.pc.kilojoulesrest.controller;

import com.pc.kilojoulesrest.entity.Food;
import com.pc.kilojoulesrest.exception.RecordNotFoundException;
import com.pc.kilojoulesrest.model.ErrorDTO;
import com.pc.kilojoulesrest.model.FoodCreateDto;
import com.pc.kilojoulesrest.model.FoodDto;
import com.pc.kilojoulesrest.model.FoodPagedDto;
import com.pc.kilojoulesrest.service.FoodService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class RestFoodController {

    private final FoodService foodService;
    private static final Logger log = LoggerFactory.getLogger(RestPortionController.class);

    @Autowired
    public RestFoodController(FoodService foodService) {
        this.foodService = foodService;
    }

    @GetMapping({"/food"})
    public ResponseEntity<?> fetchFoodsPaged(@RequestParam(name = "page",  defaultValue = "0") int page){
        page = Math.max(page, 0);
//       for page with defaultValue = "1"
//        page = Math.max(page - 1, 0);


        Page<Food> foodsPage = foodService.getFoodsByPage(page);
        int totalPages = foodsPage.getTotalPages();

        if (page >= totalPages && totalPages != 0) {
            page = totalPages - 1;
            foodsPage = foodService.getFoodsByPage(page);
        } else if (totalPages == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorDTO("No records found."));
        }

        List<FoodDto> foodDtos = foodsPage.getContent().stream()
                .map(FoodDto::fromEntity)
                .collect(Collectors.toList());

        FoodPagedDto foodPagedDto = new FoodPagedDto();
        foodPagedDto.setFoodDtoList(foodDtos);
        foodPagedDto.setPageNumber(page + 1);
        foodPagedDto.setTotalPages(totalPages);

        return ResponseEntity.ok(foodPagedDto);

    }

    @GetMapping("/food/{id}")
    public ResponseEntity<?> getFoodById(@PathVariable Long id) {
        try {
            FoodDto dto = foodService.fetchFoodDtoById(id);
            return ResponseEntity.status(HttpStatus.OK).body(dto);
//            return ResponseEntity.ok(dto);
        } catch (RecordNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorDTO(e.getMessage()));
        } catch (DataAccessException e) {
            log.error("Database access error:", e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(new ErrorDTO(e.getMessage()));
        }
    }
    @PostMapping("/food")
    public ResponseEntity<?> createFood(@Valid @RequestBody FoodCreateDto dto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(foodService.buildErrorResponseForFood(bindingResult));
        }

        Food food = foodService.createFoodFromDto(dto);
        Food savedFood = foodService.addPortionsToFood(food);
        FoodDto foodDto = foodService.convertFoodToFoodDto(savedFood);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", "/api/food/" + food.getId().toString());
        return ResponseEntity.status(HttpStatus.CREATED).headers(headers).body(foodDto);
    }

    @PutMapping("/food/{id}")
    public ResponseEntity<?> updateFood(@PathVariable Long id, @Valid @RequestBody FoodDto foodDto, BindingResult bindingResult) {
        if(!id.equals(foodDto.getId())){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorDTO("Invalid food ID"));
        }
        if (bindingResult.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(foodService.buildErrorResponseForFood(bindingResult));
        }
        try {
            Food updatedFood = foodService.updateFood(foodDto);
            FoodDto updatedFoodDto = foodService.convertFoodToFoodDto(updatedFood);
            return ResponseEntity.ok(updatedFoodDto);
        } catch (RecordNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorDTO(e.getMessage()));
        } catch (DataAccessException e) {
            log.error("Database access error:", e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(new ErrorDTO(e.getMessage()));
        }
    }

    @DeleteMapping("/food/{id}")
    public ResponseEntity<?> deleteFoodById(@PathVariable Long id) {
        try {
            Food deletedFood = foodService.deleteFoodById(id);
//            FoodDto deletedFoodDto = foodService.convertFoodToFoodDto(deletedFood);
//            return ResponseEntity.ok(deletedFoodDto);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RecordNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorDTO(e.getMessage()));
        } catch (DataAccessException e) {
            log.error("Database access error:", e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(new ErrorDTO(e.getMessage()));
        }
    }

    @GetMapping("/food/search")
    public ResponseEntity<?> searchFood(@RequestParam(name = "query") String query,
                                        @PageableDefault(size = 25) Pageable pageable) {
        try {
            Page<Food> foods = foodService.searchFood(query, pageable);
            if (foods.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorDTO("No records found."));
            }
            List<FoodDto> foodDtos = foods.getContent().stream()
                    .map(FoodDto::fromEntity)
                    .collect(Collectors.toList());
            Page<FoodDto> dtoPage = new PageImpl<>(foodDtos, pageable, foods.getTotalElements());
            return ResponseEntity.ok(dtoPage);
        } catch (DataAccessException e) {
            log.error("Database access error:", e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(new ErrorDTO(e.getMessage()));
        }
    }
//    @GetMapping("/food/search")
//    public ResponseEntity<?> searchFood(@RequestParam(name = "query") String query) {
//        try {
//            List<Food> foods = foodService.searchFood(query);
//            if (foods.isEmpty()) {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorDTO("No records found."));
//            }
//            List<FoodDto> foodDtos = foods.stream()
//                    .map(FoodDto::fromEntity)
//                    .collect(Collectors.toList());
//            return ResponseEntity.ok(foodDtos);
//        } catch (DataAccessException e) {
//            log.error("Database access error:", e);
//            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(new ErrorDTO(e.getMessage()));
//        }
//    }
}

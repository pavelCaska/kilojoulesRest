package com.pc.kilojoulesrest.controller;

import com.pc.kilojoulesrest.entity.Meal;
import com.pc.kilojoulesrest.entity.MealFood;
import com.pc.kilojoulesrest.entity.User;
import com.pc.kilojoulesrest.model.ErrorDTO;
import com.pc.kilojoulesrest.model.MealDTO;
import com.pc.kilojoulesrest.model.MealFormDTO;
import com.pc.kilojoulesrest.model.MealPagedDto;
import com.pc.kilojoulesrest.service.MealFoodService;
import com.pc.kilojoulesrest.service.MealService;
import com.pc.kilojoulesrest.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class MealController {

    private final MealService mealService;
    private final UserService userService;
    private final MealFoodService mealFoodService;

    public MealController(MealService mealService, UserService userService, MealFoodService mealFoodService) {
        this.mealService = mealService;
        this.userService = userService;
        this.mealFoodService = mealFoodService;
    }

    @GetMapping("/meal")
    public ResponseEntity<?> fetchMealsPaged(@RequestParam(name = "page",  defaultValue = "0") int page,
                                             @AuthenticationPrincipal UserDetails userDetails) {

        User user = userService.fetchUserByUsername(userDetails.getUsername());

        page = Math.max(page, 0);
//       for page with defaultValue = "1"
//        page = Math.max(page - 1, 0);

        Page<Meal> mealsPage = mealService.fetchMealsPaged(page, user);
        int totalPages = mealsPage.getTotalPages();

        if (page >= totalPages && totalPages != 0) {
            page = totalPages - 1;
            mealsPage = mealService.fetchMealsPaged(page, user);
        } else if (totalPages == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorDTO("No records found."));
        }
        List<MealDTO> meals = mealsPage.getContent().stream()
                .map(mealService::calculateAndReturnMealDto)
                .toList();

        MealPagedDto mealPagedDto = new MealPagedDto();
        mealPagedDto.setMeals(meals);
        mealPagedDto.setPageNumber(page + 1);
        mealPagedDto.setTotalPages(totalPages);

        return ResponseEntity.ok(mealPagedDto);
    }

    @GetMapping("/meal/{id}")
    public ResponseEntity<?> fetchMealById(@PathVariable Long id,
                                           @AuthenticationPrincipal UserDetails userDetails) {
            User user = userService.fetchUserByUsername(userDetails.getUsername());
            Meal meal = mealService.getMealByIdAndUser(id, user);
            MealDTO mealDTO = mealService.calculateAndReturnMealDto(meal);
            return ResponseEntity.ok(mealDTO);
    }

    @PostMapping("/meal")
    public ResponseEntity<?> createMeal(@Valid @RequestBody MealFormDTO mealFormDTO, BindingResult bindingResult,
                                        @RequestParam List<Long> foods,
                                        @AuthenticationPrincipal UserDetails userDetails) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(mealService.buildErrorResponseForMeal(bindingResult));
        }
            User user = userService.fetchUserByUsername(userDetails.getUsername());
            Meal meal = mealService.createMeal(user, mealFormDTO, foods);
            MealDTO mealDTO = mealService.calculateAndReturnMealDto(meal);
            HttpHeaders headers = new HttpHeaders();
            headers.add("Location", "/api/meal/" + meal.getId().toString());
            return ResponseEntity.status(HttpStatus.OK).headers(headers).body(mealDTO);
    }

    @PostMapping("/meal/{id}/add-food")
    public ResponseEntity<?> addFoodToMeal(@PathVariable Long id,
                                           @Valid @RequestBody MealFormDTO mealFormDTO, BindingResult bindingResult,
                                           @RequestParam List<Long> foods,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(mealService.buildErrorResponseForMeal(bindingResult));
        }
            User user = userService.fetchUserByUsername(userDetails.getUsername());
            Meal meal = mealService.addFoodToMeal(user, id, mealFormDTO, foods);
            MealDTO mealDTO = mealService.calculateAndReturnMealDto(meal);
            return ResponseEntity.ok(mealDTO);
    }

    @PatchMapping("/meal/{id}/update-name")
    public ResponseEntity<?> updateMealName(@PathVariable Long id,
                                            @RequestParam String mealName,
                                            @AuthenticationPrincipal UserDetails userDetails) {
            User user = userService.fetchUserByUsername(userDetails.getUsername());
            Meal meal = mealService.updateMealName(id, mealName, user);
            MealDTO mealDTO = mealService.calculateAndReturnMealDto(meal);
            return ResponseEntity.ok(mealDTO);
    }

    @DeleteMapping("/meal/{id}")
    public ResponseEntity<?> deleteMealById(@PathVariable Long id,
                                            @AuthenticationPrincipal UserDetails userDetails) {
            User user = userService.fetchUserByUsername(userDetails.getUsername());
            Meal deletedMeal = mealService.deleteMealById(id, user);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/meal/{mealId}/food/{mealFoodId}")
    public ResponseEntity<?> deleteMealFoodById(@PathVariable Long mealId,
                                                @PathVariable Long mealFoodId,
                                                @AuthenticationPrincipal UserDetails userDetails) {
        if(!mealFoodService.existsMealFoodByMealIdAndId(mealId, mealFoodId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorDTO("Food record with id " + mealFoodId + " is not associated with Meal record with id " + mealId));
        }
            User user = userService.fetchUserByUsername(userDetails.getUsername());
            MealFood deletedMf = mealFoodService.deleteMealFoodById(mealFoodId, user);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/meal/{mealId}/food/{mealFoodId}")
    public ResponseEntity<?> updateMealFood(@PathVariable Long mealId,
                                            @PathVariable Long mealFoodId,
                                            @Valid @RequestBody MealFormDTO mealFormDTO, BindingResult bindingResult,
                                            @AuthenticationPrincipal UserDetails userDetails) {
        if (!mealFoodService.existsMealFoodByMealIdAndId(mealId, mealFoodId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorDTO("Food record with id " + mealFoodId + " is not associated with Meal record with id " + mealId));
        }
        if (bindingResult.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(mealService.buildErrorResponseForMeal(bindingResult));
        }
            User user = userService.fetchUserByUsername(userDetails.getUsername());
            MealFood updatedMealFood = mealFoodService.updateMealFood(mealFoodId, mealFormDTO, user);
            MealDTO mealDTO = mealService.calculateAndReturnMealDto(updatedMealFood.getMeal());
            return ResponseEntity.ok(mealDTO);
    }
}

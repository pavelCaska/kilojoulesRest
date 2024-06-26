package com.pc.kilojoulesrest.controller;

import com.pc.kilojoulesrest.entity.*;
import com.pc.kilojoulesrest.model.ErrorDTO;
import com.pc.kilojoulesrest.model.JournalEntryFoodDto;
import com.pc.kilojoulesrest.model.JournalFoodFormDTO;
import com.pc.kilojoulesrest.model.JournalMealFoodDto;
import com.pc.kilojoulesrest.service.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/journal")
public class JournalFoodController {

    private final JournalFoodService journalFoodService;
    private final JournalFoodPortionService journalFoodPortionService;
    private final FoodService foodService;
    private final JournalService journalService;
    private final UserService userService;

    @Autowired
    public JournalFoodController(JournalFoodService journalFoodService, JournalFoodPortionService journalFoodPortionService, FoodService foodService, JournalService journalService, UserService userService) {
        this.journalFoodService = journalFoodService;
        this.journalFoodPortionService = journalFoodPortionService;
        this.foodService = foodService;
        this.journalService = journalService;
        this.userService = userService;
    }

    @PostMapping("/food/{foodId}")
    public ResponseEntity<?> addFoodToJournal(@PathVariable("foodId") Long foodId,
                                              @Valid @RequestBody JournalFoodFormDTO dto, BindingResult bindingResult,
                                              @AuthenticationPrincipal UserDetails userDetails) {

        if (bindingResult.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(journalFoodService.buildErrorResponseForJournalFood(bindingResult));
        }

        Food food = foodService.getFoodById(foodId);
        User user = userService.fetchUserByUsername(userDetails.getUsername());
        BigDecimal quantity = dto.getQuantity();
        String foodName = dto.getFoodName();
        String mealType = dto.getMealType();
        LocalDate date = LocalDate.parse(dto.getConsumedAt());

        Journal journal = journalService.addFoodToJournal(food, quantity, date, mealType, foodName, user);
        JournalEntryFoodDto journalEntryFoodDto = JournalEntryFoodDto.fromEntity(journal, journal.getJournalFood());
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", "/api/journal/" + journal.getId());

        return ResponseEntity.status(HttpStatus.CREATED).headers(headers).body(journalEntryFoodDto);

//        RecordNotFoundException
//        DataAccessException

    }

    @PutMapping("/{journalId}/food/{journalFoodId}")
    public ResponseEntity<?> updateJournalFood(@PathVariable("journalId") Long journalId,
                                               @PathVariable("journalFoodId") Long journalFoodId,
                                               @Valid @RequestBody JournalFoodFormDTO dto, BindingResult bindingResult,
                                               @AuthenticationPrincipal UserDetails userDetails) {

        if(!journalService.existsJournalById(journalId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorDTO("Journal record with id " + journalId + " not found"));
        }
        User user = userService.fetchUserByUsername(userDetails.getUsername());
        if(!journalService.existsJournalByIdAndUser(journalId, user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorDTO("The journal entry doesn't belong to the current user."));
        }
        if(!journalService.existsJournalByIdAndJournalFoodId(journalId, journalFoodId)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorDTO("Food record with id " + journalFoodId + " is not associated with Journal record with id " + journalId));
        }
        if (bindingResult.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(journalFoodService.buildErrorResponseForJournalFood(bindingResult));
        }
        BigDecimal quantity = dto.getQuantity();
        LocalDate date = LocalDate.parse(dto.getConsumedAt());
        String mealType = dto.getMealType();
        String foodName = dto.getFoodName();
        Journal journal = journalService.updateJournalFood(journalId, quantity, date, mealType, foodName, user);

        JournalEntryFoodDto journalEntryFoodDto = JournalEntryFoodDto.fromEntity(journal, journal.getJournalFood());

        return ResponseEntity.status(HttpStatus.OK).body(journalEntryFoodDto);

//        RecordNotFoundException
//        DataAccessException

    }

    @GetMapping("/{journalId}/food/{journalFoodId}")
    public ResponseEntity<?> getJournalFood(@PathVariable("journalId") Long journalId,
                                            @PathVariable("journalFoodId") Long journalFoodId,
                                            @AuthenticationPrincipal UserDetails userDetails) {
        if(!journalService.existsJournalById(journalId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorDTO("Journal record with id " + journalId + " not found"));
        }
        User user = userService.fetchUserByUsername(userDetails.getUsername());
        if(!journalService.existsJournalByIdAndUser(journalId, user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorDTO("The journal entry doesn't belong to the current user."));
        }
        if (!journalService.existsJournalByIdAndJournalFoodId(journalId, journalFoodId)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorDTO("Food record with id " + journalFoodId + " is not associated with Journal record with id " + journalId));
        }

        Journal journal = journalService.fetchJournalEntryById(journalId);
        JournalEntryFoodDto dto = JournalEntryFoodDto.fromEntity(journal, journal.getJournalFood());
        return ResponseEntity.status(HttpStatus.OK).body(dto);
    }

}

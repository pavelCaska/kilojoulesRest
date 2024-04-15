package com.pc.kilojoulesrest.controller;

import com.pc.kilojoulesrest.entity.Food;
import com.pc.kilojoulesrest.entity.Portion;
import com.pc.kilojoulesrest.exception.RecordCountException;
import com.pc.kilojoulesrest.exception.RecordNotDeletableException;
import com.pc.kilojoulesrest.exception.RecordNotFoundException;
import com.pc.kilojoulesrest.model.ErrorDTO;
import com.pc.kilojoulesrest.model.PortionRequestDTO;
import com.pc.kilojoulesrest.model.PortionResponseDTO;
import com.pc.kilojoulesrest.service.FoodService;
import com.pc.kilojoulesrest.service.PortionService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class RestPortionController {

    private final PortionService portionService;
    private final FoodService foodService;
    private static final Logger log = LoggerFactory.getLogger(RestPortionController.class);

    public RestPortionController(PortionService portionService, FoodService foodService) {
        this.portionService = portionService;
        this.foodService = foodService;
    }
    @GetMapping("/food/{foodId}/portion")
    public ResponseEntity<?> fetchPortionsByFoodId(@PathVariable Long foodId) {
        try {
            Food food = foodService.getFoodById(foodId);
            List<PortionResponseDTO> portionResponseDTOList = food.getPortions().stream()
                    .map(o -> new PortionResponseDTO(o.getId(), o.getFood().getId(), o.getPortionName(), o.getPortionSize()))
                    .toList();
            return ResponseEntity.ok(portionResponseDTOList);
        } catch (RecordNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorDTO(e.getMessage()));
        } catch (DataAccessException e) {
            log.error("Database access error:", e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(new ErrorDTO(e.getMessage()));
        }
    }

    @PostMapping("/food/{foodId}/portion")
    public ResponseEntity<?> createPortion(@PathVariable Long foodId, @Valid @RequestBody PortionRequestDTO portionRequestDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(portionService.buildErrorResponseForPortionCreation(bindingResult));
        }
        try {
            Food food = foodService.getFoodById(foodId);
            Portion portion = portionService.createPortionRest(food, portionRequestDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(new PortionResponseDTO(portion.getId(), portion.getFood().getId(), portion.getPortionName(), portion.getPortionSize()));
        } catch (RecordCountException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorDTO(e.getMessage()));
        } catch (RecordNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorDTO(e.getMessage()));
        } catch (DataAccessException e) {
            log.error("Database access error:", e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(new ErrorDTO(e.getMessage()));
        }
    }

    @DeleteMapping("/food/{foodId}/portion/{portionId}/delete")
    public ResponseEntity<?> deletePortionById(@PathVariable Long foodId, @PathVariable Long portionId) {
        if(!portionService.existsPortionByIdAndFoodId(portionId, foodId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorDTO("Food record with id " + foodId + " is not associated with Portion record with id " + portionId));
        }
        try {
            portionService.deletePortionById(portionId);
            return ResponseEntity.noContent().build();
        } catch (RecordNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorDTO(e.getMessage()));
        } catch (RecordNotDeletableException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorDTO(e.getMessage()));
        } catch (DataAccessException e) {
            log.error("Database access error:", e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(new ErrorDTO(e.getMessage()));
        }
    }
}

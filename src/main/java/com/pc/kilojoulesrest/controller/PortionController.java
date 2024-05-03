package com.pc.kilojoulesrest.controller;

import com.pc.kilojoulesrest.entity.Food;
import com.pc.kilojoulesrest.entity.Portion;
import com.pc.kilojoulesrest.model.ErrorDTO;
import com.pc.kilojoulesrest.model.PortionRequestDTO;
import com.pc.kilojoulesrest.model.PortionResponseDTO;
import com.pc.kilojoulesrest.service.FoodService;
import com.pc.kilojoulesrest.service.PortionService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class PortionController {

    private final PortionService portionService;
    private final FoodService foodService;
    private static final Logger log = LoggerFactory.getLogger(PortionController.class);

    public PortionController(PortionService portionService, FoodService foodService) {
        this.portionService = portionService;
        this.foodService = foodService;
    }
    @GetMapping("/food/{foodId}/portion")
    public ResponseEntity<?> fetchPortionsByFoodId(@PathVariable Long foodId) {
            Food food = foodService.getFoodById(foodId);
            List<PortionResponseDTO> portionResponseDTOList = portionService.convertFoodToPortionResponseDtoList(food);
            return ResponseEntity.ok(portionResponseDTOList);
    }

    @PostMapping("/food/{foodId}/portion")
    public ResponseEntity<?> createPortion(@PathVariable Long foodId, @Valid @RequestBody PortionRequestDTO portionRequestDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(portionService.buildErrorResponseForPortionCreation(bindingResult));
        }
            Food food = foodService.getFoodById(foodId);
            Portion portion = portionService.createPortionRest(food, portionRequestDTO);
            HttpHeaders headers = new HttpHeaders();
            headers.add("Location", "/api/food/" + foodId + "/portion/" + portion.getId());
            return ResponseEntity.status(HttpStatus.CREATED).headers(headers).body(PortionResponseDTO.fromEntity(portion));
    }

    @DeleteMapping("/food/{foodId}/portion/{portionId}")
    public ResponseEntity<?> deletePortionById(@PathVariable Long foodId, @PathVariable Long portionId) {
        if(!portionService.existsPortionByIdAndFoodId(portionId, foodId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorDTO("Food record with id " + foodId + " is not associated with Portion record with id " + portionId));
        }
            portionService.deletePortionById(portionId);
            return ResponseEntity.noContent().build();
    }
}

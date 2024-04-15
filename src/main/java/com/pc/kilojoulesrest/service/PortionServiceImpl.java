package com.pc.kilojoulesrest.service;

import com.pc.kilojoulesrest.entity.Food;
import com.pc.kilojoulesrest.entity.Portion;
import com.pc.kilojoulesrest.exception.RecordCountException;
import com.pc.kilojoulesrest.exception.RecordNameExistsException;
import com.pc.kilojoulesrest.exception.RecordNotDeletableException;
import com.pc.kilojoulesrest.exception.RecordNotFoundException;
import com.pc.kilojoulesrest.model.PortionRequestDTO;
import com.pc.kilojoulesrest.repository.PortionRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PortionServiceImpl implements PortionService {

    private final PortionRepository portionRepository;
    private final FoodService foodService;

    @Autowired
    public PortionServiceImpl(PortionRepository portionRepository, FoodService foodService) {
        this.portionRepository = portionRepository;
        this.foodService = foodService;
    }


    @Override
    public Portion createPortion(Food food, Portion portion) throws RecordNameExistsException {
        List<Portion> portionList = food.getPortions();
        for (Portion item : portionList) {
            if(item.getPortionName().equals(portion.getPortionName())) {
                throw new RecordNameExistsException("Portion name already exists for this food.");
            }
        }
        Portion newPortion = Portion.builder()
                .portionName(portion.getPortionName())
                .portionSize(portion.getPortionSize())
                .food(food)
                .build();
        portionRepository.save(newPortion);
        portionList.add(newPortion);
        food.setPortions(portionList);
        foodService.saveFood(food);
        return newPortion;
    }

    @Override
    public Portion createPortionRest(Food food, PortionRequestDTO portionRequestDTO) throws RecordNameExistsException {
        if (portionRepository.countPortionByFood(food) >= 9) {
            throw new RecordCountException("Maximum allowed portions (9) exceeded. Delete a portion before creating a new one.");
        }
        List<Portion> portionList = food.getPortions();
        for (Portion item : portionList) {
            if(item.getPortionName().equals(portionRequestDTO.getPortionName())) {
                throw new RecordNameExistsException("Portion name already exists for this food.");
            }
        }
        Portion newPortion = Portion.builder()
                .portionName(portionRequestDTO.getPortionName())
                .portionSize(portionRequestDTO.getPortionSize())
                .food(food)
                .build();
        portionRepository.save(newPortion);
        portionList.add(newPortion);
        food.setPortions(portionList);
        foodService.saveFood(food);
        return newPortion;
    }

    @Override
    public void deletePortionById(Long id) throws RecordNotDeletableException {
        Portion portion = portionRepository.findById(id).orElseThrow(()-> new RecordNotFoundException("Portion record with id \" + id + \" does not exist!"));
        if(portion.getPortionName().equals("100 g") || portion.getPortionName().equals("1 g")) {
            throw new RecordNotDeletableException("This record cannot be deleted.");
        }
        List<Portion> portionList = portion.getFood().getPortions();
        portionList.remove(portion);
        portion.getFood().setPortions(portionList);
        foodService.saveFood(portion.getFood());
        portionRepository.delete(portion);
    }

    @Override
    public Map<String, String> buildErrorResponseForPortionCreation(BindingResult bindingResult) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return errors;
    }

    @Override
    public boolean existsPortionByIdAndFoodId(Long portionId, Long foodId) {
        return portionRepository.findPortionByIdAndFoodId(portionId, foodId).isPresent();
    }

}

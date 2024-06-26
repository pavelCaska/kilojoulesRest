package com.pc.kilojoulesrest.service;

import com.pc.kilojoulesrest.entity.Food;
import com.pc.kilojoulesrest.entity.Portion;
import com.pc.kilojoulesrest.exception.RecordNotDeletableException;
import com.pc.kilojoulesrest.exception.RecordNotFoundException;
import com.pc.kilojoulesrest.model.FoodCreateDto;
import com.pc.kilojoulesrest.model.FoodDto;
import com.pc.kilojoulesrest.repository.FoodRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.pc.kilojoulesrest.constant.Constant.ONE_HUNDRED;

@Service
public class FoodServiceImpl implements FoodService {
    private final FoodRepository foodRepository;
    private final MealFoodService mealFoodService;

    @Autowired
    public FoodServiceImpl(FoodRepository foodRepository, @Lazy MealFoodService mealFoodService) {
        this.foodRepository = foodRepository;
        this.mealFoodService = mealFoodService;
    }

    @Override
    public List<Food> fetchAllFoods() {
        return foodRepository.findAll();
    }

    @Override
    public Page<Food> getFoodsByPage(int page) {
        Pageable pageable = PageRequest.of(page, 3, Sort.by(Sort.Direction.DESC, "updatedAt"));
        return foodRepository.findAll(pageable);
    }

    @Override
    public Food getFoodById(Long id) {
        return foodRepository.findById(id).orElseThrow(() -> new RecordNotFoundException("Food record with id " + id + " does not exist!"));
    }
    @Override
    public FoodDto fetchFoodDtoById(Long id) {
        return foodRepository.findById(id)
                .map(FoodDto::fromEntity)
                .orElseThrow(() -> new RecordNotFoundException("Food record with id " + id + " does not exist!"));
    }
    @Override
    public FoodDto convertFoodToFoodDto(Food food) {
        return FoodDto.fromEntity(food);
    }

    @Override
    public Food createFoodFromDto(FoodCreateDto dto) {
        Food.FoodBuilder builder = Food.builder()
                .name(dto.getName())
                .quantity(ONE_HUNDRED)
                .kiloJoules(dto.getKiloJoules())
                .proteins(dto.getProteins())
                .carbohydrates(dto.getCarbohydrates())
                .fat(dto.getFat());

        if(dto.getFiber() != null) {
            builder.fiber(dto.getFiber());
        }
        if (dto.getSugar() != null) {
            builder.sugar(dto.getSugar());
        }
        if (dto.getSafa() != null) {
            builder.safa(dto.getSafa());
        }
        if (dto.getTfa() != null) {
            builder.tfa(dto.getTfa());
        }
        if (dto.getCholesterol() != null) {
            builder.cholesterol(dto.getCholesterol());
        }
        if (dto.getSodium() != null) {
            builder.sodium(dto.getSodium());
        }
        if (dto.getCalcium() != null) {
            builder.calcium(dto.getCalcium());
        }
        if (dto.getPhe() != null) {
            builder.phe(dto.getPhe());
        }

        Food food = builder.build();
        return foodRepository.save(food);
    }

    @Override
    public Map<String, String> buildErrorResponseForFood(BindingResult bindingResult) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return errors;
    }


    @Override
    public void saveFood(Food food) {
        foodRepository.save(food);
    }

    @Override
    public Food updateFood(FoodDto foodDto) {
        Food existingFood = foodRepository.findById(foodDto.getId()).orElseThrow(() -> new RecordNotFoundException("Food record with id " + foodDto.getId() + " does not exist!"));
        BeanUtils.copyProperties(foodDto, existingFood, new String[] {"id", "quantity", "createdAt", "updatedAt", "portions"});
        return foodRepository.save(existingFood);
    }

    @Override
    @Transactional
    public Food addPortionsToFood(Food food) {

        List<Portion> portions = new ArrayList<>();
        Portion portion1 = new Portion("1 g", BigDecimal.ONE, food);
        Portion portion100 = new Portion("100 g", ONE_HUNDRED, food);
        portions.add(portion1);
        portions.add(portion100);
        food.setPortions(portions);
        return foodRepository.save(food);
    }

    @Override
    @Transactional
    public Food deleteFoodById(Long id) {
        if(mealFoodService.isFoodAssociatedToMealFood(id)) {
            throw new RecordNotDeletableException("Food with id: " + id + " is associated to a meal and cannot be deleted.");
        }
        Food food = foodRepository.findById(id).orElseThrow(() -> new RecordNotFoundException("Food record with id " + id + " does not exist!"));
        foodRepository.delete(food);
        return food;
    }

    @Override
    public Page<Food> searchFood(String query, Pageable pageable) {
//        if (query == null || query.length() < 3) {
//            throw new IllegalArgumentException("Query must contain at least 3 characters!");
//        }
        return foodRepository.findAllByNameContainsIgnoreCase(query, pageable);
    }
}

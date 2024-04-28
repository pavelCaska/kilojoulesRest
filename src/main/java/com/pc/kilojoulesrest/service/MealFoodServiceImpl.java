package com.pc.kilojoulesrest.service;

import com.pc.kilojoulesrest.entity.Meal;
import com.pc.kilojoulesrest.entity.MealFood;
import com.pc.kilojoulesrest.entity.User;
import com.pc.kilojoulesrest.exception.RecordNotFoundException;
import com.pc.kilojoulesrest.model.MealFormDTO;
import com.pc.kilojoulesrest.repository.MealFoodRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class MealFoodServiceImpl implements MealFoodService {

    private final MealFoodRepository mealFoodRepository;
    private final MealService mealService;

    @Autowired
    public MealFoodServiceImpl(MealFoodRepository mealFoodRepository, MealService mealService) {
        this.mealFoodRepository = mealFoodRepository;
        this.mealService = mealService;
    }

    @Override
    public MealFood saveMealFood(MealFood mealFood) {
        return mealFoodRepository.save(mealFood);
    }

    @Override
    @Transactional
    public void deleteMealFood(MealFood mealFood){
        mealFoodRepository.delete(mealFood);
    }

    @Override
    public MealFood getMealFoodById(Long id) {
        return mealFoodRepository.findById(id).orElseThrow(() -> new RecordNotFoundException("MealFood record with id " + id + " does not exist!"));
    }

    @Override
    @Transactional
    public MealFood deleteMealFoodById(Long id, User user) {
        MealFood mf = mealFoodRepository.findById(id).orElseThrow(()-> new RecordNotFoundException("MealFood record with id " + id + " does not exist!"));
        Meal meal = mf.getMeal();
        if(!user.getId().equals(meal.getUser().getId()) && !user.getRoles().contains("ROLE_ADMIN")) {
            throw new IllegalArgumentException("User does not have permission to modify this meal!");
        }
        meal.getMealFoods().remove(mf);
        mealService.saveMeal(meal);
        mealFoodRepository.delete(mf);
        return mf;
    }

    @Override
    public boolean existsMealFoodByMealIdAndId(Long mealId, Long id) {
        return mealFoodRepository.findMealFoodByMealIdAndId(mealId, id).isPresent();
    }

    @Override
    @Transactional
    public MealFood updateMealFood(Long mealFoodId, MealFormDTO mealFormDTO, User user) {
        MealFood mf = getMealFoodById(mealFoodId);
        Meal meal = mf.getMeal();

        if (!user.getId().equals(meal.getUser().getId()) && !user.getRoles().contains("ROLE_ADMIN")) {
            throw new IllegalArgumentException("User does not have permission to modify this meal!");
        }

        BigDecimal savedQuantity = mealFormDTO.getQuantity().multiply(mealFormDTO.getPortionSize());

        mf.setQuantity(savedQuantity);
        meal.setMealName(mealFormDTO.getMealName());
        meal.getMealFoods().removeIf(mealFood -> mealFood.getId().equals(mf.getId()));
        meal.getMealFoods().add(mf);

        mealFoodRepository.save(mf);
        mealService.saveMeal(meal);
        return mf;
    }

    @Override
    public boolean isFoodAssociatedToMealFood(Long foodId) {
        List<MealFood> mealFoodList = mealFoodRepository.findMealFoodByFoodId (foodId);
        return !mealFoodList.isEmpty();
    }

}

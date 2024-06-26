package com.pc.kilojoulesrest.service;

import com.pc.kilojoulesrest.entity.*;
import com.pc.kilojoulesrest.exception.RecordNotFoundException;
import com.pc.kilojoulesrest.repository.JournalMealRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.pc.kilojoulesrest.constant.Constant.ONE_HUNDRED;

@Service
public class JournalMealServiceImpl implements JournalMealService {

    private final JournalMealRepository journalMealRepository;
    private final JournalService journalService;
    private final JournalMealFoodService journalMealFoodService;
    private final JournalMealFoodPortionService journalMealFoodPortionService;

    public JournalMealServiceImpl(JournalMealRepository journalMealRepository, JournalService journalService, JournalMealFoodService journalMealFoodService, JournalMealFoodPortionService journalMealFoodPortionService) {
        this.journalMealRepository = journalMealRepository;
        this.journalService = journalService;
        this.journalMealFoodService = journalMealFoodService;
        this.journalMealFoodPortionService = journalMealFoodPortionService;
    }

    @Override
    public Map<String, String> buildErrorResponseForJournalMeal(BindingResult bindingResult) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return errors;
    }

    @Override
    @Transactional
    public JournalMeal convertMealToJournalMeal(Meal meal) {

        JournalMeal journalMeal = JournalMeal.builder()
                .mealName(meal.getMealName())
                .saved(true)
                .build();
        journalMealRepository.save(journalMeal);

        Set<JournalMealFood> jmfSet = meal.getMealFoods().stream()
                .map(mealFood -> createJournalMealFoodFromMealFood(mealFood, journalMeal))
                .collect(Collectors.toSet());

        journalMeal.setJournalMealFoods(jmfSet);
        calculateAndSetTotalFieldsFromSet(journalMeal, jmfSet);

        return journalMealRepository.save(journalMeal);
    }

    private JournalMealFood createJournalMealFoodFromMealFood(MealFood mealFood, JournalMeal journalMeal) {
        JournalMealFood jmf = new JournalMealFood();
        BigDecimal quantity = mealFood.getQuantity();
        Food food = mealFood.getFood();
        jmf.setName(food.getName());
        jmf.setQuantity(quantity);
        jmf.setKiloJoules(food.getKiloJoules().multiply(quantity).divide(ONE_HUNDRED, RoundingMode.HALF_UP));
        jmf.setProteins(food.getProteins().multiply(quantity).divide(ONE_HUNDRED, RoundingMode.HALF_UP));
        jmf.setCarbohydrates(food.getCarbohydrates().multiply(quantity).divide(ONE_HUNDRED, RoundingMode.HALF_UP));
        jmf.setSugar(food.getSugar().multiply(quantity).divide(ONE_HUNDRED, RoundingMode.HALF_UP));
        jmf.setFiber(food.getFiber().multiply(quantity).divide(ONE_HUNDRED, RoundingMode.HALF_UP));
        jmf.setFat(food.getFat().multiply(quantity).divide(ONE_HUNDRED, RoundingMode.HALF_UP));
        jmf.setSafa(food.getSafa().multiply(quantity).divide(ONE_HUNDRED, RoundingMode.HALF_UP));
        jmf.setTfa(food.getTfa().multiply(quantity).divide(ONE_HUNDRED, RoundingMode.HALF_UP));
        jmf.setCholesterol(food.getCholesterol().multiply(quantity).divide(ONE_HUNDRED, RoundingMode.HALF_UP));
        jmf.setSodium(food.getSodium().multiply(quantity).divide(ONE_HUNDRED, RoundingMode.HALF_UP));
        jmf.setCalcium(food.getCalcium().multiply(quantity).divide(ONE_HUNDRED, RoundingMode.HALF_UP));
        jmf.setPhe(food.getPhe().multiply(quantity).divide(ONE_HUNDRED, RoundingMode.HALF_UP));
        jmf.setJournalMeal(journalMeal);
        jmf = journalMealFoodService.saveJournalMealFood(jmf);
        final JournalMealFood finalJmf = jmf;

        List<JournalMealFoodPortion> portionList = food.getPortions().stream().map(portion -> {
            JournalMealFoodPortion jmfp = JournalMealFoodPortion.builder()
                    .portionName(portion.getPortionName())
                    .portionSize(portion.getPortionSize())
                    .journalMealFood(finalJmf)
                    .build();
            journalMealFoodPortionService.saveJmfp(jmfp);
            return jmfp;
        }).collect(Collectors.toList());
        jmf.setPortions(portionList);
        return journalMealFoodService.saveJournalMealFood(jmf);
    }

    @Override
    public void calculateAndSetTotalFieldsFromSet(JournalMeal journalMeal, Set<JournalMealFood> jmfSet) {
        journalMeal.setQuantity(totalField(jmfSet, JournalMealFood::getQuantity));
        journalMeal.setKiloJoules(totalField(jmfSet, JournalMealFood::getKiloJoules));
        journalMeal.setProteins(totalField(jmfSet, JournalMealFood::getProteins));
        journalMeal.setCarbohydrates(totalField(jmfSet, JournalMealFood::getCarbohydrates));
        journalMeal.setFiber(totalField(jmfSet, JournalMealFood::getFiber));
        journalMeal.setFat(totalField(jmfSet, JournalMealFood::getFat));
        journalMeal.setSugar(totalField(jmfSet, JournalMealFood::getSugar));
        journalMeal.setSafa(totalField(jmfSet, JournalMealFood::getSafa));
        journalMeal.setTfa(totalField(jmfSet, JournalMealFood::getTfa));
        journalMeal.setCholesterol(totalField(jmfSet, JournalMealFood::getCholesterol));
        journalMeal.setSodium(totalField(jmfSet, JournalMealFood::getSodium));
        journalMeal.setCalcium(totalField(jmfSet, JournalMealFood::getCalcium));
        journalMeal.setPhe(totalField(jmfSet, JournalMealFood::getPhe));
    }

    private BigDecimal totalField(Set<JournalMealFood> jmSet, Function<JournalMealFood, BigDecimal> function) {
        return jmSet.stream().map(function).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public JournalMeal fetchJournalMealById(Long id) {
        return journalMealRepository.findById(id).orElseThrow(()-> new RecordNotFoundException("Meal record with id " + id + " does not exist!"));
    }

    @Override
    public void updateSetWithJournalMealFood(JournalMeal jm, JournalMealFood jmf) {
        Set<JournalMealFood> jmfSet = jm.getJournalMealFoods();
        jmfSet.removeIf(journalMealFood -> journalMealFood.getId().equals(jmf.getId()));
        jmfSet.add(jmf);
        jm.setJournalMealFoods(jmfSet);
    }

    @Override
    public void addJmfToJournalMealTotals(JournalMeal jm, JournalMealFood jmf) {
        jm.setQuantity(jm.getQuantity().add(jmf.getQuantity()));
        jm.setKiloJoules(jm.getKiloJoules().add(jmf.getKiloJoules()));
        jm.setProteins(jm.getProteins().add(jmf.getProteins()));
        jm.setCarbohydrates(jm.getCarbohydrates().add(jmf.getCarbohydrates()));
        jm.setFiber(jm.getFiber().add(jmf.getFiber()));
        jm.setFat(jm.getFat().add(jmf.getFat()));
        jm.setSugar(jm.getSugar().add(jmf.getSugar()));
        jm.setSafa(jm.getSafa().add(jmf.getSafa()));
        jm.setTfa(jm.getTfa().add(jmf.getTfa()));
        jm.setCholesterol(jm.getCholesterol().add(jmf.getCholesterol()));
        jm.setSodium(jm.getSodium().add(jmf.getSodium()));
        jm.setCalcium(jm.getCalcium().add(jmf.getCalcium()));
        jm.setPhe(jm.getPhe().add(jmf.getPhe()));
    }

    @Override
    public void saveJournalMeal(JournalMeal jm) {
        journalMealRepository.save(jm);
    }

    @Override
    public JournalMeal getJournalMealById(Long id) {
        return journalMealRepository.findById(id).orElseThrow(()-> new RecordNotFoundException("Meal record with id " + id + " does not exist!"));
    }

    @Override
    public void removeJmfAndSubtractFromJournalMealTotals(JournalMeal jm, JournalMealFood jmf) {
        jm.setQuantity(jm.getQuantity().subtract(jmf.getQuantity()));
        jm.setKiloJoules(jm.getKiloJoules().subtract(jmf.getKiloJoules()));
        jm.setProteins(jm.getProteins().subtract(jmf.getProteins()));
        jm.setCarbohydrates(jm.getCarbohydrates().subtract(jmf.getCarbohydrates()));
        jm.setFiber(jm.getFiber().subtract(jmf.getFiber()));
        jm.setFat(jm.getFat().subtract(jmf.getFat()));
        jm.setSugar(jm.getSugar().subtract(jmf.getSugar()));
        jm.setSafa(jm.getSafa().subtract(jmf.getSafa()));
        jm.setTfa(jm.getTfa().subtract(jmf.getTfa()));
        jm.setCholesterol(jm.getCholesterol().subtract(jmf.getCholesterol()));
        jm.setSodium(jm.getSodium().subtract(jmf.getSodium()));
        jm.setCalcium(jm.getCalcium().subtract(jmf.getCalcium()));
        jm.setPhe(jm.getPhe().subtract(jmf.getPhe()));
        jm.getJournalMealFoods().remove(jmf);
        journalMealRepository.save(jm);
    }





}

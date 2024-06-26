package com.pc.kilojoulesrest.service;

import com.pc.kilojoulesrest.entity.Food;
import com.pc.kilojoulesrest.entity.JournalMeal;
import com.pc.kilojoulesrest.entity.JournalMealFood;
import com.pc.kilojoulesrest.entity.JournalMealFoodPortion;
import com.pc.kilojoulesrest.exception.RecordNotFoundException;
import com.pc.kilojoulesrest.repository.JournalMealFoodRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

import static com.pc.kilojoulesrest.constant.Constant.ONE_HUNDRED;

@Service
public class JournalMealFoodServiceImpl implements JournalMealFoodService {

    private final JournalMealFoodRepository journalMealFoodRepository;
    private final JournalMealService journalMealService;
    private final JournalMealFoodPortionService journalMealFoodPortionService;


    public JournalMealFoodServiceImpl(JournalMealFoodRepository journalMealFoodRepository, @Lazy JournalMealService journalMealService, JournalMealFoodPortionService journalMealFoodPortionService) {
        this.journalMealFoodRepository = journalMealFoodRepository;
        this.journalMealService = journalMealService;
        this.journalMealFoodPortionService = journalMealFoodPortionService;
    }

    @Override
    public JournalMealFood saveJournalMealFood(JournalMealFood journalMealFood) {
        return journalMealFoodRepository.save(journalMealFood);
    }

    @Override
    public JournalMealFood createJournalMealFoodFromFood(JournalMeal jm, Food food, BigDecimal quantity) {
        JournalMealFood jmf = JournalMealFood.builder()
                .journalMeal(jm)
                .quantity(quantity)
                .name(food.getName())
                .kiloJoules(food.getKiloJoules().multiply(quantity).divide(ONE_HUNDRED, RoundingMode.HALF_UP))
                .proteins(food.getProteins().multiply(quantity).divide(ONE_HUNDRED, RoundingMode.HALF_UP))
                .carbohydrates(food.getCarbohydrates().multiply(quantity).divide(ONE_HUNDRED, RoundingMode.HALF_UP))
                .fat(food.getFat().multiply(quantity).divide(ONE_HUNDRED, RoundingMode.HALF_UP))
                .fiber(food.getFiber().multiply(quantity).divide(ONE_HUNDRED, RoundingMode.HALF_UP))
                .sugar(food.getSugar().multiply(quantity).divide(ONE_HUNDRED, RoundingMode.HALF_UP))
                .safa(food.getSafa().multiply(quantity).divide(ONE_HUNDRED, RoundingMode.HALF_UP))
                .tfa(food.getTfa().multiply(quantity).divide(ONE_HUNDRED, RoundingMode.HALF_UP))
                .cholesterol(food.getCholesterol().multiply(quantity).divide(ONE_HUNDRED, RoundingMode.HALF_UP))
                .sodium(food.getSodium().multiply(quantity).divide(ONE_HUNDRED, RoundingMode.HALF_UP))
                .calcium(food.getCalcium().multiply(quantity).divide(ONE_HUNDRED, RoundingMode.HALF_UP))
                .phe(food.getPhe().multiply(quantity).divide(ONE_HUNDRED, RoundingMode.HALF_UP))
                .build();
        JournalMealFood finalJmf = journalMealFoodRepository.save(jmf);
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
        return journalMealFoodRepository.save(jmf);
    }

    @Override
    public JournalMealFood  updateJournalMealFood(Long id, BigDecimal quantity, BigDecimal portionSize, String foodName) {
        JournalMealFood jmf = journalMealFoodRepository.findById(id).orElseThrow(()-> new RecordNotFoundException("Food with id " + id + " doesn't exist"));
        BigDecimal savedQuantity = quantity.multiply(portionSize);
        jmf.setName(foodName);
        jmf.calculateAndUpdate(savedQuantity);
        return journalMealFoodRepository.save(jmf);
    }

    @Override
    @Transactional
    public void deleteJmfByIdAndMealId(Long journalMealFoodId, Long journalMealId) {
        JournalMealFood jmf = journalMealFoodRepository.findById(journalMealFoodId).orElseThrow(()-> new RecordNotFoundException("Food with id " + journalMealFoodId + " doesn't exist"));
        JournalMeal jm = journalMealService.getJournalMealById(journalMealId);
        journalMealService.removeJmfAndSubtractFromJournalMealTotals(jm, jmf);
        journalMealFoodRepository.delete(jmf);
    }


}

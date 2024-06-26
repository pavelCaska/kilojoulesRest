package com.pc.kilojoulesrest.service;

import com.pc.kilojoulesrest.entity.JournalMeal;
import com.pc.kilojoulesrest.entity.JournalMealFood;
import com.pc.kilojoulesrest.entity.Meal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import java.util.Map;
import java.util.Set;

public interface JournalMealService {

    Map<String, String> buildErrorResponseForJournalMeal(BindingResult bindingResult);


    @Transactional
    JournalMeal convertMealToJournalMeal(Meal meal);

    void calculateAndSetTotalFieldsFromSet(JournalMeal journalMeal, Set<JournalMealFood> jmfSet);

    JournalMeal fetchJournalMealById(Long id);

    void updateSetWithJournalMealFood(JournalMeal jm, JournalMealFood jmf);

    void addJmfToJournalMealTotals(JournalMeal jm, JournalMealFood jmf);

    void saveJournalMeal(JournalMeal jm);

    JournalMeal getJournalMealById(Long id);

    void removeJmfAndSubtractFromJournalMealTotals(JournalMeal jm, JournalMealFood jmf);
}

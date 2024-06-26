package com.pc.kilojoulesrest.service;

import com.pc.kilojoulesrest.entity.JournalMealFoodPortion;
import com.pc.kilojoulesrest.repository.JournalMealFoodPortionRepository;
import org.springframework.stereotype.Service;

@Service
public class JournalMealFoodPortionServiceImpl implements JournalMealFoodPortionService {

    private final JournalMealFoodPortionRepository journalMealFoodPortionRepository;


    public JournalMealFoodPortionServiceImpl(JournalMealFoodPortionRepository journalMealFoodPortionRepository) {
        this.journalMealFoodPortionRepository = journalMealFoodPortionRepository;
    }

    @Override
    public void saveJmfp(JournalMealFoodPortion jmfp) {
        journalMealFoodPortionRepository.save(jmfp);
    }

}

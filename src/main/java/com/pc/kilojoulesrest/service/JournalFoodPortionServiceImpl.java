package com.pc.kilojoulesrest.service;

import com.pc.kilojoulesrest.entity.JournalFoodPortion;
import com.pc.kilojoulesrest.repository.JournalFoodPortionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JournalFoodPortionServiceImpl implements JournalFoodPortionService {

    private final JournalFoodPortionRepository journalFoodPortionRepository;

    @Autowired
    public JournalFoodPortionServiceImpl(JournalFoodPortionRepository journalFoodPortionRepository) {
        this.journalFoodPortionRepository = journalFoodPortionRepository;
    }

    @Override
    public void saveJfp(JournalFoodPortion jfp) {
        journalFoodPortionRepository.save(jfp);
    }

}

package com.pc.kilojoulesrest.service;

import com.pc.kilojoulesrest.entity.Food;
import com.pc.kilojoulesrest.entity.JournalFood;
import com.pc.kilojoulesrest.entity.User;
import org.springframework.validation.BindingResult;

import java.math.BigDecimal;
import java.util.Map;

public interface JournalFoodService {

    JournalFood createJournalFood(Food food, BigDecimal quantity, String foodName);

    Map<String, String> buildErrorResponseForJournalFood(BindingResult bindingResult);
}

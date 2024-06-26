package com.pc.kilojoulesrest.entity;

import java.math.BigDecimal;

public interface JournalEntry {

    Long getJournalFoodId();
    Long getJournalMealId();

    BigDecimal getQuantity();
    BigDecimal getKiloJoules();
    BigDecimal getProteins();
    BigDecimal getCarbohydrates();
    BigDecimal getFiber();
    BigDecimal getFat();

}

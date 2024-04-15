package com.pc.kilojoulesrest.entity;

import java.math.BigDecimal;

public interface JournalEntry {
    BigDecimal getQuantity();
    BigDecimal getKiloJoules();
    BigDecimal getProteins();
    BigDecimal getCarbohydrates();
    BigDecimal getFiber();
    BigDecimal getFat();


}

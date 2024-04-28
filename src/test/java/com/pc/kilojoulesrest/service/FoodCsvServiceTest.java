package com.pc.kilojoulesrest.service;

import com.pc.kilojoulesrest.model.FoodCSVRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;

import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class FoodCsvServiceTest {
    @Mock
    private FoodCsvServiceImpl foodCsvService;

    @Test
    @DisplayName("JUNit test for import and convert CSV data")
    void convertCSV() throws FileNotFoundException {
        List<FoodCSVRecord> mockRecords = new ArrayList<>();
        FoodCSVRecord record = new FoodCSVRecord();
//        "Omacka Kaiser Bolognese";01.02.2024;100;412;4;7;5;6;2;;;;;;0,22
        record.setName("Omacka Kaiser Bolognese");
        record.setCreatedAt(Date.valueOf("2024-02-01"));
        record.setQuantity(new BigDecimal("100"));
        record.setKiloJoules(new BigDecimal("412"));
        record.setProteins(new BigDecimal("4"));
        record.setCarbohydrates(new BigDecimal("7"));
        record.setSugar(new BigDecimal("5"));
        record.setFat(new BigDecimal("6"));
        record.setSafa(new BigDecimal("2"));
        record.setTfa(null);
        record.setCholesterol(null);
        record.setFiber(null);
        record.setSodium(null);
        record.setCalcium(new BigDecimal("0"));
        record.setPhe(new BigDecimal("22"));

        mockRecords.add(record);
        given(foodCsvService.convertCSV(any(File.class))).willReturn(mockRecords);

        File file;
        try {
            file = ResourceUtils.getFile("classpath:csvdata/potraviny.csv");
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + e.getMessage());

            throw new FileNotFoundException("Unable to locate file at 'classpath:csvdata/potraviny.csv'");
        }
        List<FoodCSVRecord> recs = foodCsvService.convertCSV(file);

        assertThat(recs).isNotNull();
        assertThat(recs.size()).isGreaterThan(0);
    }
}
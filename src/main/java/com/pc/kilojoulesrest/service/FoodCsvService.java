package com.pc.kilojoulesrest.service;

import com.pc.kilojoulesrest.model.FoodCSVRecord;

import java.io.File;
import java.util.List;

public interface FoodCsvService {
        List<FoodCSVRecord> convertCSV(File csvFile);

    }

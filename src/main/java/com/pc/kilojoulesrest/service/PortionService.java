package com.pc.kilojoulesrest.service;

import com.pc.kilojoulesrest.entity.Food;
import com.pc.kilojoulesrest.entity.Portion;
import com.pc.kilojoulesrest.exception.RecordNameExistsException;
import com.pc.kilojoulesrest.model.PortionRequestDTO;
import org.springframework.validation.BindingResult;

import java.util.Map;

public interface PortionService {

    Portion createPortion(Food food, Portion portion) throws RecordNameExistsException;

    Portion createPortionRest(Food food, PortionRequestDTO portionRequestDTO) throws RecordNameExistsException;

    void deletePortionById(Long id);

    Map<String, String> buildErrorResponseForPortionCreation(BindingResult bindingResult);

    boolean existsPortionByIdAndFoodId(Long portionId, Long foodId);
}

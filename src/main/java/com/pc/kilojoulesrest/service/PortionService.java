package com.pc.kilojoulesrest.service;

import com.pc.kilojoulesrest.entity.Food;
import com.pc.kilojoulesrest.entity.Portion;
import com.pc.kilojoulesrest.exception.RecordNameExistsException;
import com.pc.kilojoulesrest.model.PortionRequestDTO;
import com.pc.kilojoulesrest.model.PortionResponseDTO;
import org.springframework.validation.BindingResult;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface PortionService {

    void savePortion(Portion portion);

    Optional<Portion> fetchPortionById(Long portionId);

    Portion addPortionToList(Food food, Portion portion) throws RecordNameExistsException;

    Portion createPortionRest(Food food, PortionRequestDTO portionRequestDTO) throws RecordNameExistsException;

    void deletePortionById(Long id);

    Map<String, String> buildErrorResponseForPortionCreation(BindingResult bindingResult);

    boolean existsPortionByIdAndFoodId(Long portionId, Long foodId);

    List<PortionResponseDTO> convertFoodToPortionResponseDtoList(Food food);
}

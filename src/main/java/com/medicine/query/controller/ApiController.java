package com.medicine.query.controller;

import com.medicine.query.model.MedEntity;
import com.medicine.query.service.MedicineService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@Slf4j
@RestController
public class ApiController {

    @Autowired
    MedicineService medicineService;

    @GetMapping(value = {"/get/{med}", "/api/get/{med}"})
    public List<MedEntity> getMedicine(@PathVariable("med") String med) {
        try {
            List<MedEntity> result = medicineService.getMedicine(med);
            return result != null ? result : Collections.emptyList();
        } catch (Exception e) {
            log.error("API getMedicine error for [{}]: ", med, e);
            return Collections.emptyList();
        }
    }

}

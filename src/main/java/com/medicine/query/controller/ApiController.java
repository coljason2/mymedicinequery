package com.medicine.query.controller;

import com.medicine.query.model.MedEntity;
import com.medicine.query.service.MedicineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ApiController {

    @Autowired
    MedicineService medicineService;

    @GetMapping(value = "/get/{med}")
    public List<MedEntity> getMedicine(@PathVariable("med") String med) {
        return medicineService.getMedicine(med);
    }
}

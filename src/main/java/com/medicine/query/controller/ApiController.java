package com.medicine.query.controller;

import com.medicine.query.exception.MedException;
import com.medicine.query.model.IbonRsp;
import com.medicine.query.model.MedEntity;
import com.medicine.query.service.MedicineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ApiController {

    @Autowired
    MedicineService medicineService;

    @GetMapping(value = "/get/{med}")
    public List<MedEntity> getMedicine(@PathVariable("med") String med) {
        try {
            return medicineService.getMedicine(med);
        } catch (Exception e) {
            throw new MedException(e);
        }
    }

}

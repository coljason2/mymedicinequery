package com.medicine.query.controller;


import com.medicine.query.exception.MedException;
import com.medicine.query.service.MedicineService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.constraints.NotNull;

@Slf4j
@Controller
@RequestMapping("/")
public class AppController {

    @Autowired
    MedicineService medicineService;

    @RequestMapping(value = {"/login", "/index"}, method = RequestMethod.GET)
    public String login(ModelMap model) {
        return "login";
    }

    @RequestMapping(value = {"/home", "/", "/index"}, method = RequestMethod.GET)
    public String home(ModelMap model) {
        return "home";
    }

    @RequestMapping(value = "/query", method = RequestMethod.POST)
    public String POSTQuery(@RequestParam String querystring, Model model) {
        try {
            model.addAttribute("meds", medicineService.getMedicine(querystring));
        } catch (Exception e) {
            throw new MedException(e);
        }
        model.addAttribute("querystring", querystring);
        return "result";
    }
}

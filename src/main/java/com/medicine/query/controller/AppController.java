package com.medicine.query.controller;


import com.medicine.query.common.Constant;
import com.medicine.query.exception.MedException;
import com.medicine.query.model.IbonRsp;
import com.medicine.query.model.MedEntity;
import com.medicine.query.service.MedicinePrintService;
import com.medicine.query.service.MedicineService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import javax.validation.constraints.NotNull;
import java.io.ByteArrayInputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/")
public class AppController {

    @Autowired
    private MedicineService medicineService;

    @Autowired
    private MedicinePrintService medicinePrintService;

    @RequestMapping(value = {"/login", "/index"}, method = RequestMethod.GET)
    public String login(ModelMap model) {
        return "login";
    }

    @RequestMapping(value = {"/home", "/", "/index"}, method = RequestMethod.GET)
    public String home(ModelMap model) {
        model.addAttribute("medCompanys", Constant.medCompanys);
        return "home";
    }

    @RequestMapping(value = "/query", method = RequestMethod.POST)
    public String POSTQuery(@RequestParam String querystring, Model model) {
        try {
            log.info("querystring :{}", querystring);
            model.addAttribute("medCompanys", Constant.medCompanys);
            model.addAttribute("meds", medicineService.getMedicine(querystring));
        } catch (Exception e) {
            throw new MedException(e);
        }
        model.addAttribute("querystring", querystring);
        return "result";
    }

    @RequestMapping(value = "/query/list", method = RequestMethod.POST)
    public String POSTQueryList(@RequestParam String querystring, Model model) {
        try {
            model.addAttribute("medCompanys", Constant.medCompanys);
            model.addAttribute("meds", medicineService.getMedicineByList(querystring));
        } catch (Exception e) {
            throw new MedException(e);
        }
        model.addAttribute("querystring", querystring);
        return "result";
    }

    @RequestMapping(value = "/pdfreport", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<InputStreamResource> medsReport(@RequestParam String querystring) throws Exception {

        ByteArrayInputStream bis;
        if (querystring.contains(",")) {
            bis = medicinePrintService.medsReport(medicineService.getMedicineByList(querystring));
        } else {
            bis = medicinePrintService.medsReport(medicineService.getMedicine(querystring));
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline;filename=\"" + querystring + ".pdf\"");
        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bis));

    }

    @RequestMapping(value = "/qrcode", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_PDF_VALUE)
    public String createQRprint(@RequestParam String querystring, Model model) throws Exception {

        IbonRsp rsp;
        if (querystring.contains(",")) {
            rsp = medicineService.createQRcode(medicineService.getMedicineByList(querystring));
        } else {
            rsp = medicineService.createQRcode(medicineService.getMedicine(querystring));
        }

        model.addAttribute("fileCode", rsp.getFileqrcode().trim());
        model.addAttribute("fileNumber", rsp.getPincode());
        model.addAttribute("querystring", querystring);
        model.addAttribute("medCompanys", Constant.medCompanys);
        return "qrcode";
    }

    @RequestMapping(value = "/query/health/code/{acCode}", method = RequestMethod.GET)
    public String queryHealthCode(@PathVariable("acCode") String acCode, Model model) {
        log.info("健保碼：{}", acCode);
        try {
            model.addAttribute("link", medicineService.createFDALink(acCode));
        } catch (Exception e) {
            log.error("queryHealthCode", e);
            throw new MedException(e);
        }
        return "jump";
    }
}

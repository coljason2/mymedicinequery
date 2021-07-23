package com.medicine.query.controller;


import com.medicine.query.exception.MedException;
import com.medicine.query.model.IbonRsp;
import com.medicine.query.model.MedEntity;
import com.medicine.query.service.MedicineService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.constraints.NotNull;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/")
public class AppController {

    @Autowired
    private MedicineService medicineService;

    private static List<String> medCompanys = Arrays.asList(new String[]{"邁蘭", "衛達", "友華", "福元", "意欣", "一成", "陽生", "皇佳", "黃氏"
            , "國嘉", "新瑞", "泰宗", "鼎泰", "井田", "天義", "衛肯", "順華", "鴻文", "星寶", "曼哈頓", "南光", "永勝", "韋淳", "拜耳", "海喬", "益普生", "幸生", "永茂", "西海", "柏理"
            , "榮民", "羅得", "應元", "元宙", "五洲", "華興", "中化", "信東", "瑪科隆", "政德", "武田", "盈盈"
    });

    @RequestMapping(value = {"/login", "/index"}, method = RequestMethod.GET)
    public String login(ModelMap model) {
        return "login";
    }

    @RequestMapping(value = {"/home", "/", "/index"}, method = RequestMethod.GET)
    public String home(ModelMap model) {
        model.addAttribute("medCompanys", medCompanys);
        return "home";
    }

    @RequestMapping(value = "/query", method = RequestMethod.POST)
    public String POSTQuery(@RequestParam String querystring, Model model) {
        try {
            log.info("querystring :{}", querystring);
            model.addAttribute("medCompanys", medCompanys);
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
            model.addAttribute("medCompanys", medCompanys);
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
            bis = medicineService.medsReport(medicineService.getMedicineByList(querystring));
        } else {
            bis = medicineService.medsReport(medicineService.getMedicine(querystring));
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
        model.addAttribute("medCompanys", medCompanys);
        return "qrcode";
    }

}

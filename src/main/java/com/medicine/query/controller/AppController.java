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
import java.util.Collections;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/")
public class AppController {

    @Autowired
    private MedicineService medicineService;

    @Autowired
    private MedicinePrintService medicinePrintService;

    @RequestMapping(value = "/login", method = RequestMethod.GET)
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
            List<MedEntity> meds = medicineService.getMedicine(querystring);
            model.addAttribute("meds", meds != null ? meds : Collections.emptyList());
        } catch (Exception e) {
            log.error("POSTQuery error for [{}]: ", querystring, e);
            model.addAttribute("meds", Collections.emptyList());
            model.addAttribute("errorMessage", "查詢時發生異常（外部連線或代理逾時），請稍後再試。");
        }
        model.addAttribute("querystring", querystring);
        return "result :: resultFragment";
    }

    @RequestMapping(value = "/query/list", method = RequestMethod.POST)
    public String POSTQueryList(@RequestParam String querystring, Model model) {
        try {
            model.addAttribute("medCompanys", Constant.medCompanys);
            List<MedEntity> meds = medicineService.getMedicineByList(querystring);
            model.addAttribute("meds", meds != null ? meds : Collections.emptyList());
        } catch (Exception e) {
            log.error("POSTQuery error for [{}]: ", querystring, e);
            model.addAttribute("meds", Collections.emptyList());
            model.addAttribute("errorMessage", "查詢時發生異常（外部連線或代理逾時），請稍後再試。");
        }
        model.addAttribute("querystring", querystring);
        return "result :: resultFragment";
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
        String encodedFilename = URLEncoder.encode(querystring, "UTF-8").replace("+", "%20");
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + encodedFilename + ".pdf\"; filename*=UTF-8''" + encodedFilename + ".pdf");
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

        if (rsp == null || rsp.getFileqrcode() == null) {
            log.warn("IBON 雲端列印上傳失敗或未取得 QR Code");
            model.addAttribute("fileCode", "");
            model.addAttribute("fileNumber", "上傳失敗，請稍後重試");
            model.addAttribute("querystring", querystring);
            model.addAttribute("medCompanys", Constant.medCompanys);
            return "qrcode";
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
            log.error("queryHealthCode error for code [{}]: ", acCode, e);
            model.addAttribute("link", "");
            model.addAttribute("errorMessage", "查詢健保碼仿單時發生異常，請稍後再試。");
        }
        return "jump";
    }
}

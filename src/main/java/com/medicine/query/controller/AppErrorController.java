package com.medicine.query.controller;


import com.medicine.query.common.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@Controller
public class AppErrorController implements ErrorController {

    @ExceptionHandler(Exception.class)
    @RequestMapping("/error")
    public String handleError(HttpServletRequest req, Model model, Exception ex) {
        model.addAttribute("ex", ex);
        model.addAttribute("url", req.getRequestURI());
        model.addAttribute("medCompanys", Constant.medCompanys);
        return "error";
    }

    @Override
    public String getErrorPath() {
        return "/error";
    }
}

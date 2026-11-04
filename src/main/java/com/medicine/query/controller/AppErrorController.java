package com.medicine.query.controller;

import com.medicine.query.common.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

@Slf4j
@Controller
public class AppErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest req, Model model) {
        Object status = req.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object exception = req.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        Object originalUri = req.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);

        String displayUrl = (originalUri != null) ? originalUri.toString() : req.getRequestURI();
        String displayError = (exception != null) ? exception.toString() : (status != null ? "HTTP 狀態碼：" + status : "系統發生未預期錯誤");

        log.warn("Handling error for URI [{}], Status [{}], Exception: {}", displayUrl, status, displayError);

        model.addAttribute("ex", displayError);
        model.addAttribute("url", displayUrl);
        model.addAttribute("medCompanys", Constant.medCompanys);
        return "error";
    }

    @Override
    public String getErrorPath() {
        return "/error";
    }
}

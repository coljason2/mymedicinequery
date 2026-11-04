package com.medicine.query.controller;

import com.medicine.query.common.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Controller
public class AppErrorController implements ErrorController {

    @RequestMapping("/error")
    public Object handleError(HttpServletRequest req, HttpServletResponse resp, Model model) {
        Object statusCode = req.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object exception = req.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        Object originalUri = req.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);

        String displayUrl = (originalUri != null) ? originalUri.toString() : req.getRequestURI();
        int status = (statusCode instanceof Integer) ? (Integer) statusCode : resp.getStatus();
        if (status <= 0) {
            status = 500;
        }

        String displayError = (exception != null) ? exception.toString() : "HTTP 狀態碼：" + status;
        log.warn("Handling error for URI [{}], Status [{}], Exception: {}", displayUrl, status, displayError);

        String accept = req.getHeader("Accept");
        boolean isApi = (displayUrl != null && (displayUrl.startsWith("/api/") || displayUrl.startsWith("/get/")))
                || (accept != null && accept.contains(MediaType.APPLICATION_JSON_VALUE));

        if (isApi) {
            Map<String, Object> errorMap = new LinkedHashMap<>();
            errorMap.put("status", status);
            errorMap.put("error", status == 404 ? "Not Found" : (status == 401 ? "Unauthorized" : "Internal Server Error"));
            errorMap.put("message", displayError);
            errorMap.put("path", displayUrl);
            return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(errorMap);
        }

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

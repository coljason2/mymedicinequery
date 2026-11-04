package com.medicine.query.controller;

import com.medicine.query.model.AppVersionConfig;
import com.medicine.query.service.AppVersionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AppVersionController {

    private final AppVersionService appVersionService;

    /**
     * APP 檢查版本公開端點 (相容 /version.json 與 /api/app/config)
     */
    @GetMapping(value = {"/version.json", "/api/app/config"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> getVersion() {
        log.info("App requested version check");
        return appVersionService.getVersionResponseMap();
    }

    /**
     * Web 後台版本調整頁面
     */
    @GetMapping("/download")
    public String showDownloadPage(Model model) {
        model.addAttribute("config", appVersionService.getVersionConfig());
        return "download";
    }

    @GetMapping("/admin/version")
    public String showVersionPage(Model model) {
        model.addAttribute("config", appVersionService.getVersionConfig());
        return "admin/version";
    }

    /**
     * Web 後台更新版本設定
     */
    @PostMapping("/admin/version")
    public String updateVersion(@ModelAttribute AppVersionConfig formConfig, RedirectAttributes redirectAttributes) {
        try {
            if (formConfig.getVersionCode() == null || formConfig.getVersionCode() <= 0) {
                redirectAttributes.addFlashAttribute("errorMessage", "版號 (versionCode) 必須為大於 0 的整數！");
                return "redirect:/admin/version";
            }
            if (formConfig.getVersionName() == null || formConfig.getVersionName().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "版本名稱 (versionName) 不得為空！");
                return "redirect:/admin/version";
            }
            if (formConfig.getDownloadUrl() == null || formConfig.getDownloadUrl().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "下載網址 (downloadUrl) 不得為空！");
                return "redirect:/admin/version";
            }

            if (formConfig.getForceUpdate() == null) {
                formConfig.setForceUpdate(false);
            }
            if (formConfig.getNotice() != null && formConfig.getNotice().getEnable() == null) {
                formConfig.getNotice().setEnable(false);
            }
            if (formConfig.getMaintenance() != null && formConfig.getMaintenance().getIsUnderMaintenance() == null) {
                formConfig.getMaintenance().setIsUnderMaintenance(false);
            }

            appVersionService.updateVersionConfig(formConfig);
            redirectAttributes.addFlashAttribute("successMessage", "版本設定已成功更新！APP 將立即讀取到最新設定。");
        } catch (Exception e) {
            log.error("Failed to update app version config", e);
            redirectAttributes.addFlashAttribute("errorMessage", "更新失敗：" + e.getMessage());
        }
        return "redirect:/admin/version";
    }
}


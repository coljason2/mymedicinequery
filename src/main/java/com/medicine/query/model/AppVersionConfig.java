package com.medicine.query.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppVersionConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 核心版號數字（例如 4）
     */
    private Integer versionCode;

    /**
     * 版本名稱（例如 "3.6"）
     */
    private String versionName;

    /**
     * APK 下載連結
     */
    private String downloadUrl;

    /**
     * 更新日誌說明
     */
    private String updateLog;

    /**
     * 是否強制更新
     */
    private Boolean forceUpdate;

    /**
     * 系統公告設定
     */
    private NoticeConfig notice;

    /**
     * 系統維護狀態設定
     */
    private MaintenanceConfig maintenance;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NoticeConfig implements Serializable {
        private static final long serialVersionUID = 1L;
        private Boolean enable;
        private Integer id;
        private String title;
        private String message;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MaintenanceConfig implements Serializable {
        private static final long serialVersionUID = 1L;
        private Boolean isUnderMaintenance;
        private String message;
    }
}


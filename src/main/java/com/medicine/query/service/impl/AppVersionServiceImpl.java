package com.medicine.query.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.medicine.query.model.AppVersionConfig;
import com.medicine.query.service.AppVersionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Slf4j
@Service
public class AppVersionServiceImpl implements AppVersionService {

    private static final String CONFIG_FILE_PATH = "data/app-version.json";
    private final ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private volatile AppVersionConfig currentConfig;

    @PostConstruct
    public void init() {
        File file = new File(CONFIG_FILE_PATH);
        if (file.exists() && file.length() > 0) {
            try {
                rwLock.writeLock().lock();
                currentConfig = objectMapper.readValue(file, AppVersionConfig.class);
                log.info("Loaded AppVersionConfig from {}: v{} ({})", CONFIG_FILE_PATH, currentConfig.getVersionCode(), currentConfig.getVersionName());
            } catch (Exception e) {
                log.error("Failed to load {}, initializing with default values", CONFIG_FILE_PATH, e);
                initDefaultConfig();
            } finally {
                rwLock.writeLock().unlock();
            }
        } else {
            rwLock.writeLock().lock();
            try {
                initDefaultConfig();
            } finally {
                rwLock.writeLock().unlock();
            }
        }
    }

    private void initDefaultConfig() {
        currentConfig = AppVersionConfig.builder()
                .versionCode(4)
                .versionName("3.6")
                .downloadUrl("https://drive.google.com/file/d/1cHBbBULiCisBH4dQfqsUhfr4y-nvOGPy/view?usp=sharing")
                .updateLog("1. 修復大盤查詢問題\n2. 增加健保資料快速複製\n3. 效能優化")
                .forceUpdate(false)
                .notice(AppVersionConfig.NoticeConfig.builder()
                        .enable(false)
                        .id(1)
                        .title("系統公告")
                        .message("系統目前正常運作中。")
                        .build())
                .maintenance(AppVersionConfig.MaintenanceConfig.builder()
                        .isUnderMaintenance(false)
                        .message("系統維護中，請稍後再試。")
                        .build())
                .build();
        saveToFile(currentConfig);
    }

    @Override
    public AppVersionConfig getVersionConfig() {
        rwLock.readLock().lock();
        try {
            return currentConfig;
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public Map<String, Object> getVersionResponseMap() {
        rwLock.readLock().lock();
        try {
            Map<String, Object> map = new LinkedHashMap<>();
            // 扁平層級（相容舊版直接由 root 解析的客戶端）
            map.put("versionCode", currentConfig.getVersionCode());
            map.put("versionName", currentConfig.getVersionName());
            map.put("downloadUrl", currentConfig.getDownloadUrl());
            map.put("updateLog", currentConfig.getUpdateLog());
            map.put("forceUpdate", Boolean.TRUE.equals(currentConfig.getForceUpdate()));

            // 巢狀層級（相容結構化規範與新版客戶端）
            Map<String, Object> versionMap = new LinkedHashMap<>();
            versionMap.put("versionCode", currentConfig.getVersionCode());
            versionMap.put("versionName", currentConfig.getVersionName());
            versionMap.put("downloadUrl", currentConfig.getDownloadUrl());
            versionMap.put("updateLog", currentConfig.getUpdateLog());
            versionMap.put("forceUpdate", Boolean.TRUE.equals(currentConfig.getForceUpdate()));
            map.put("version", versionMap);

            if (currentConfig.getNotice() != null) {
                map.put("notice", currentConfig.getNotice());
            }
            if (currentConfig.getMaintenance() != null) {
                map.put("maintenance", currentConfig.getMaintenance());
            }
            return map;
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public void updateVersionConfig(AppVersionConfig newConfig) {
        rwLock.writeLock().lock();
        try {
            if (newConfig.getNotice() == null && currentConfig.getNotice() != null) {
                newConfig.setNotice(currentConfig.getNotice());
            }
            if (newConfig.getMaintenance() == null && currentConfig.getMaintenance() != null) {
                newConfig.setMaintenance(currentConfig.getMaintenance());
            }
            currentConfig = newConfig;
            saveToFile(currentConfig);
            log.info("AppVersionConfig updated to: v{} ({})", newConfig.getVersionCode(), newConfig.getVersionName());
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    private void saveToFile(AppVersionConfig config) {
        try {
            File file = new File(CONFIG_FILE_PATH);
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
                objectMapper.writeValue(writer, config);
            }
            log.info("Successfully persisted AppVersionConfig to {}", file.getAbsolutePath());
        } catch (Exception e) {
            log.error("Failed to persist AppVersionConfig to {}", CONFIG_FILE_PATH, e);
        }
    }
}


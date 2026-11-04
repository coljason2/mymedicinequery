package com.medicine.query.service;

import com.medicine.query.model.AppVersionConfig;

import java.util.Map;

public interface AppVersionService {

    /**
     * 取得目前版本設定物件
     */
    AppVersionConfig getVersionConfig();

    /**
     * 取得供 APP 讀取的相容 JSON 回應 Map
     */
    Map<String, Object> getVersionResponseMap();

    /**
     * 更新版本設定並持久化至檔案
     */
    void updateVersionConfig(AppVersionConfig config);
}


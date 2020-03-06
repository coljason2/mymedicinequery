package com.medicine.query.service;

import com.medicine.query.model.MedEntity;

import java.util.List;

public interface MedicineService {

    List<MedEntity> getMedicine(String name) throws Exception;

    String getContext(String name, String cookiePara) throws Exception;

    String getContextPage(String name, String cookiePara, int Now_page) throws Exception;

    String decode(String s) throws Exception;

    String getCookies() throws Exception;
}

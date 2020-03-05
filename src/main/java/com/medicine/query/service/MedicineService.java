package com.medicine.query.service;

import com.medicine.query.model.MedEntity;

import java.util.List;

public interface MedicineService {

    List<MedEntity> getMedicine(String name);

    String getContext(String name, String cookiePara);

    String getContextPage(String name, String cookiePara, int Now_page);

    String decode(String s);

    String getCookies();
}

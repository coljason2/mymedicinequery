package com.medicine.query.service;

import com.medicine.query.model.IbonRsp;
import com.medicine.query.model.MedEntity;

import java.io.ByteArrayInputStream;
import java.util.List;

public interface MedicineService {

    List<MedEntity> getMedicine(String name) throws Exception;

    @Deprecated
    IbonRsp createQRcode(List<MedEntity> meds);

    List<MedEntity> getMedicineByList(String strList);

    String createFDALink(String code);
}

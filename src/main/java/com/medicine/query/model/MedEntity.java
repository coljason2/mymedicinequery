package com.medicine.query.model;

import lombok.Data;

import java.util.Comparator;

@Data
public class MedEntity implements Comparator<MedEntity> {
    String name;
    String isEnough;
    String oid;
    String oidPrice;
    String company;

    @Override
    public int compare(MedEntity o1, MedEntity o2) {
        return o1.getCompany().hashCode() - o2.getCompany().hashCode();
    }
}

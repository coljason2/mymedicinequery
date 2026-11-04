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
        if (o1 == null && o2 == null) return 0;
        if (o1 == null) return 1;
        if (o2 == null) return -1;
        String c1 = o1.getCompany();
        String c2 = o2.getCompany();
        if (c1 == null && c2 == null) return 0;
        if (c1 == null) return 1;
        if (c2 == null) return -1;
        return c1.compareTo(c2);
    }
}

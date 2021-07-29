package com.medicine.query.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Comparator;

@ApiModel(value = "MedEntity")
@Data
public class MedEntity implements Comparator<MedEntity> {
    @ApiModelProperty(value = "名稱")
    String name;
    @ApiModelProperty(value = "庫存")
    String isEnough;
    @ApiModelProperty(value = "OID")
    String oid;
    @ApiModelProperty(value = "OID 價格")
    String oidPrice;
    @ApiModelProperty(value = "藥品公司名稱")
    String company;

    @Override
    public int compare(MedEntity o1, MedEntity o2) {
        return o1.getCompany().hashCode() - o2.getCompany().hashCode();
    }
}

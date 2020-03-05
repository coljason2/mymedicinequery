package com.medicine.query.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel(value = "MedEntity")
@Data
public class MedEntity {
    @ApiModelProperty(value = "名稱")
    String name;
    @ApiModelProperty(value = "庫存")
    String isEnough;
    @ApiModelProperty(value = "OID")
    String oid;
    @ApiModelProperty(value = "OID 價格")
    String oidPrice;
}

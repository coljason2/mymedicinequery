package com.medicine.query.model;

import io.swagger.annotations.ApiModel;
import lombok.Data;

@ApiModel("Ibon Response")
@Data
public class IbonRsp {
    String pincode;
    String deadLine;
    String fileQrcode;
    String fileDate;
    String resultCode;
    String message;
    String email;
}

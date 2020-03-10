package com.medicine.query.model;

import io.swagger.annotations.ApiModel;
import lombok.Data;

@ApiModel("Ibon Response")
@Data
public class IbonRsp {
    String pincode;
    String deadline;
    String fileqrcode;
    String filedate;
    String resultcode;
    String message;
    String email;
}

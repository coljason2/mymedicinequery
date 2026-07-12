package com.medicine.query.model;

import lombok.Data;

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

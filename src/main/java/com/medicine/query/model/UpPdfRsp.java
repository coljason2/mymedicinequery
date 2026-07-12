package com.medicine.query.model;

import lombok.Data;

@Data
public class UpPdfRsp {
    String hash;
    String filename;
    Integer size;
    String resultcode;
    String message;
}

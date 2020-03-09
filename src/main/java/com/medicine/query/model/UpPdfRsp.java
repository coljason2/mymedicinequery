package com.medicine.query.model;

import io.swagger.annotations.ApiModel;
import lombok.Data;

@ApiModel("7-11 列印PDF上傳回應")
@Data
public class UpPdfRsp {
    String hash;
    String filename;
    Integer size;
    String resultcode;
    String message;
}

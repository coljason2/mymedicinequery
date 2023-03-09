package com.medicine.query.model;


import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.Base64;


@ApiModel(value = "登入資訊")
@Data
public class LoginFormData {
    private String username = "TGlzYW5pdGFteQ==";
    private String password = "MzY3OTQ5OA==";
    private String wsrc = "aI9ta3KeZJqRaFeXa5qaZ2pya2lqc6ibyZLJn6Ks3HBoj21rcp5mkpFoV5drmppnanJraWpzpqTQmKFgY2ycbnCTbmRphmWXmHBqmG6bkmY";
    private String act = "act_login";
    private String back_act = "user.php";

    private String submit="";
}

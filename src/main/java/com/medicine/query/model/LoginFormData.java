package com.medicine.query.model;


import io.swagger.annotations.ApiModel;
import lombok.Data;


@ApiModel(value = "登入資訊")
@Data
public class LoginFormData {
    private String username = "Lisanitamy";
    private String password = "3679498";
    private String wsrc = "aI9tZG2XZJeRaFeXaZaSZ29uZW1xc6ibyZLJn6Ks3HBoj21kbZdlmpFoV5dplpJnb25lbXFzpqTQmKFgY2yVaWmTcGRphmWVlGhqnWqVlm0";
    private String act = "act_login";
    private String back_act = "user.php";
}

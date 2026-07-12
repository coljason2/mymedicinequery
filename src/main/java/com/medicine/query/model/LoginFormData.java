package com.medicine.query.model;


import lombok.Data;

import java.util.Base64;


@Data
public class LoginFormData {
    private String username = "TGlzYW5pdGFteQ==";
    private String password = "MzY3OTQ5OA==";
    private String wsrc = "aI9tZG2XZJeRaFeXaZaSZ29uZW1xc6ibyZLJn6Ks3HBoj21kbZdlmpFoV5dplpJnb25lbXFzpqTQmKFgY2yVaWmTcGRphmWVlGhqnWqVlm0";
    private String act = "act_login";
    private String back_act = "user.php";
}

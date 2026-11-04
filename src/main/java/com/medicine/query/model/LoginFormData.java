package com.medicine.query.model;

import lombok.Data;

@Data
public class LoginFormData {
    // 支援由環境變數注入覆蓋（若未設定則保留預設值）
    private String username = (System.getenv("CHAHWA_USERNAME") != null && !System.getenv("CHAHWA_USERNAME").trim().isEmpty())
            ? System.getenv("CHAHWA_USERNAME")
            : "TGlzYW5pdGFteQ==";

    private String password = (System.getenv("CHAHWA_PASSWORD") != null && !System.getenv("CHAHWA_PASSWORD").trim().isEmpty())
            ? System.getenv("CHAHWA_PASSWORD")
            : "MzY3OTQ5OA==";

    private String wsrc = (System.getenv("CHAHWA_WSRC") != null && !System.getenv("CHAHWA_WSRC").trim().isEmpty())
            ? System.getenv("CHAHWA_WSRC")
            : "aI9tZG2XZJeRaFeXaZaSZ29uZW1xc6ibyZLJn6Ks3HBoj21kbZdlmpFoV5dplpJnb25lbXFzpqTQmKFgY2yVaWmTcGRphmWVlGhqnWqVlm0";

    private String act = "act_login";
    private String back_act = "user.php";
}

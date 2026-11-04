package com.medicine.query.service;

import com.medicine.query.exception.MedException;
import com.medicine.query.model.LoginFormData;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Slf4j
public class CrawlerSessionManager {

    private static final long THREE_HOURS_MS = 3 * 60 * 60 * 1000L; // 3 小時快取
    private static final int TIMEOUT_MS = 60000;
    private static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36";
    private static final LoginFormData form = new LoginFormData();

    private static volatile String cachedCookie = null;
    private static volatile long lastLoginTime = 0L;
    private static final Object LOCK = new Object();

    /**
     * 取得有效之 Cookie (有效期限為 3 小時，過期或未登入才執行登入)
     */
    public static String getValidCookie() {
        long now = System.currentTimeMillis();
        if (cachedCookie != null && (now - lastLoginTime) < THREE_HOURS_MS) {
            log.debug("使用已快取的 Session Cookie，剩餘效期: {} 秒", (THREE_HOURS_MS - (now - lastLoginTime)) / 1000);
            return cachedCookie;
        }

        synchronized (LOCK) {
            // Double-checked locking
            if (cachedCookie != null && (System.currentTimeMillis() - lastLoginTime) < THREE_HOURS_MS) {
                return cachedCookie;
            }
            log.info("Crawler Session Cookie 不存在或已超過 3 小時，執行登入以更新 Cookie...");
            cachedCookie = performLogin();
            lastLoginTime = System.currentTimeMillis();
            return cachedCookie;
        }
    }

    /**
     * 強制使 Cookie 快取失效（例如爬蟲遭遇 Session 過期時調用）
     */
    public static void invalidateCookie() {
        synchronized (LOCK) {
            log.warn("強制清空已快取的 Session Cookie。");
            cachedCookie = null;
            lastLoginTime = 0L;
        }
    }

    private static String performLogin() {
        Map<String, String> cookies;
        StringBuilder cookiePara = new StringBuilder();
        try {
            String baseUrl = MedicineGrabberCallable.getBaseUrl();
            if (System.getenv("REVERSE_PROXY_URL") != null) {
                log.info("Using Reverse Proxy for getCookies: {}", baseUrl);
            } else {
                log.info("Not using Reverse Proxy for getCookies: {}", baseUrl);
            }

            Connection con = Jsoup.connect(baseUrl + "/user.php")
                    .sslSocketFactory(MedicineGrabberCallable.getTrustAllSSLSocketFactory())
                    .data("username", new String(Base64.getDecoder().decode(form.getUsername()), StandardCharsets.UTF_8),
                          "password", new String(Base64.getDecoder().decode(form.getPassword()), StandardCharsets.UTF_8),
                          "wsrc", form.getWsrc(), "act", form.getAct(), "back_act", form.getBack_act())
                    .method(Connection.Method.POST)
                    .timeout(TIMEOUT_MS)
                    .userAgent(DEFAULT_USER_AGENT)
                    .followRedirects(false);

            if (System.getenv("REVERSE_PROXY_URL") != null) {
                con.header("ngrok-skip-browser-warning", "69420");
                String authHeader = MedicineGrabberCallable.getAuthHeader();
                if (authHeader != null) {
                    con.header("Authorization", authHeader);
                }
            }

            Connection.Response res = con.execute();
            cookies = res.cookies();

            for (Map.Entry<String, String> entry : cookies.entrySet()) {
                cookiePara.append(entry.getKey()).append("=").append(entry.getValue()).append(";");
            }

            log.info("爬蟲登入成功，取得新 Cookie，將快取 3 小時。");
            return cookiePara.toString();
        } catch (Exception e) {
            log.error("爬蟲登入取得 Cookie 失敗: ", e);
            throw new MedException(e);
        }
    }
}


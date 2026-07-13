package com.medicine.query.service;

import com.medicine.query.exception.MedException;
import com.medicine.query.model.LoginFormData;
import com.medicine.query.model.MedEntity;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.*;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class MedicineGrabberCallable implements Callable<List<MedEntity>> {

    private static final int TIMEOUT_MS = 60000;
    private static final String SCRAPER_API_URL_TEMPLATE = "http://api.scraperapi.com?api_key=%s&country_code=tw&url=%s";
    private static final String SCRAPER_API_WITH_HEADERS_TEMPLATE = "http://api.scraperapi.com?api_key=%s&country_code=tw&keep_headers=true&url=%s";
    private static final String TARGET_LOGIN_URL = "https://www.chahwa.com.tw/user.php";
    private static final String getdrug = "https://www.chahwa.com.tw/order.php?act=query&&drug=";
    private static final Pattern reUnicode = Pattern.compile("\\\\u([0-9a-zA-Z]{4})");
    private static final LoginFormData form = new LoginFormData();

    private String queryName;

    public MedicineGrabberCallable(String queryName) {
        this.queryName = queryName;
    }

    @Override
    public List<MedEntity> call() throws Exception {
        return getMedicine(queryName);
    }

    public List<MedEntity> getMedicine(String queryName) {

        String cookie = getCookies();
        List<MedEntity> meds = new CopyOnWriteArrayList<>();
        // clean code
        String parseString = decode(getContext(queryName, cookie).replace("\\//", "")).replace("\\", "").replace("}", "")
                .replace("{", "").replace("rn", "");
        Document resault = Jsoup.parse(parseString);
        int total_page = 1;
        org.jsoup.select.Elements pagenavi = resault.getElementsByClass("pagenavi");
        if (pagenavi != null && !pagenavi.isEmpty()) {
            org.jsoup.select.Elements spanBs = pagenavi.select("span").select("b");
            if (spanBs.size() > 1) {
                try {
                    total_page = Integer.parseInt(spanBs.get(1).text());
                } catch (NumberFormatException e) {
                    log.error("Parse total page error", e);
                }
            }
        } else {
            log.warn("Cannot find pagenavi, parseString content: {}", parseString);
        }
        log.info("total_page = {}", total_page);
        if (total_page > 1) {
            for (int i = 1; i <= total_page; i++) {
                parseString = decode(getContextPage(queryName, cookie, i).replace("\\//", "")).replace("\\", "")
                        .replace("}", "").replace("{", "").replace("rn", "");
                resault = Jsoup.parse(parseString);
                this.setResponseEntities(meds, resault, queryName);
            }
        } else {
            this.setResponseEntities(meds, resault, queryName);
        }
        return meds;
    }

    public String getContext(String name, String cookiePara) {
        StringBuilder context = new StringBuilder();
        try {
            String targetUrl = getdrug + URLEncoder.encode(name, "utf-8");
            String connectUrl = targetUrl;
            if ("true".equals(System.getenv("USE_SCRAPER_API"))) {
                String apiKey = System.getenv("SCRAPER_API_KEY");
                String encodedUrl = URLEncoder.encode(targetUrl, "UTF-8");
                connectUrl = String.format(SCRAPER_API_WITH_HEADERS_TEMPLATE, apiKey, encodedUrl);
            }
            URL getUrl = new URL(connectUrl);
            HttpURLConnection connection = (HttpURLConnection) getUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.setReadTimeout(TIMEOUT_MS);
            connection.addRequestProperty("Cookie", cookiePara);
            connection.connect();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String lines;
            while ((lines = reader.readLine()) != null) {
                context.append(decode(lines));
            }
            reader.close();
            connection.disconnect();
        } catch (Exception e) {
            log.error("Getcontext Error ", e);
            throw new MedException(e);
        }
        return context.toString();
    }

    public String getContextPage(String name, String cookiePara, int Now_page) {
        StringBuilder context = new StringBuilder();
        try {
            String targetUrl = getdrug + URLEncoder.encode(name, "utf-8") + "&page=" + Now_page;
            String connectUrl = targetUrl;
            if ("true".equals(System.getenv("USE_SCRAPER_API"))) {
                String apiKey = System.getenv("SCRAPER_API_KEY");
                String encodedUrl = URLEncoder.encode(targetUrl, "UTF-8");
                connectUrl = String.format(SCRAPER_API_WITH_HEADERS_TEMPLATE, apiKey, encodedUrl);
            }
            URL getUrl = new URL(connectUrl);
            HttpURLConnection connection = (HttpURLConnection) getUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.setReadTimeout(TIMEOUT_MS);
            connection.addRequestProperty("Cookie", cookiePara);
            connection.connect();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String lines;
            while ((lines = reader.readLine()) != null) {
                context.append(decode(lines));
            }
            reader.close();
            connection.disconnect();
        } catch (Exception e) {
            log.error("Getcontext Error ", e);
            throw new MedException(e);
        }
        return context.toString();
    }

    public String decode(String s) {
        Matcher m = reUnicode.matcher(s);
        StringBuffer sb = new StringBuffer(s.length());
        while (m.find()) {
            m.appendReplacement(sb, Character.toString((char) Integer.parseInt(m.group(1), 16)));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    public String getCookies() {
        Map<String, String> cookies = null;
        String cookiePara = "";
        try {

            String targetUrl = TARGET_LOGIN_URL;
            String connectUrl = targetUrl;
            
            if ("true".equals(System.getenv("USE_SCRAPER_API"))) {
                log.info("Using Scraper API for getCookies");
                String apiKey = System.getenv("SCRAPER_API_KEY");
                String encodedUrl = java.net.URLEncoder.encode(targetUrl, "UTF-8");
                connectUrl = String.format(SCRAPER_API_URL_TEMPLATE, apiKey, encodedUrl);
            } else {
                log.info("Not using Scraper API for getCookies");
            }

            // 套用自訂 SSL 設定
            Connection.Response res = Jsoup.connect(connectUrl)
                    .data("username", new String(Base64.getDecoder().decode(form.getUsername())), "password", new String(Base64.getDecoder().decode((form.getPassword()))), "wsrc", form.getWsrc(), "act",
                            form.getAct(), "back_act", form.getBack_act())
                    .method(Connection.Method.POST)
                    .timeout(TIMEOUT_MS)
                    .execute();
            cookies = res.cookies();

        } catch (Exception e) {
            log.error("getCookies Error ", e);
            throw new MedException(e);
        }
        for (String cookie : cookies.keySet()) {
            cookiePara = cookiePara + cookie.toString() + "=" + cookies.get(cookie) + ";";
        }
        // log.info(cookiePara.toString());
        return cookiePara;
    }

    private void setResponseEntities(List<MedEntity> meds, Document resault, String company) {
        for (org.jsoup.nodes.Element n : resault.getElementsByClass("item_text")) {
            MedEntity m = new MedEntity();
            m.setOid(n.getElementsByClass("code").text());
            m.setOidPrice(n.getElementsByClass("price").text());
            m.setIsEnough(n.getElementsByClass("sell_price").text());
            m.setName(n.getElementsByClass("name").text());
            m.setCompany(company);
            meds.add(m);
        }
    }
}

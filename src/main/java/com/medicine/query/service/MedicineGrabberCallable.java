package com.medicine.query.service;

import com.medicine.query.exception.MedException;
import com.medicine.query.model.LoginFormData;
import com.medicine.query.model.MedEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class MedicineGrabberCallable implements Callable<List<MedEntity>> {

    private static final String getdrug = "http://www.chahwa.com.tw/order.php?act=query&&drug=";
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
        List<MedEntity> meds = new ArrayList<MedEntity>();
        // clean code
        String parseString = decode(getContext(queryName, cookie).replace("\\//", "")).replace("\\", "").replace("}", "")
                .replace("{", "").replace("rn", "");
        Document resault = Jsoup.parse(parseString);
        int total_page = Integer
                .parseInt(resault.getElementsByClass("pagenavi").select("span").select("b").get(1).text());
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
        String context = "";
        try {
            URL getUrl = new URL(getdrug + URLEncoder.encode(name, "utf-8"));
            HttpURLConnection connection = (HttpURLConnection) getUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            connection.setReadTimeout(3000);
            connection.addRequestProperty("Cookie", cookiePara);
            connection.connect();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String lines;
            while ((lines = reader.readLine()) != null) {
                context = context + decode(lines);
            }
            reader.close();
            connection.disconnect();
        } catch (Exception e) {
            log.error("Getcontext Error ", e);
            throw new MedException(e);
        }
        return context;
    }

    public String getContextPage(String name, String cookiePara, int Now_page) {
        String context = "";
        try {
            URL getUrl = new URL(getdrug + URLEncoder.encode(name, "utf-8") + "&page=" + Now_page);
            HttpURLConnection connection = (HttpURLConnection) getUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            connection.setReadTimeout(3000);
            connection.addRequestProperty("Cookie", cookiePara);
            connection.connect();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String lines;
            while ((lines = reader.readLine()) != null) {
                context = context + decode(lines);
            }
            reader.close();
            connection.disconnect();
        } catch (Exception e) {
            log.error("Getcontext Error ", e);
            throw new MedException(e);
        }
        return context;
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
            Connection.Response res = Jsoup.connect("http://www.chahwa.com.tw/user.php")
                    .data("username", new String(Base64.getDecoder().decode(form.getUsername())), "password", new String(Base64.getDecoder().decode((form.getPassword()))), "wsrc", form.getWsrc(), "act",
                            form.getAct(), "back_act", form.getBack_act())
                    .method(Connection.Method.POST).execute();
            cookies = res.cookies();

        } catch (IOException e) {
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
        for (Element n : resault.getElementsByClass("item_text")) {
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

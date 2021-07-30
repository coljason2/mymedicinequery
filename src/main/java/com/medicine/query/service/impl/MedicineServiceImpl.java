package com.medicine.query.service.impl;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.medicine.query.exception.MedException;
import com.medicine.query.model.*;
import com.medicine.query.service.MedicinePrintService;
import com.medicine.query.service.MedicineService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Slf4j
@Service
public class MedicineServiceImpl implements MedicineService {

    static final String getdrug = "http://www.chahwa.com.tw/order.php?act=query&&drug=";
    static final String uploadPath = "https://printadmin.ibon.com.tw/IbonUpload/IbonUpload/LocalFileUpload";
    static final String postPath = "https://printadmin.ibon.com.tw/IbonUpload/IbonUpload/IbonFileUpload";
    static final Pattern reUnicode = Pattern.compile("\\\\u([0-9a-zA-Z]{4})");
    static final LoginFormData form = new LoginFormData();
    private final OkHttpClient client = new OkHttpClient();

    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private MedicinePrintService medicinePrintService;

    @Override
    public List<MedEntity> getMedicine(String queryName) {

        if (StringUtils.isBlank(queryName)) {
            throw new MedException("查詢空白");
        }

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

    @Override
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

    @Override
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

    @Override
    public String decode(String s) {
        Matcher m = reUnicode.matcher(s);
        StringBuffer sb = new StringBuffer(s.length());
        while (m.find()) {
            m.appendReplacement(sb, Character.toString((char) Integer.parseInt(m.group(1), 16)));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    @Override
    public String getCookies() {
        Map<String, String> cookies = null;
        String cookiePara = "";
        try {
            Connection.Response res = Jsoup.connect("http://www.chahwa.com.tw/user.php")
                    .data("username", form.getUsername(), "password", form.getPassword(), "wsrc", form.getWsrc(), "act",
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

    @Override
    public IbonRsp createQRcode(List<MedEntity> meds) {

        String hash = UUID.randomUUID().toString();
        RequestBody fileBody = RequestBody.create(medicinePrintService.createPdfOutputStream(meds).toByteArray(), MediaType.parse("pdf"));
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM).addFormDataPart("file", "", fileBody)
                .addFormDataPart("fileName", "meds.pdf")
                .addFormDataPart("hash", hash)
                .build();
        Request request = new Request.Builder()
                .url(uploadPath)
                .post(requestBody)
                .build();


        UpPdfRsp respEntity = null;
        IbonRsp ibonResp = new IbonRsp();
        Response response = null;
        String jsonString = null;
        try {
            response = client.newCall(request).execute();
            jsonString = response.body().string().toLowerCase();
            respEntity = mapper.readValue(jsonString, UpPdfRsp.class);
            log.info("respEntity = {} ", respEntity);
            if ("00".equals(respEntity.getResultcode())) {
                RequestBody formBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                        .addFormDataPart("hash", respEntity.getHash())
                        .addFormDataPart("user", "zzshcool")
                        .addFormDataPart("email", "zzshcool@gmail.com")
                        .build();

                Headers headers = new Headers.Builder()
                        .add("accept-encoding", "gzip, deflate, br")
                        .add("accept-language", "zh-TW,zh;q=0.9,en-US;q=0.8,en;q=0.7")
                        .add("content-type", "application/x-www-form-urlencoded; charset=UTF-8")
                        .add("sec-fetch-dest", "empty")
                        .add("sec-fetch-mode", "cors")
                        .add("sec-fetch-site", "same-site")
                        .add("accept", "/*")
                        .add("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.122 Safari/537.36")
                        .build();

                Request postReq = new Request.Builder()
                        .url(postPath)
                        .headers(headers)
                        .post(formBody)
                        .build();

                response = client.newCall(postReq).execute();
                JsonNode rootNode = mapper.readTree(response.body().string());
                log.info("jsonString = {} ", jsonString);
                ibonResp.setPincode(rootNode.get("Pincode").asText());
                ibonResp.setFileqrcode(rootNode.get("FileQrcode").asText());

            } else {
                throw new MedException("檔案上傳錯誤!!!");
            }
            log.info("ibonResp = {} ", ibonResp);
            return ibonResp;
        } catch (Exception e) {
            log.error("upload Exception ", e);
        }
        return null;
    }

    @Override
    public List<MedEntity> getMedicineByList(String strList) {
        List<String> namesList = Arrays.asList(strList.split(","));
        List<MedEntity> list = new ArrayList<>();
        for (String company : namesList) {
            list.addAll(this.getMedicine(company));
        }
        Collections.sort(list, new MedEntity());
        log.info("list :{} ", list);
        return list;
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

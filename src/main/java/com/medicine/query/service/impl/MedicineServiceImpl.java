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
import com.medicine.query.service.MedicineGrabberCallable;
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
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Slf4j
@Service
public class MedicineServiceImpl implements MedicineService {


    private static final String uploadPath = "https://printadmin.ibon.com.tw/IbonUpload/IbonUpload/LocalFileUpload";
    private static final String postPath = "https://printadmin.ibon.com.tw/IbonUpload/IbonUpload/IbonFileUpload";
    private static final LoginFormData form = new LoginFormData();
    private final OkHttpClient client = new OkHttpClient();

    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private MedicinePrintService medicinePrintService;
    @Autowired
    private ExecutorService executorService;

    @Override
    public List<MedEntity> getMedicine(String queryName) {
        try {
            if (StringUtils.isBlank(queryName)) {
                throw new MedException("查詢空白");
            }

            MedicineGrabberCallable callable = new MedicineGrabberCallable(queryName);
            Future<List<MedEntity>> future = executorService.submit(callable);

            return future.get();
        } catch (Exception e) {
            log.error("getMedicine :{} ", e);
            return null;
        }
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
        List<MedEntity> medList = new ArrayList<>();
        List<Future<List<MedEntity>>> futureList = new ArrayList<Future<List<MedEntity>>>();
        for (String company : namesList) {
            MedicineGrabberCallable callable = new MedicineGrabberCallable(company);
            Future<List<MedEntity>> future = executorService.submit(callable);
            futureList.add(future);
        }

        for (Future<List<MedEntity>> f : futureList) {
            try {
                medList.addAll(f.get());
            } catch (Exception e) {
                log.error("future get error :{} ", e);
            }
        }

        Collections.sort(medList, new MedEntity());
        //log.info("list :{} ", medList);
        return medList;
    }


}

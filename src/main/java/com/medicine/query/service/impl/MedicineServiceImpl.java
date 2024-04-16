package com.medicine.query.service.impl;


import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicine.query.exception.MedException;
import com.medicine.query.model.*;
import com.medicine.query.service.MedicineGrabberCallable;
import com.medicine.query.service.MedicinePrintService;
import com.medicine.query.service.MedicineService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;


@Slf4j
@Service
public class MedicineServiceImpl implements MedicineService {


    private static final String uploadPath = "https://printadmin.ibon.com.tw/IbonUpload/IbonUpload/LocalFileUpload";
    private static final String postPath = "https://printadmin.ibon.com.tw/IbonUpload/IbonUpload/IbonFileUpload";
    private static final LoginFormData form = new LoginFormData();
    private final OkHttpClient client = new OkHttpClient();

    private static final String fdaLink = "https://info.fda.gov.tw/MLMS/H0001D.aspx?Type=Lic&LicId=";

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
        List<Future<List<MedEntity>>> futureList = new CopyOnWriteArrayList<>();
        namesList.parallelStream().forEachOrdered(company -> {
            MedicineGrabberCallable callable = new MedicineGrabberCallable(company);
            Future<List<MedEntity>> future = executorService.submit(callable);
            futureList.add(future);
        });

        futureList.parallelStream().forEachOrdered(f -> {
            try {
                medList.addAll(f.get());
            } catch (Exception e) {
                log.error("future get error :{} ", e);
            }
        });

        Collections.sort(medList, new MedEntity());
        //log.info("list :{} ", medList);
        return medList;
    }

    @Override
    public String createFDALink(String code) {
        // Create a JSONObject
        JSONObject jsonObject = new JSONObject();

        // Add key-value pairs to the JSONObject
        jsonObject.put("DRUG_CODE", code);
        jsonObject.put("DRUG_NAME", "");
        jsonObject.put("DRUG_DOSE", "");
        jsonObject.put("DRUG_CLASSIFY_NAME", "");
        jsonObject.put("DRUG_ING", "");
        jsonObject.put("DRUG_ING_QTY", "");
        jsonObject.put("DRUG_ING_UNIT", "");
        jsonObject.put("DRUG_STD_QTY", "");
        jsonObject.put("DRUG_STD_UNIT", "");
        jsonObject.put("DRUGGIST_NAME", "");
        jsonObject.put("MIXTURE", "");
        jsonObject.put("PAY_START_DATE_YEAR", "");
        jsonObject.put("PAY_START_DATE_MON", "");
        jsonObject.put("ORAL_TYPE", "");
        jsonObject.put("ATC_CODE", "");
        jsonObject.put("SHOWTYPE", "Y");
        jsonObject.put("CURPAGE", 1);
        jsonObject.put("PAGESIZE", 50);

        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, jsonObject.toJSONString());
        Request request = new Request.Builder()
                .url("https://info.nhi.gov.tw/api/INAE3000/INAE3000S01/SQL0001")
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .build();

        Response response = null;
        JSONObject rsp;
        String resultLink = fdaLink;
        try {
            response = client.newCall(request).execute();
            rsp = JSONObject.parseObject(response.body().string());
            if (Objects.nonNull(rsp.getJSONArray("data")) && rsp.getJSONArray("data").size() > 0) {
                resultLink = fdaLink + rsp.getJSONArray("data").getJSONObject(0).getString("doH_ID");
            }

            log.info("resultLink:{}", resultLink);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        return resultLink;
    }


}

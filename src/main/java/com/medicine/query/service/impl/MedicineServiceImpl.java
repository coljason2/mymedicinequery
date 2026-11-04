package com.medicine.query.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.medicine.query.exception.MedException;
import com.medicine.query.model.IbonRsp;
import com.medicine.query.model.LoginFormData;
import com.medicine.query.model.MedEntity;
import com.medicine.query.model.UpPdfRsp;
import com.medicine.query.service.MedicineGrabberCallable;
import com.medicine.query.service.MedicinePrintService;
import com.medicine.query.service.MedicineService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MedicineServiceImpl implements MedicineService {

    private static final String uploadPath = "https://printadmin.ibon.com.tw/IbonUpload/IbonUpload/LocalFileUpload";
    private static final String postPath = "https://printadmin.ibon.com.tw/IbonUpload/IbonUpload/IbonFileUpload";
    private static final String fdaLink = "https://lmspiq.fda.gov.tw/web/DRPIQ/DRPIQ1000Result?licId=";
    private static final LoginFormData form = new LoginFormData();
    private final OkHttpClient client = getUnsafeOkHttpClient();

    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private MedicinePrintService medicinePrintService;
    @Autowired
    private ExecutorService executorService;

    @Override
    @Cacheable(value = "medicine", key = "T(com.medicine.query.service.MedicineGrabberCallable).normalizeQuery(#queryName)", unless = "#result == null or #result.isEmpty()")
    public List<MedEntity> getMedicine(String queryName) {
        try {
            String normalized = MedicineGrabberCallable.normalizeQuery(queryName);
            if (StringUtils.isBlank(normalized)) {
                throw new MedException("查詢空白");
            }

            MedicineGrabberCallable callable = new MedicineGrabberCallable(normalized);
            Future<List<MedEntity>> future = executorService.submit(callable);

            return future.get(70, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("getMedicine error for queryName [{}]: ", queryName, e);
            return Collections.emptyList();
        }
    }

    @Override
    public IbonRsp createQRcode(List<MedEntity> meds) {
        String hash = UUID.randomUUID().toString();
        RequestBody fileBody = RequestBody.create(medicinePrintService.createPdfOutputStream(meds).toByteArray(), MediaType.parse("application/pdf"));
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM).addFormDataPart("file", "", fileBody)
                .addFormDataPart("fileName", "meds.pdf")
                .addFormDataPart("hash", hash)
                .build();
        Request request = new Request.Builder()
                .url(uploadPath)
                .post(requestBody)
                .build();

        IbonRsp ibonResp = new IbonRsp();
        try (Response response = client.newCall(request).execute()) {
            if (response.body() == null) {
                throw new MedException("檔案上傳無回應");
            }
            String jsonString = response.body().string().toLowerCase();
            UpPdfRsp respEntity = mapper.readValue(jsonString, UpPdfRsp.class);
            log.info("respEntity = {}", respEntity);

            if ("00".equals(respEntity.getResultcode())) {
                String ibonUser = (System.getenv("IBON_USER") != null && !System.getenv("IBON_USER").trim().isEmpty())
                        ? System.getenv("IBON_USER") : "zzshcool";
                String ibonEmail = (System.getenv("IBON_EMAIL") != null && !System.getenv("IBON_EMAIL").trim().isEmpty())
                        ? System.getenv("IBON_EMAIL") : "zzshcool@gmail.com";

                RequestBody formBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                        .addFormDataPart("hash", respEntity.getHash())
                        .addFormDataPart("user", ibonUser)
                        .addFormDataPart("email", ibonEmail)
                        .build();

                Headers headers = new Headers.Builder()
                        .add("accept-encoding", "gzip, deflate, br")
                        .add("accept-language", "zh-TW,zh;q=0.9,en-US;q=0.8,en;q=0.7")
                        .add("content-type", "application/x-www-form-urlencoded; charset=UTF-8")
                        .add("sec-fetch-dest", "empty")
                        .add("sec-fetch-mode", "cors")
                        .add("sec-fetch-site", "same-site")
                        .add("accept", "*/*")
                        .add("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36")
                        .build();

                Request postReq = new Request.Builder()
                        .url(postPath)
                        .headers(headers)
                        .post(formBody)
                        .build();

                try (Response postResponse = client.newCall(postReq).execute()) {
                    if (postResponse.body() != null) {
                        JsonNode rootNode = mapper.readTree(postResponse.body().string());
                        ibonResp.setPincode(rootNode.hasNonNull("Pincode") ? rootNode.get("Pincode").asText() : "");
                        ibonResp.setFileqrcode(rootNode.hasNonNull("FileQrcode") ? rootNode.get("FileQrcode").asText() : "");
                    }
                }
            } else {
                throw new MedException("檔案上傳錯誤!!!");
            }
            log.info("ibonResp = {}", ibonResp);
            return ibonResp;
        } catch (Exception e) {
            log.error("upload Exception: ", e);
            return null;
        }
    }

    public static String canonicalizeCompanyList(String strList) {
        if (StringUtils.isBlank(strList)) {
            return "";
        }
        return Arrays.stream(strList.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .sorted()
                .collect(Collectors.joining(","));
    }

    @Override
    @Cacheable(value = "medicineList", key = "T(com.medicine.query.service.impl.MedicineServiceImpl).canonicalizeCompanyList(#strList)", unless = "#result == null or #result.isEmpty()")
    public List<MedEntity> getMedicineByList(String strList) {
        String canonical = canonicalizeCompanyList(strList);
        if (canonical.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> namesList = Arrays.asList(canonical.split(","));
        List<Future<List<MedEntity>>> futureList = new ArrayList<>();

        for (String company : namesList) {
            String trimmed = company.trim();
            if (!trimmed.isEmpty()) {
                MedicineGrabberCallable callable = new MedicineGrabberCallable(trimmed);
                futureList.add(executorService.submit(callable));
            }
        }

        // 線程安全的循序匯總多執行緒爬取結果
        List<MedEntity> medList = new ArrayList<>();
        for (Future<List<MedEntity>> f : futureList) {
            try {
                List<MedEntity> res = f.get(70, TimeUnit.SECONDS);
                if (res != null) {
                    medList.addAll(res);
                }
            } catch (Exception e) {
                log.error("future get error in getMedicineByList: ", e);
                f.cancel(true);
            }
        }

        Collections.sort(medList, new MedEntity());
        return medList;
    }

    @Override
    @Cacheable(value = "fdaLink", key = "#code", unless = "#result == null or #result.isEmpty() or #result == 'https://lmspiq.fda.gov.tw/web/DRPIQ/DRPIQ1000Result?licId='")
    public String createFDALink(String code) {
        ObjectNode jsonObject = mapper.createObjectNode();
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

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, jsonObject.toString());
        Request request = new Request.Builder()
                .url("https://info.nhi.gov.tw/api/INAE3000/INAE3000S01/SQL0001")
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .build();

        String resultLink = fdaLink;
        try (Response response = client.newCall(request).execute()) {
            if (response.body() != null) {
                JsonNode rsp = mapper.readTree(response.body().string());
                JsonNode dataArray = rsp.get("data");
                if (dataArray != null && dataArray.isArray() && dataArray.size() > 0) {
                    JsonNode firstItem = dataArray.get(0);
                    if (firstItem.hasNonNull("doH_ID")) {
                        resultLink = fdaLink + firstItem.get("doH_ID").asText();
                    }
                }
            }
            log.info("resultLink: {}", resultLink);
        } catch (Exception e) {
            log.error("createFDALink error for code [{}]: ", code, e);
            return resultLink;
        }

        return resultLink;
    }

    private static OkHttpClient getUnsafeOkHttpClient() {
        try {
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }
                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }
                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            return new OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0])
                    .hostnameVerifier((hostname, session) -> true)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

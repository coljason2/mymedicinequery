package com.medicine.query.service.impl;


import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.medicine.query.exception.MedException;
import com.medicine.query.model.LoginFormData;
import com.medicine.query.model.MedEntity;
import com.medicine.query.service.MedicineService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Slf4j
@Service
public class MedicineServiceImpl implements MedicineService {

    static final String getdrug = "http://www.chahwa.com.tw/order.php?act=query&&drug=";
    static final Pattern reUnicode = Pattern.compile("\\\\u([0-9a-zA-Z]{4})");
    static final LoginFormData form = new LoginFormData();

    @Override
    public List<MedEntity> getMedicine(String name) {

        if (StringUtils.isBlank(name)) {
            throw new MedException("查詢空白");
        }

        String cookie = getCookies();
        List<MedEntity> meds = new ArrayList<MedEntity>();
        // clean code
        String parseString = decode(getContext(name, cookie).replace("\\//", "")).replace("\\", "").replace("}", "")
                .replace("{", "").replace("rn", "");
        Document resault = Jsoup.parse(parseString);
        int total_page = Integer
                .parseInt(resault.getElementsByClass("pagenavi").select("span").select("b").get(1).text());
        log.info("total_page = {}", total_page);
        if (total_page > 1) {
            for (int i = 1; i <= total_page; i++) {
                parseString = decode(getContextPage(name, cookie, i).replace("\\//", "")).replace("\\", "")
                        .replace("}", "").replace("{", "").replace("rn", "");
                resault = Jsoup.parse(parseString);
                this.setResponseEntities(meds, resault);
            }
        } else {
            this.setResponseEntities(meds, resault);
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
    public ByteArrayInputStream medsReport(List<MedEntity> meds) {
        com.itextpdf.text.Document document = new com.itextpdf.text.Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(105);
            table.setWidths(new int[]{4, 3, 2, 2});

            BaseFont bfChinese = BaseFont.createFont("MHei-Medium", "UniCNS-UCS2-H", BaseFont.NOT_EMBEDDED);
            Font defaultFont = new Font(bfChinese, 12, 0);
            Font font = defaultFont;

            PdfPCell hcell;
            hcell = new PdfPCell(new Phrase("藥名", font));
            hcell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            table.addCell(hcell);

            hcell = new PdfPCell(new Phrase("藥價/庫存", font));
            hcell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            table.addCell(hcell);

            hcell = new PdfPCell(new Phrase("健保碼", font));
            hcell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            table.addCell(hcell);

            hcell = new PdfPCell(new Phrase("健保價", font));
            hcell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            table.addCell(hcell);

            for (MedEntity med : meds) {

                PdfPCell cell;

                cell = new PdfPCell(new Phrase(med.getName(), font));
                cell.setVerticalAlignment(com.itextpdf.text.Element.ALIGN_MIDDLE);
                cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                table.addCell(cell);

                cell = new PdfPCell(new Phrase(med.getIsEnough(), font));
                cell.setVerticalAlignment(com.itextpdf.text.Element.ALIGN_MIDDLE);
                cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                table.addCell(cell);

                cell = new PdfPCell(new Phrase(String.valueOf(med.getOid()), font));
                cell.setVerticalAlignment(com.itextpdf.text.Element.ALIGN_MIDDLE);
                cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                table.addCell(cell);

                cell = new PdfPCell(new Phrase(String.valueOf(med.getOidPrice()), font));
                cell.setVerticalAlignment(com.itextpdf.text.Element.ALIGN_MIDDLE);
                cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                table.addCell(cell);
            }

            PdfWriter.getInstance(document, out);
            document.open();
            document.add(table);

            document.close();

        } catch (Exception ex) {
            log.error("Error occurred: {0}", ex);
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    private void setResponseEntities(List<MedEntity> meds, Document resault) {
        for (Element n : resault.getElementsByClass("item_text")) {
            MedEntity m = new MedEntity();
            m.setOid(n.getElementsByClass("code").text());
            m.setOidPrice(n.getElementsByClass("price").text());
            m.setIsEnough(n.getElementsByClass("sell_price").text());
            m.setName(n.getElementsByClass("name").text());
            meds.add(m);
        }
    }
}

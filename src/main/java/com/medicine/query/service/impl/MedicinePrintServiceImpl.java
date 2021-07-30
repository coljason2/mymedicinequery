package com.medicine.query.service.impl;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.medicine.query.model.MedEntity;
import com.medicine.query.service.MedicinePrintService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Slf4j
@Service
public class MedicinePrintServiceImpl implements MedicinePrintService {


    @Override
    public ByteArrayInputStream medsReport(List<MedEntity> meds) {
        return new ByteArrayInputStream(this.createPdfOutputStream(meds).toByteArray());
    }

    @Override
    public ByteArrayOutputStream createPdfOutputStream(List<MedEntity> meds) {
        com.itextpdf.text.Document document = new com.itextpdf.text.Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {

            PdfPTable table = this.createTable();

            BaseFont bfChinese = BaseFont.createFont("MHei-Medium", "UniCNS-UCS2-H", BaseFont.NOT_EMBEDDED);
            Font defaultFont = new Font(bfChinese, 12, 0);
            Font font = defaultFont;
            PdfWriter.getInstance(document, out);

            document.open();
            String preCompany = meds.get(0).getCompany();
            Paragraph title = new Paragraph("【" + meds.get(0).getCompany() + "】", new Font(bfChinese, 20, 0));
            for (MedEntity med : meds) {

                if (!med.getCompany().equals(preCompany)) {
                    document.add(title);
                    document.add(new Paragraph(" ", font));
                    document.add(table);
                    document.newPage();
                    title = new Paragraph("【" + med.getCompany() + "】", new Font(bfChinese, 20, 0));
                    table = this.createTable();
                }

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

                preCompany = med.getCompany();

            }

            document.add(title);
            document.add(new Paragraph(" ", font));
            document.add(table);
            document.close();

        } catch (Exception ex) {
            log.error("Error occurred: {0}", ex);
        }
        return out;
    }

    @Override
    public PdfPTable createTable() throws DocumentException, IOException {
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
        return table;
    }
}

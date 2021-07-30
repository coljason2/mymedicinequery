package com.medicine.query.service;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfPTable;
import com.medicine.query.model.MedEntity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public interface MedicinePrintService {

    ByteArrayInputStream medsReport(List<MedEntity> meds);

    ByteArrayOutputStream createPdfOutputStream(List<MedEntity> meds);

    PdfPTable createTable() throws DocumentException, IOException;

}

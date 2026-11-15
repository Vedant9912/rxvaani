package com.rxvaani.service;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.rxvaani.model.Medicine;
import com.rxvaani.model.Prescription;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class PdfService {

    private static final DeviceRgb BLUE_DARK   = new DeviceRgb(0x1E, 0x3A, 0x8A);
    private static final DeviceRgb GRAY_LIGHT  = new DeviceRgb(0xF1, 0xF5, 0xF9);
    private static final DeviceRgb GRAY_ROW    = new DeviceRgb(0xF8, 0xFA, 0xFC);
    private static final DeviceRgb GRAY_BORDER = new DeviceRgb(0xCB, 0xD5, 0xE1);
    private static final DeviceRgb TEXT_DARK   = new DeviceRgb(0x1F, 0x29, 0x37);
    private static final DeviceRgb TEXT_GRAY   = new DeviceRgb(0x64, 0x74, 0x8B);

    public byte[] buildPdf(Prescription rx, String doctorName, String clinicName,
                           String clinicAddress, String regNumber) throws Exception {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document doc = new Document(pdfDoc, PageSize.A4);
        doc.setMargins(36, 36, 36, 36);

        PdfFont bold    = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont regular = PdfFontFactory.createFont(StandardFonts.HELVETICA);
        PdfFont italic  = PdfFontFactory.createFont(StandardFonts.HELVETICA_OBLIQUE);

        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy"));

        // ── 1. Header ──────────────────────────────────────────────────────────
        Table header = new Table(UnitValue.createPercentArray(new float[]{60, 40}))
            .setWidth(UnitValue.createPercentValue(100));

        header.addCell(new Cell().setBorder(new SolidBorder(0))
            .add(new Paragraph(clinicName)
                .setFont(bold).setFontSize(18).setFontColor(BLUE_DARK))
            .add(new Paragraph(clinicAddress)
                .setFont(regular).setFontSize(8).setFontColor(TEXT_GRAY)));

        header.addCell(new Cell().setBorder(new SolidBorder(0))
            .setTextAlignment(TextAlignment.RIGHT)
            .add(new Paragraph(doctorName)
                .setFont(bold).setFontSize(10).setFontColor(TEXT_DARK))
            .add(new Paragraph(regNumber)
                .setFont(regular).setFontSize(8).setFontColor(TEXT_GRAY))
            .add(new Paragraph("Date: " + today)
                .setFont(regular).setFontSize(8).setFontColor(TEXT_GRAY)));

        doc.add(header);

        // Blue separator line
        SolidLine blueLine = new SolidLine(1.5f);
        blueLine.setColor(BLUE_DARK);
        doc.add(new LineSeparator(blueLine).setMarginTop(6).setMarginBottom(10));

        // ── 2. Patient Info Bar ────────────────────────────────────────────────
        String patName   = rx.getPatient().getName()   != null ? rx.getPatient().getName()   : "Walk-in Patient";
        String patAge    = rx.getPatient().getAge()    != null ? rx.getPatient().getAge() + " Yrs" : "—";
        String patGender = rx.getPatient().getGender() != null ? rx.getPatient().getGender() : "—";
        String rxId      = "RXV-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd")) + "-01";

        Table patTable = new Table(UnitValue.createPercentArray(new float[]{35, 15, 20, 30}))
            .setWidth(UnitValue.createPercentValue(100))
            .setBackgroundColor(GRAY_LIGHT);

        addPatientCell(patTable, "Patient", patName, bold, regular);
        addPatientCell(patTable, "Age", patAge, bold, regular);
        addPatientCell(patTable, "Gender", patGender, bold, regular);
        addPatientCell(patTable, "Rx ID", rxId, bold, regular);
        doc.add(patTable);
        doc.add(new Paragraph().setMarginBottom(10));

        // ── 3. Diagnosis & Symptoms ───────────────────────────────────────────
        if (rx.getDiagnosis() != null || !rx.getSymptoms().isEmpty()) {
            if (rx.getDiagnosis() != null) {
                doc.add(new Paragraph()
                    .add(new Text("Provisional Diagnosis: ").setFont(bold))
                    .add(new Text(rx.getDiagnosis()).setFont(regular))
                    .setFontSize(9.5f));
            }
            if (!rx.getSymptoms().isEmpty()) {
                doc.add(new Paragraph()
                    .add(new Text("Reported Symptoms: ").setFont(bold))
                    .add(new Text(String.join(", ", rx.getSymptoms())).setFont(regular))
                    .setFontSize(9f).setFontColor(TEXT_GRAY));
            }
            doc.add(new Paragraph().setMarginBottom(8));
        }

        // ── 4. Medicines Table ────────────────────────────────────────────────
        doc.add(new Paragraph()
            .add(new Text("℞  Prescribed Medications:").setFont(bold).setFontColor(BLUE_DARK))
            .setFontSize(10).setMarginBottom(5));

        Table medTable = new Table(UnitValue.createPercentArray(new float[]{5, 32, 12, 18, 18, 15}))
            .setWidth(UnitValue.createPercentValue(100));

        // Header row
        for (String h : new String[]{"#", "Medicine & Strength", "Dose", "Frequency", "Timing", "Duration"}) {
            medTable.addHeaderCell(new Cell()
                .setBackgroundColor(BLUE_DARK)
                .setBorder(new SolidBorder(0))
                .add(new Paragraph(h)
                    .setFont(bold).setFontSize(8).setFontColor(ColorConstants.WHITE)));
        }

        // Data rows
        int i = 1;
        for (Medicine m : rx.getMedicines()) {
            DeviceRgb rowBg = (i % 2 == 0) ? GRAY_ROW : new DeviceRgb(255, 255, 255);
            String nameStr = m.getName()
                + (m.getStrength() != null ? " (" + m.getStrength() + ")" : "");
            String[] cells = {
                String.valueOf(i++),
                nameStr,
                m.getDose()      != null ? m.getDose()      : "1 tab",
                m.getFrequency() != null ? m.getFrequency() : "As directed",
                m.getTiming()    != null ? m.getTiming()    : "After food",
                m.getDuration()  != null ? m.getDuration()  : "—"
            };
            for (String val : cells) {
                medTable.addCell(new Cell()
                    .setBackgroundColor(rowBg)
                    .setBorder(new SolidBorder(GRAY_BORDER, 0.5f))
                    .add(new Paragraph(val)
                        .setFont(regular).setFontSize(8.5f).setFontColor(TEXT_DARK)));
            }
        }
        doc.add(medTable);
        doc.add(new Paragraph().setMarginBottom(12));

        // ── 5. Advice ─────────────────────────────────────────────────────────
        if (rx.getAdvice() != null && !rx.getAdvice().isEmpty()) {
            doc.add(new Paragraph("General Advice & Instructions:")
                .setFont(bold).setFontSize(10).setFontColor(BLUE_DARK));
            for (String a : rx.getAdvice()) {
                doc.add(new Paragraph("• " + a)
                    .setFont(regular).setFontSize(9.5f).setFontColor(TEXT_DARK));
            }
            doc.add(new Paragraph().setMarginBottom(8));
        }

        // ── 6. Lab Tests ──────────────────────────────────────────────────────
        if (rx.getTests() != null && !rx.getTests().isEmpty()) {
            doc.add(new Paragraph("Recommended Lab / Diagnostic Tests:")
                .setFont(bold).setFontSize(10).setFontColor(BLUE_DARK));
            for (String t : rx.getTests()) {
                doc.add(new Paragraph("• " + t)
                    .setFont(regular).setFontSize(9.5f).setFontColor(TEXT_DARK));
            }
            doc.add(new Paragraph().setMarginBottom(8));
        }

        // ── 7. Follow-up ──────────────────────────────────────────────────────
        if (rx.getFollowUp() != null && !rx.getFollowUp().isBlank()) {
            doc.add(new Paragraph()
                .add(new Text("Next Follow-up Visit: ").setFont(bold))
                .add(new Text(rx.getFollowUp()).setFont(regular))
                .setFontSize(9.5f).setFontColor(TEXT_DARK));
        }

        // ── 8. Footer ─────────────────────────────────────────────────────────
        doc.add(new Paragraph().setMarginBottom(30));

        Table sigTable = new Table(UnitValue.createPercentArray(new float[]{60, 40}))
            .setWidth(UnitValue.createPercentValue(100));
        sigTable.addCell(new Cell().setBorder(new SolidBorder(0)));
        sigTable.addCell(new Cell().setBorder(new SolidBorder(0))
            .setTextAlignment(TextAlignment.RIGHT)
            .add(new Paragraph("____________________________")
                .setFont(regular).setFontSize(10))
            .add(new Paragraph("Doctor's Signature & Stamp")
                .setFont(bold).setFontSize(9)));
        doc.add(sigTable);
        doc.add(new Paragraph().setMarginBottom(20));

        // Gray separator line
        SolidLine grayLine = new SolidLine(0.5f);
        grayLine.setColor(GRAY_BORDER);
        doc.add(new LineSeparator(grayLine).setMarginBottom(5));

        doc.add(new Paragraph(
            "\u2695 RxVaani Safety Protocol: AI-generated consultation draft. " +
            "Strictly reviewed, verified and approved by registered medical practitioner before issuance. " +
            "Built on Gnani AI Prisma STT.")
            .setFont(italic).setFontSize(7.5f).setFontColor(TEXT_GRAY)
            .setTextAlignment(TextAlignment.CENTER));

        doc.close();
        return baos.toByteArray();
    }

    private void addPatientCell(Table t, String label, String value,
                                 PdfFont bold, PdfFont regular) {
        t.addCell(new Cell()
            .setBorder(new SolidBorder(GRAY_BORDER, 0.5f))
            .setPadding(6)
            .add(new Paragraph()
                .add(new Text(label + ": ").setFont(bold).setFontSize(9))
                .add(new Text(value).setFont(regular).setFontSize(9))));
    }
}

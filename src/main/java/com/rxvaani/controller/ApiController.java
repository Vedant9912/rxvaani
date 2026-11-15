package com.rxvaani.controller;

import com.rxvaani.model.*;
import com.rxvaani.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "RxVaani API", description = "Clinical AI Prescription Scribe Endpoints")
public class ApiController {

    private final SttService sttService;
    private final ExtractService extractService;
    private final PdfService pdfService;

    public ApiController(SttService sttService, ExtractService extractService,
                         PdfService pdfService) {
        this.sttService = sttService;
        this.extractService = extractService;
        this.pdfService = pdfService;
    }

    // ── Health ────────────────────────────────────────────────────────────────

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Returns service health status")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "healthy",
            "service", "RxVaani Clinical EHR (Spring Boot)",
            "version", "2.0.0",
            "framework", "Spring Boot 3.2"
        ));
    }

    // ── Demo Scenarios ────────────────────────────────────────────────────────

    @GetMapping("/scenarios")
    @Operation(summary = "Get demo scenarios", description = "Returns all 6 pre-built Hindi consultation scenarios")
    public ResponseEntity<List<Map<String, Object>>> getScenarios() {
        return ResponseEntity.ok(sttService.loadScenarios());
    }

    // ── Transcribe ────────────────────────────────────────────────────────────

    @PostMapping("/transcribe")
    @Operation(summary = "Transcribe audio",
               description = "Transcribes Hindi/Hinglish audio using Gnani Prisma STT. " +
                             "Provide either audio_base64 (live recording) or scenario_id (demo).")
    public ResponseEntity<Map<String, Object>> transcribe(@RequestBody TranscribeRequest req) {
        Map<String, Object> result = sttService.transcribe(req);
        return ResponseEntity.ok(result);
    }

    // ── Extract ───────────────────────────────────────────────────────────────

    @PostMapping("/extract")
    @Operation(summary = "Extract prescription",
               description = "Extracts structured prescription JSON from Hindi/Hinglish transcript using LLM. " +
                             "Falls back to rule-based extractor if no API key provided.")
    public ResponseEntity<Prescription> extract(@RequestBody ExtractRequest req) {
        if (req.getTranscript() == null || req.getTranscript().isBlank()) {
            throw new IllegalArgumentException("Transcript cannot be empty");
        }
        Prescription rx = extractService.extract(
            req.getTranscript(), req.getLlmProvider(), req.getApiKey()
        );
        return ResponseEntity.ok(rx);
    }

    // ── Generate PDF ──────────────────────────────────────────────────────────

    @PostMapping("/generate-pdf")
    @Operation(summary = "Generate prescription PDF",
               description = "Compiles validated prescription into a clinical A4 PDF using iText 7. " +
                             "Returns PDF bytes as file download.")
    public ResponseEntity<byte[]> generatePdf(@RequestBody GeneratePdfRequest req) throws Exception {
        if (req.getPrescription() == null) {
            throw new IllegalArgumentException("Prescription data is required");
        }

        byte[] pdfBytes = pdfService.buildPdf(
            req.getPrescription(),
            req.getDoctorName(),
            req.getClinicName(),
            req.getClinicAddress(),
            req.getRegNumber()
        );

        Patient patient = req.getPrescription().getPatient();
        String patName = (patient != null && patient.getName() != null)
            ? patient.getName().replace(" ", "_") : "Patient";
        String filename = "Prescription_" + patName + ".pdf";

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
            .body(pdfBytes);
    }
}

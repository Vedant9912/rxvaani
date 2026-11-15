package com.rxvaani.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rxvaani.model.Medicine;
import com.rxvaani.model.Patient;
import com.rxvaani.model.Prescription;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.*;

@Service
public class ExtractService {

    @Value("${rxvaani.llm.gemini-api-key:}")
    private String geminiKey;

    @Value("${rxvaani.llm.openai-api-key:}")
    private String openaiKey;

    @Value("${rxvaani.llm.groq-api-key:}")
    private String groqKey;

    @Value("${rxvaani.llm.anthropic-api-key:}")
    private String anthropicKey;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final MedicineService medicineService;
    private String systemPrompt = "You are a medical documentation assistant. Extract a valid JSON prescription from this Hindi/Hinglish doctor-patient transcript. Return ONLY valid JSON matching: {patient:{name,age,gender}, symptoms:[], diagnosis:string, medicines:[{name,strength,dose,frequency,timing,duration,confidence}], advice:[], tests:[], follow_up:string, uncertain_fields:[]}";

    public ExtractService(WebClient webClient, ObjectMapper objectMapper,
                          MedicineService medicineService) {
        this.webClient = webClient;
        this.objectMapper = objectMapper;
        this.medicineService = medicineService;
    }

    @PostConstruct
    public void loadPrompt() {
        try {
            ClassPathResource resource = new ClassPathResource("prompt.txt");
            systemPrompt = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            // Use default prompt defined above
        }
    }

    // ─── Main Pipeline ────────────────────────────────────────────────────────

    public Prescription extract(String transcript, String provider, String overrideKey) {
        Prescription rx = null;

        // 1. Gemini
        String gKey = "Gemini".equalsIgnoreCase(provider) ? overrideKey : geminiKey;
        if (gKey != null && !gKey.isBlank()) {
            rx = extractWithGemini(transcript, gKey);
            if (rx != null) return postProcess(rx);
        }

        // 2. OpenAI
        String oKey = "OpenAI".equalsIgnoreCase(provider) ? overrideKey : openaiKey;
        if (oKey != null && !oKey.isBlank()) {
            rx = extractWithOpenAICompat(transcript, oKey,
                "https://api.openai.com/v1/chat/completions", "gpt-4o-mini");
            if (rx != null) return postProcess(rx);
        }

        // 3. Groq (OpenAI-compatible)
        if (groqKey != null && !groqKey.isBlank()) {
            rx = extractWithOpenAICompat(transcript, groqKey,
                "https://api.groq.com/openai/v1/chat/completions", "llama-3.3-70b-versatile");
            if (rx != null) return postProcess(rx);
        }

        // 4. Anthropic
        String aKey = "Anthropic".equalsIgnoreCase(provider) ? overrideKey : anthropicKey;
        if (aKey != null && !aKey.isBlank()) {
            rx = extractWithAnthropic(transcript, aKey);
            if (rx != null) return postProcess(rx);
        }

        // 5. Fallback: rule-based extractor
        return postProcess(ruleBasedExtract(transcript));
    }

    // ─── Gemini ───────────────────────────────────────────────────────────────

    private Prescription extractWithGemini(String transcript, String apiKey) {
        try {
            String url = "https://generativelanguage.googleapis.com/v1beta/models/" +
                         "gemini-1.5-flash:generateContent?key=" + apiKey;

            Map<String, Object> payload = Map.of(
                "contents", List.of(Map.of("parts", List.of(
                    Map.of("text", systemPrompt + "\n\nTranscript:\n" + transcript)
                ))),
                "generationConfig", Map.of(
                    "temperature", 0.0,
                    "responseMimeType", "application/json"
                )
            );

            String response = webClient.post().uri(url)
                .header("Content-Type", "application/json")
                .bodyValue(objectMapper.writeValueAsString(payload))
                .retrieve()
                .bodyToMono(String.class)
                .block();

            JsonNode root = objectMapper.readTree(response);
            String text = root.at("/candidates/0/content/parts/0/text").asText("");
            return objectMapper.readValue(cleanJson(text), Prescription.class);
        } catch (Exception e) {
            return null;
        }
    }

    // ─── OpenAI / Groq Compatible ─────────────────────────────────────────────

    private Prescription extractWithOpenAICompat(String transcript, String apiKey,
                                                   String endpoint, String model) {
        try {
            Map<String, Object> payload = Map.of(
                "model", model,
                "temperature", 0.0,
                "response_format", Map.of("type", "json_object"),
                "messages", List.of(
                    Map.of("role", "system", "content", systemPrompt),
                    Map.of("role", "user", "content", "Transcript:\n" + transcript)
                )
            );

            String response = webClient.post().uri(endpoint)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(objectMapper.writeValueAsString(payload))
                .retrieve()
                .bodyToMono(String.class)
                .block();

            JsonNode root = objectMapper.readTree(response);
            String content = root.at("/choices/0/message/content").asText("");
            return objectMapper.readValue(cleanJson(content), Prescription.class);
        } catch (Exception e) {
            return null;
        }
    }

    // ─── Anthropic ────────────────────────────────────────────────────────────

    private Prescription extractWithAnthropic(String transcript, String apiKey) {
        try {
            Map<String, Object> payload = Map.of(
                "model", "claude-3-5-sonnet-20241022",
                "max_tokens", 1500,
                "temperature", 0,
                "system", systemPrompt,
                "messages", List.of(
                    Map.of("role", "user", "content", "Transcript:\n" + transcript)
                )
            );

            String response = webClient.post()
                .uri("https://api.anthropic.com/v1/messages")
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .header("Content-Type", "application/json")
                .bodyValue(objectMapper.writeValueAsString(payload))
                .retrieve()
                .bodyToMono(String.class)
                .block();

            JsonNode root = objectMapper.readTree(response);
            String content = root.at("/content/0/text").asText("");
            return objectMapper.readValue(cleanJson(content), Prescription.class);
        } catch (Exception e) {
            return null;
        }
    }

    // ─── Rule-Based Fallback ──────────────────────────────────────────────────

    private Prescription ruleBasedExtract(String transcript) {
        Prescription rx = new Prescription();
        String text = transcript.toLowerCase();

        // Patient Name
        Pattern namePattern = Pattern.compile(
            "(?:namaste|aaiye|mr|mrs|shri|haan)\\s+([A-Za-z]+(?:\\s+[A-Za-z]+)?)",
            Pattern.CASE_INSENSITIVE);
        Matcher nameMatcher = namePattern.matcher(transcript);
        if (nameMatcher.find()) {
            String cand = nameMatcher.group(1).replace("ji", "").replace("Ji", "").strip();
            if (!cand.equalsIgnoreCase("doctor") && !cand.equalsIgnoreCase("sahab")) {
                Patient p = new Patient();
                p.setName(capitalize(cand));
                rx.setPatient(p);
            }
        }

        // Symptoms
        List<String> symptoms = new ArrayList<>();
        if (containsAny(text, "bukhar", "fever", "tap")) symptoms.add("Fever (Bukhar)");
        if (containsAny(text, "badan dard", "body pain", "body ache")) symptoms.add("Body ache (Badan dard)");
        if (containsAny(text, "sir dard", "headache")) symptoms.add("Headache (Sir dard)");
        if (containsAny(text, "pet dard", "acidity", "jalan")) symptoms.add("Acidity / Stomach burning");
        if (containsAny(text, "gale", "kharash", "sore throat")) symptoms.add("Sore throat");
        if (containsAny(text, "khansi", "cough")) symptoms.add("Cough (Khansi)");
        if (containsAny(text, "jukam", "cold", "runny")) symptoms.add("Common Cold (Jukam)");
        if (containsAny(text, "bp", "blood pressure")) symptoms.add("Hypertension monitoring");
        rx.setSymptoms(symptoms);

        // Diagnosis
        if (containsAny(text, "bukhar") && containsAny(text, "khansi", "gale"))
            rx.setDiagnosis("Upper Respiratory Tract Infection / Viral Fever");
        else if (containsAny(text, "pet", "acidity"))
            rx.setDiagnosis("Acute Gastritis / Acid Peptic Disease");
        else if (containsAny(text, "bukhar"))
            rx.setDiagnosis("Acute Febrile Illness (Viral Fever)");
        else if (containsAny(text, "bp"))
            rx.setDiagnosis("Essential Hypertension");

        // Medicines
        List<Medicine> medicines = new ArrayList<>();
        extractMedicine(text, medicines, "Paracetamol", new String[]{"paracetamol", "dolo", "crocin", "calpol"});
        extractMedicine(text, medicines, "Pantoprazole", new String[]{"pantoprazole", "pan 40", "pantocid"});
        extractMedicine(text, medicines, "Domperidone", new String[]{"domperidone", "vomistop"});
        extractMedicine(text, medicines, "Azithromycin", new String[]{"azithromycin", "azee"});
        extractMedicine(text, medicines, "Cetirizine", new String[]{"cetirizine", "cetzine", "okacet"});
        extractMedicine(text, medicines, "Telmisartan", new String[]{"telmisartan", "telma"});
        extractMedicine(text, medicines, "Metformin", new String[]{"metformin", "glycomet"});
        rx.setMedicines(medicines);

        // Advice
        List<String> advice = new ArrayList<>();
        if (containsAny(text, "paani", "water")) advice.add("Drink plenty of warm water and stay hydrated.");
        if (containsAny(text, "aaram", "rest")) advice.add("Adequate bed rest recommended.");
        if (containsAny(text, "garare", "gargle")) advice.add("Warm salt water gargles 3 times a day.");
        if (containsAny(text, "oily", "tel", "teekha")) advice.add("Avoid oily, spicy, and deep fried foods.");
        if (containsAny(text, "walk")) advice.add("30 minutes of brisk daily walk.");
        rx.setAdvice(advice);

        // Follow-up
        if (text.contains("do din baad")) rx.setFollowUp("After 2 days");
        else if (text.contains("paanch din baad")) rx.setFollowUp("After 5 days");
        else if (text.contains("mahine baad") || text.contains("month")) rx.setFollowUp("After 1 month");

        return rx;
    }

    private void extractMedicine(String text, List<Medicine> medicines,
                                  String stdName, String[] aliases) {
        for (String alias : aliases) {
            if (text.contains(alias)) {
                Medicine m = new Medicine();
                m.setName(stdName);
                m.setDose("1 tablet");

                // Strength
                Pattern strPat = Pattern.compile("(?:" + alias + ")\\s*(\\d+\\s*(?:mg|ml)?)", Pattern.CASE_INSENSITIVE);
                Matcher sm = strPat.matcher(text);
                if (sm.find()) {
                    String s = sm.group(1).trim().toUpperCase();
                    if (!s.endsWith("MG") && !s.endsWith("ML")) s += " mg";
                    m.setStrength(s);
                }

                // Frequency
                if (text.contains("subah") && text.contains("shaam")) m.setFrequency("Twice a day");
                else if (text.contains("teen baar")) m.setFrequency("3 times a day");
                else if (text.contains("raat ko")) m.setFrequency("Once daily at night");
                else m.setFrequency("Once daily");

                // Timing
                if (containsAny(text, "khali pet", "khane se pehle")) m.setTiming("Before food");
                else m.setTiming("After food");

                // Duration
                Pattern durPat = Pattern.compile("(\\d+|teen|paanch|do|saat)\\s*(?:din|days)");
                Matcher dm = durPat.matcher(text);
                if (dm.find()) {
                    Map<String, String> map = Map.of(
                        "teen", "3", "paanch", "5", "do", "2", "saat", "7");
                    String d = dm.group(1);
                    m.setDuration(map.getOrDefault(d, d) + " days");
                } else if (text.contains("hafta") || text.contains("week")) {
                    m.setDuration("7 days / 1 week");
                }

                if (m.getDuration() == null) m.setConfidence("low");
                medicines.add(m);
                break;
            }
        }
    }

    // ─── Post Processing ──────────────────────────────────────────────────────

    private Prescription postProcess(Prescription rx) {
        List<String> uncertainFields = rx.getUncertainFields() != null
            ? rx.getUncertainFields() : new ArrayList<>();

        for (Medicine med : rx.getMedicines()) {
            MedicineService.MedicineMatchResult result = medicineService.fuzzyMatch(med.getName());
            if (result.corrected()) med.setName(result.name());

            if (med.getDuration() == null || med.getDuration().isBlank()) {
                String flag = med.getName() + ": Duration missing";
                if (!uncertainFields.contains(flag)) uncertainFields.add(flag);
                med.setConfidence("low");
            }
            if (med.getFrequency() == null || med.getFrequency().isBlank()) {
                String flag = med.getName() + ": Frequency missing";
                if (!uncertainFields.contains(flag)) uncertainFields.add(flag);
                med.setConfidence("low");
            }
        }
        rx.setUncertainFields(uncertainFields);
        return rx;
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private String cleanJson(String raw) {
        String s = raw.strip();
        if (s.startsWith("```json")) s = s.substring(7);
        else if (s.startsWith("```")) s = s.substring(3);
        if (s.endsWith("```")) s = s.substring(0, s.length() - 3);
        return s.strip();
    }

    private boolean containsAny(String text, String... keywords) {
        for (String kw : keywords) {
            if (text.contains(kw)) return true;
        }
        return false;
    }

    private String capitalize(String s) {
        if (s == null || s.isBlank()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase();
    }
}

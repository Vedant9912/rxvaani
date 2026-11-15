package com.rxvaani.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rxvaani.model.TranscribeRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;

@Service
public class SttService {

    @Value("${rxvaani.gnani.api-key:}")
    private String gnaniApiKey;

    @Value("${rxvaani.gnani.endpoint:https://api.vachana.ai/stt/v3}")
    private String gnaniEndpoint;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private List<Map<String, Object>> scenarios = new ArrayList<>();

    public SttService(WebClient webClient, ObjectMapper objectMapper) {
        this.webClient = webClient;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void loadScenarioData() {
        try {
            ClassPathResource resource = new ClassPathResource("scenarios.json");
            JsonNode root = objectMapper.readTree(resource.getInputStream());
            if (root.isArray()) {
                for (JsonNode node : root) {
                    Map<String, Object> sc = objectMapper.convertValue(node, Map.class);
                    scenarios.add(sc);
                }
            }
        } catch (Exception e) {
            // Scenarios file not found — use built-in mock scenarios
            scenarios = getBuiltInScenarios();
        }
    }

    public List<Map<String, Object>> loadScenarios() {
        return scenarios;
    }

    public Map<String, Object> transcribe(TranscribeRequest req) {
        // Handle scenario_id (demo mode)
        if (req.getScenarioId() != null && !req.getScenarioId().isBlank()) {
            for (Map<String, Object> sc : scenarios) {
                if (req.getScenarioId().equals(sc.get("id"))) {
                    String key = req.getGnaniKey() != null ? req.getGnaniKey() : gnaniApiKey;
                    if (key == null || key.isBlank()) {
                        return Map.of(
                            "transcript", sc.getOrDefault("transcript", "").toString(),
                            "success", true,
                            "metadata", Map.of(
                                "model", "prisma-v3.0 (Demo Mode)",
                                "detected_language", "hi-IN",
                                "latency", 0.38,
                                "demo_mode", true
                            )
                        );
                    }
                }
            }
        }

        // Handle audio_base64 (live recording)
        if (req.getAudioBase64() != null && !req.getAudioBase64().isBlank()) {
            String key = req.getGnaniKey() != null ? req.getGnaniKey() : gnaniApiKey;
            if (key == null || key.isBlank()) {
                return mockTranscription(req.getFilename());
            }
            return callGnaniApi(req.getAudioBase64(), req.getFilename(), key);
        }

        // Default fallback
        return mockTranscription("default");
    }

    private Map<String, Object> callGnaniApi(String audioBase64, String filename, String key) {
        try {
            // Decode base64
            String raw = audioBase64.contains(",")
                ? audioBase64.split(",")[1] : audioBase64;
            byte[] audioBytes = Base64.getDecoder().decode(raw);

            // Call Gnani STT API via WebClient (multipart)
            String response = webClient.post()
                .uri(gnaniEndpoint)
                .header("X-API-Key-ID", key)
                .header("Content-Type", "multipart/form-data")
                .retrieve()
                .bodyToMono(String.class)
                .block();

            JsonNode json = objectMapper.readTree(response);
            return Map.of(
                "transcript", json.path("transcript").asText(""),
                "success", true,
                "metadata", Map.of(
                    "model", json.path("model").asText("prisma-v3.0"),
                    "detected_language", json.path("detected_language_code").asText("hi-IN"),
                    "latency", json.path("end_to_end_latency").asDouble(0.0)
                )
            );
        } catch (Exception e) {
            return Map.of(
                "transcript", "",
                "success", false,
                "error", "Gnani API error: " + e.getMessage()
            );
        }
    }

    private Map<String, Object> mockTranscription(String filename) {
        String fn = filename != null ? filename.toLowerCase() : "";
        String transcript;

        if (fn.contains("fever") || fn.contains("scenario_1")) {
            transcript = "Doctor: Namaste Ramesh ji, bataiye kya takleef hai? " +
                "Patient: Doctor sahab do din se tez bukhar hai aur badan dard ho raha hai. " +
                "Doctor: Theek hai, thermometer check kiya? 101 bukhar hai. Aapko Paracetamol 650 mg " +
                "subah aur shaam khane ke baad leni hai teen din tak. Khoob paani pijiye aur aaram kijiye. " +
                "Agar bukhar na utre to do din baad dobara aana.";
        } else if (fn.contains("acidity") || fn.contains("scenario_2")) {
            transcript = "Doctor: Aaiye Sunita ji, kya pareshani hai? " +
                "Patient: Doctor sahab pet mein bahut jalan aur acidity ho rahi hai do teen din se. " +
                "Doctor: Khaane mein tel masala zyada ho raha hai shayad. Main Pantoprazole 40 mg likh raha hoon, " +
                "roz subah khali pet lena paanch din tak. Saath mein Domperidone 10 mg din mein do baar khane se aadha ghanta pehle. " +
                "Teekha aur oily khana bilkul band rakhiye. Paanch din baad checkup ke liye aana.";
        } else if (fn.contains("chest") || fn.contains("scenario_3")) {
            transcript = "Doctor: Haan Amit, kya dikkat hai? " +
                "Patient: Doctor sahab 4 din se gale mein dard hai aur khansi-jukam ho gaya hai. " +
                "Doctor: Gale mein infection lag raha hai. Aapko Azithromycin 500 mg din mein ek baar khane ke baad leni hai 3 din tak. " +
                "Saath mein Cetirizine 10 mg raat ko sone se pehle leni hai 5 din. " +
                "Bukhar ke liye Paracetamol 500 mg jab zarurat ho. Agar aaraam na mile to CBC blood test karwana hoga.";
        } else if (fn.contains("bp") || fn.contains("diabetes") || fn.contains("scenario_4")) {
            transcript = "Doctor: Namaste Sharma ji, regular BP checkup ke liye aaye hain? " +
                "Patient: Ji doctor sahab, thoda sir bhari rehta hai. " +
                "Doctor: BP check kiya hai, 145 by 90 hai, thoda high hai. Aapki purani dawa Telmisartan 40 mg " +
                "roz subah naashte ke baad continue kijiye. Aur diabetes ke liye Metformin 500 mg din mein do baar " +
                "subah shaam khane ke baad. Namak kam kijiye, 30 minute brisk walk roz zaroori hai. " +
                "Ek mahine baad fasting blood sugar check kara ke milna.";
        } else if (fn.contains("scenario_6") || fn.contains("incomplete")) {
            transcript = "Doctor: Namaste Vikas ji, kya dikkat hai? " +
                "Patient: Doctor sahab gale mein kharash hai aur thoda bukhar hai. " +
                "Doctor: Theek hai, main aapko Azithromycin 500 mg likh raha hoon din mein ek baar khana. " +
                "Paracetamol 650 mg bhi lijiye jab bukhar ho. " +
                "Patient: Kitne din lena hai? Doctor: Haan wo aage dekhte hain, filhal aaram karo.";
        } else {
            transcript = "Doctor: Namaste, bataiye kya takleef hai? " +
                "Patient: Doctor sahab kal se sir dard aur tez bukhar hai. " +
                "Doctor: Theek hai, Paracetamol 650 mg subah shaam khane ke baad lena 3 din tak. " +
                "Pantoprazole 40 mg subah khali pet lena. Thanda paani mat pina, aaram karo.";
        }

        return Map.of(
            "transcript", transcript,
            "success", true,
            "metadata", Map.of(
                "model", "prisma-v3.0 (Demo Mode)",
                "detected_language", "hi-IN",
                "latency", 0.45,
                "demo_mode", true
            )
        );
    }

    private List<Map<String, Object>> getBuiltInScenarios() {
        return List.of(
            Map.of("id", "scenario_1_fever", "title", "Viral Fever",
                   "transcript", "Doctor: Namaste Ramesh ji... Paracetamol 650mg subah shaam."),
            Map.of("id", "scenario_2_acidity", "title", "Acidity / Gastritis",
                   "transcript", "Doctor: Aaiye Sunita ji... Pantoprazole 40mg subah khali pet."),
            Map.of("id", "scenario_3_chest_infection", "title", "Chest Infection",
                   "transcript", "Doctor: Haan Amit... Azithromycin 500mg ek baar din mein."),
            Map.of("id", "scenario_4_bp_diabetes", "title", "BP + Diabetes",
                   "transcript", "Doctor: Sharma ji... Telmisartan 40mg + Metformin 500mg."),
            Map.of("id", "scenario_5_skin_allergy", "title", "Skin Allergy",
                   "transcript", "Doctor: Cetirizine 10mg raat ko sone se pehle lena."),
            Map.of("id", "scenario_6_missing_duration", "title", "Incomplete Prescription",
                   "transcript", "Doctor: Azithromycin likh raha hoon, duration baad mein batata hoon.")
        );
    }
}

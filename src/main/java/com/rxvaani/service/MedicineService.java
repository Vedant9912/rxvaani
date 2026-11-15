package com.rxvaani.service;

import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class MedicineService {

    private static final List<String> MEDICINES = Arrays.asList(
        // Analgesics & Antipyretics
        "Paracetamol", "Dolo 650", "Crocin", "Calpol", "Ibuprofen", "Brufen", "Combiflam",
        "Aceclofenac", "Zerodol-P", "Zerodol-SP", "Diclofenac", "Voveran", "Tramadol",
        // Antibiotics
        "Azithromycin", "Azee 500", "Amoxicillin", "Augmentin 625", "Moxikind-CV",
        "Cefixime", "Zifi 200", "Taxim-O", "Ciprofloxacin", "Ofloxacin", "Levofloxacin",
        "Metronidazole", "Flagyl", "Doxycycline", "Norfloxacin", "Norflox-TZ",
        // Antacids & GI
        "Pantoprazole", "Pan 40", "Pan-D", "Pantocid", "Omeprazole", "Omez",
        "Rabeprazole", "Razo 20", "Rabekind", "Domperidone", "Ondansetron", "Emeset",
        "Ranitidine", "Digene", "Gelusil", "Sucralfate", "Cremaffin",
        // Respiratory & Anti-allergic
        "Cetirizine", "Cetzine", "Okacet", "Levocetirizine", "Levocet", "Montelukast",
        "Montair-LC", "Allegra", "Fexofenadine", "Ascoril", "Benadryl",
        "Cheston Cold", "Sinarest", "Solvin Cold",
        // Cardiovascular
        "Telmisartan", "Telma 40", "Telma-H", "Amlodipine", "Amlong", "Atenolol",
        "Losartan", "Enalapril", "Metoprolol", "Ramipril", "Rosuvastatin", "Atorvastatin",
        "Atorva", "Clopidogrel", "Ecosprin 75", "Ecosprin 150",
        // Diabetes
        "Metformin", "Glycomet 500", "Glycomet SR", "Glimepiride", "Amaryl",
        "Teneligliptin", "Vildagliptin", "Galvus", "Dapagliflozin", "Forxiga",
        // Vitamins & Supplements
        "Becosules", "Neurobion Forte", "Shelcal 500", "Calcium Carbonate", "Vitamin D3",
        "Calcirol", "Zincovit", "Limcee", "Folvite", "Livogen", "Evion 400",
        // Topical / Skin
        "Betadine", "Silverex", "Soframycin", "Clobetasol", "Candid B",
        "Ketoconazole", "Ciplox Eye Drops", "Tobrex"
    );

    private final LevenshteinDistance levenshtein = LevenshteinDistance.getDefaultInstance();

    public record MedicineMatchResult(String name, boolean corrected) {}

    public MedicineMatchResult fuzzyMatch(String spokenName) {
        if (spokenName == null || spokenName.isBlank()) {
            return new MedicineMatchResult(spokenName, false);
        }

        String normalized = spokenName.strip();

        // Direct match (case-insensitive)
        for (String med : MEDICINES) {
            if (med.equalsIgnoreCase(normalized)) {
                return new MedicineMatchResult(med, false);
            }
        }

        // Fuzzy match — find minimum Levenshtein distance
        String best = null;
        int bestDist = Integer.MAX_VALUE;

        for (String med : MEDICINES) {
            int dist = levenshtein.apply(normalized.toLowerCase(), med.toLowerCase());
            if (dist < bestDist) {
                bestDist = dist;
                best = med;
            }
        }

        // Accept if within 40% edit distance of the spoken name length
        double threshold = normalized.length() * 0.4;
        if (best != null && bestDist <= threshold) {
            return new MedicineMatchResult(best, true);
        }

        return new MedicineMatchResult(spokenName, false);
    }
}

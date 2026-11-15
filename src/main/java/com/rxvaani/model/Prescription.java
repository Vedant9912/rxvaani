package com.rxvaani.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

public class Prescription {
    private Patient patient = new Patient();
    private List<String> symptoms = new ArrayList<>();
    private String diagnosis;
    private List<Medicine> medicines = new ArrayList<>();
    private List<String> advice = new ArrayList<>();
    private List<String> tests = new ArrayList<>();

    @JsonProperty("follow_up")
    private String followUp;

    @JsonProperty("uncertain_fields")
    private List<String> uncertainFields = new ArrayList<>();

    public Prescription() {}

    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }

    public List<String> getSymptoms() { return symptoms; }
    public void setSymptoms(List<String> symptoms) { this.symptoms = symptoms; }

    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }

    public List<Medicine> getMedicines() { return medicines; }
    public void setMedicines(List<Medicine> medicines) { this.medicines = medicines; }

    public List<String> getAdvice() { return advice; }
    public void setAdvice(List<String> advice) { this.advice = advice; }

    public List<String> getTests() { return tests; }
    public void setTests(List<String> tests) { this.tests = tests; }

    public String getFollowUp() { return followUp; }
    public void setFollowUp(String followUp) { this.followUp = followUp; }

    public List<String> getUncertainFields() { return uncertainFields; }
    public void setUncertainFields(List<String> uncertainFields) { this.uncertainFields = uncertainFields; }
}

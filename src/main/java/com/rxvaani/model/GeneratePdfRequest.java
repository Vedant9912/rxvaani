package com.rxvaani.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GeneratePdfRequest {
    private Prescription prescription;

    @JsonProperty("doctor_name")
    private String doctorName = "Dr. Vedant Sharma, MBBS, MD";

    @JsonProperty("clinic_name")
    private String clinicName = "Aarogya Clinic & Diagnostics";

    @JsonProperty("clinic_address")
    private String clinicAddress = "Shop 12, Health Square, MG Road, New Delhi";

    @JsonProperty("reg_number")
    private String regNumber = "MCI-Reg: 84920/2019";

    public Prescription getPrescription() { return prescription; }
    public void setPrescription(Prescription prescription) { this.prescription = prescription; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public String getClinicName() { return clinicName; }
    public void setClinicName(String clinicName) { this.clinicName = clinicName; }

    public String getClinicAddress() { return clinicAddress; }
    public void setClinicAddress(String clinicAddress) { this.clinicAddress = clinicAddress; }

    public String getRegNumber() { return regNumber; }
    public void setRegNumber(String regNumber) { this.regNumber = regNumber; }
}

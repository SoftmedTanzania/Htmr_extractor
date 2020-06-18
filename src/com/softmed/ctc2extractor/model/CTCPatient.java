package com.softmed.ctc2extractor.model;

import java.util.List;

public class CTCPatient {
    private String healthFacilityCode;
    private String firstName;
    private String surname;
    private String middleName;
    private String ctcNumber;
    private long dateOfBirth;
    private String gender;
    private long dateOfDeath;
    private boolean hivStatus;
    private String ward;
    private String village;
    private String phoneNumber;
    private String careTakerName;
    private String careTakerPhoneNumber;


    private List<PatientAppointment> patientAppointments;

    public String getHealthFacilityCode() {
        return healthFacilityCode;
    }

    public void setHealthFacilityCode(String healthFacilityCode) {
        this.healthFacilityCode = healthFacilityCode;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getCtcNumber() {
        return ctcNumber;
    }

    public void setCtcNumber(String ctcNumber) {
        this.ctcNumber = ctcNumber;
    }

    public long getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(long dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public long getDateOfDeath() {
        return dateOfDeath;
    }

    public void setDateOfDeath(long dateOfDeath) {
        this.dateOfDeath = dateOfDeath;
    }

    public boolean isHivStatus() {
        return hivStatus;
    }

    public void setHivStatus(boolean hivStatus) {
        this.hivStatus = hivStatus;
    }

    public String getWard() {
        return ward;
    }

    public void setWard(String ward) {
        this.ward = ward;
    }

    public String getVillage() {
        return village;
    }

    public void setVillage(String village) {
        this.village = village;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getCareTakerName() {
        return careTakerName;
    }

    public void setCareTakerName(String careTakerName) {
        this.careTakerName = careTakerName;
    }

    public String getCareTakerPhoneNumber() {
        return careTakerPhoneNumber;
    }

    public void setCareTakerPhoneNumber(String careTakerPhoneNumber) {
        this.careTakerPhoneNumber = careTakerPhoneNumber;
    }

    public List<PatientAppointment> getPatientAppointments() {
        return patientAppointments;
    }

    public void setPatientAppointments(List<PatientAppointment> patientAppointments) {
        this.patientAppointments = patientAppointments;
    }

}
package com.softmed.ctc2extractor.model;

public class PatientAppointment {
    private Long dateOfAppointment;
    private int appointmentType;

    //status of 2 = LTF
    //status of 3 = missed Appointment

    private int status;

    public Long getDateOfAppointment() {
        return dateOfAppointment;
    }

    public void setDateOfAppointment(Long dateOfAppointment) {
        this.dateOfAppointment = dateOfAppointment;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getAppointmentType() {
        return appointmentType;
    }

    public void setAppointmentType(int appointmentType) {
        this.appointmentType = appointmentType;
    }
}
package com.javacodegeeks.desktopjava.swing.jframe.Model;

public class PatientAppointment {
	private Long dateOfAppointment;
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
}

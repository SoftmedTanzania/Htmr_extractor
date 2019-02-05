package main.com.softmed.ctc2extractor.Model;

import java.util.List;

public class CTCPatientsModel {
	private List<CTCPatient> ctcPatients;
	private String facilityCTC2Code;
	private String hfrCode;

	public List<CTCPatient> getCtcPatients() {
		return ctcPatients;
	}

	public void setCtcPatients(List<CTCPatient> ctcPatients) {
		this.ctcPatients = ctcPatients;
	}

	public String getFacilityCTC2Code() {
		return facilityCTC2Code;
	}

	public void setFacilityCTC2Code(String facilityCTC2Code) {
		this.facilityCTC2Code = facilityCTC2Code;
	}

	public String getHfrCode() {
		return hfrCode;
	}

	public void setHfrCode(String hfrCode) {
		this.hfrCode = hfrCode;
	}
}

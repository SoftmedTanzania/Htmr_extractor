package com.softmed.ctc2extractor.util;

import com.softmed.ctc2extractor.Model.CTCPatient;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Util {
    public static List<CTCPatient> getLTFBasedOnDateRange(List<CTCPatient> patients, boolean isLTF, Date startDate, Date endDate) {
        List<CTCPatient> obtainedPatients = new ArrayList<>();
        Calendar c1 = Calendar.getInstance();
        try {
            c1.setTimeInMillis(startDate.getTime());
            c1.add(Calendar.DATE, -28);
        } catch (Exception e) {
            e.printStackTrace();
            c1.add(Calendar.YEAR, -1);
        }
        Date mStartDate = c1.getTime();

        Calendar c2 = Calendar.getInstance();
        try {
            c2.setTimeInMillis(endDate.getTime());
            c2.add(Calendar.DATE, -28);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Date mEndDate = c2.getTime();

        for (int i = 0; i < patients.size(); i++) {
            CTCPatient ctcPatient = patients.get(i);
            if (isLTF) {
                Date aDate = new Date(ctcPatient.getPatientAppointments().get(0).getDateOfAppointment());
                if (aDate.after(mStartDate) && aDate.before(mEndDate)) {
                    obtainedPatients.add(ctcPatient);
                }
            } else {
                Date aDate = new Date(ctcPatient.getPatientAppointments().get(0).getDateOfAppointment());
                if (aDate.after(startDate) && aDate.before(endDate)) {
                    obtainedPatients.add(ctcPatient);
                }
            }

        }

        return obtainedPatients;
    }
}

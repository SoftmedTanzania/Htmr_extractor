package com.softmed.ctc2extractor.service;

import com.softmed.ctc2extractor.model.CTCPatient;
import com.softmed.ctc2extractor.util.Utils;
import javafx.application.Platform;
import javafx.scene.control.TextArea;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class ExcelService {
    public TextArea log;
    private Date startDate;
    private Date endDate;

    public ExcelService(TextArea log, Date startDate, Date endDate) {
        this.log = log;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public void generateExcel(List<CTCPatient> missedAppointmentsCTCPatients, List<CTCPatient> ltfsCTCPatients) {

        Platform.runLater(() -> log.appendText("\n\nGenerating EXCEL export"));

        System.out.println("Generating EXCEL export");
        //Blank workbook
        XSSFWorkbook workbook = new XSSFWorkbook();

        createSheet(workbook, Utils.getLTFBasedOnDateRange(ltfsCTCPatients, true, startDate, endDate), "Extracted LTFs", true);
        createSheet(workbook, Utils.getLTFBasedOnDateRange(missedAppointmentsCTCPatients, false, startDate, endDate), "Patients with Missed Appointments", true);

        try {
            SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
            String dateString = format.format(Calendar.getInstance().getTime());

            //Write the workbook in file system
            FileOutputStream out = new FileOutputStream(new File("CTC2 Extracted LTF -  " + dateString + ".xlsx"));
            workbook.write(out);
            out.close();
            System.out.println("ctc2Extractor.xlsx written successfully on disk.");

            Platform.runLater(() -> log.appendText("\n\nLTF Excel file generated successfully"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createSheet(XSSFWorkbook workbook, List<CTCPatient> patients, String sheetName, boolean isLTF) {
        //Create a blank missed appointments sheet
        XSSFSheet missedAppointmentsSheet = workbook.createSheet(sheetName);

        //This data needs to be written (Object[])
        Map<String, Object[]> data = new TreeMap<>();
        data.put("1", new Object[]{"CTC-NUMBER", "NAME", "GENDER", "PHONE NUMBER", "VILLAGE", "WARD", "CARE TAKER NAME", "CARE TAKER PHONE NUMBER", "APPOINTMENT DATE"});

        for (int i = 0; i < patients.size(); i++) {
            CTCPatient ctcPatient = patients.get(i);
            saveData(ctcPatient, i, data);
        }

        String summaryMessage;
        if (isLTF) {
            summaryMessage = "\n\nPatients with LTFs found = : ";
        } else {
            summaryMessage = "\nPatients with Missed Appointments found = : ";
        }

        final int dataCount = data.size() - 1;
        Platform.runLater(() -> {
            log.appendText(summaryMessage + dataCount);
        });

        //Iterate over data and write to sheet
        Set<String> keyset = data.keySet();
        int missedAppointmentRowNum = 0;
        for (String key : keyset) {
            XSSFRow row = missedAppointmentsSheet.createRow(missedAppointmentRowNum++);
            Object[] objArr = data.get(key);
            int cellnum = 0;
            for (Object obj : objArr) {
                Cell cell = row.createCell(cellnum++);
                if (obj instanceof String)
                    cell.setCellValue((String) obj);
                else if (obj instanceof Integer)
                    cell.setCellValue((Integer) obj);
            }
        }

    }

    private void saveData(CTCPatient ctcPatient, int i, Map<String, Object[]> data) {

        String pattern = "dd-MM-yyyy";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

        data.put(String.valueOf((i + 2)), new Object[]{
                ctcPatient.getCtcNumber()
                , ctcPatient.getFirstName() + " " + ctcPatient.getMiddleName() + " " + ctcPatient.getSurname()
                , ctcPatient.getGender()
                , ctcPatient.getPhoneNumber()
                , ctcPatient.getVillage()
                , ctcPatient.getWard()
                , ctcPatient.getCareTakerName()
                , ctcPatient.getCareTakerPhoneNumber()
                , simpleDateFormat.format(new Date(ctcPatient.getPatientAppointments().get(0).getDateOfAppointment()))
        });
    }
}

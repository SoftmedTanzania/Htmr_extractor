package com.softmed.ctc2extractor;

import com.google.gson.Gson;
import com.healthmarketscience.jackcess.*;
import com.jfoenix.controls.JFXDatePicker;
import com.softmed.ctc2extractor.Model.CTCPatient;
import com.softmed.ctc2extractor.Model.CTCPatientsModel;
import com.softmed.ctc2extractor.Model.PatientAppointment;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;

public class Controller implements Initializable {
    private static final String TAG_CTC2_FILE_LOCAITON = "CTC2FileLocation";
    private static String configurationFile = "helper.properties";
    private static String CTC2DatabaseLocation;
    private static Database db;
    private static String regcode = "", discode = "", facility = "", healthcentre = "", centrecode = "", hfrCode = "";
    private static Date todaysDate;

    public Button syncButton;
    public Button exportToExcel;
    public Label facilityName;
    public Label HFRCode;
    public TextArea log;
    public JFXDatePicker startDatePicker;
    public JFXDatePicker endDatePicker;
    private Date startDate, endDate;


    private static void createDefault(String fileName, String sourceFile) throws Exception {
        File file = new File(fileName);
        if (file.exists()) {
            file.renameTo(new File(fileName + ".bak"));
        }
        file.createNewFile();
        PropertiesConfiguration config = new PropertiesConfiguration(
                fileName);
        config.setProperty(TAG_CTC2_FILE_LOCAITON, sourceFile);
        config.save();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Configuration configuration = null;
        try {
            configuration = loadFirst(TAG_CTC2_FILE_LOCAITON, configurationFile);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                createDefault(configurationFile, "");
                configuration = loadFirst(TAG_CTC2_FILE_LOCAITON, configurationFile);
            } catch (Exception e1) {
                e1.printStackTrace();
                log.appendText("\n\nError Encountered : " + e1.getMessage());
            }
        }
        Calendar c = Calendar.getInstance();
        todaysDate = c.getTime();

        try {
            CTC2DatabaseLocation = configuration.getString(TAG_CTC2_FILE_LOCAITON);
            getFacilityConfig(CTC2DatabaseLocation);
        } catch (Exception e) {
            e.printStackTrace();
        }


        if (CTC2DatabaseLocation == null || CTC2DatabaseLocation.equals("")) {
            CTC2DatabaseLocation = "Please select the CTC Database location in settings";
        }

        facilityName.setText("CTC2 File Location : " + CTC2DatabaseLocation);

        final JLabel label4 = new JLabel();
        if (hfrCode.equals("")) {
            HFRCode.setText("Facility HFR Code : ");
        } else {
            HFRCode.setText("HFR Code :  " + hfrCode);
        }

        label4.setAlignmentX(Component.LEFT_ALIGNMENT);


        // show week numbers
        startDatePicker.setShowWeekNumbers(true);
        endDatePicker.setShowWeekNumbers(true);

        // when datePicker is pressed
        startDatePicker.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                // get the date picker value
                startDate = java.sql.Date.valueOf(startDatePicker.getValue());
                System.out.println("Start Date = " + startDate.toString());
            }
        });

        // when datePicker is pressed
        endDatePicker.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                // get the date picker value
                endDate = java.sql.Date.valueOf(endDatePicker.getValue());
                System.out.println("End Date = " + endDate.toString());
            }
        });


    }

    public void ExportToExcel() {
        Runnable r = new Runnable() {
            public void run() {
                if (CTC2DatabaseLocation != null && !hfrCode.equals("")) {
                    ObtainDataFromCTC2(CTC2DatabaseLocation, "export");
                } else {
                    log.appendText("\n\n Please select the correct CTC2 Database location");
                }
            }
        };
        new Thread(r).start();
    }

    public void SyncData() {
        Runnable r = new Runnable() {
            public void run() {
                if (CTC2DatabaseLocation != null && !hfrCode.equals("")) {
                    ObtainDataFromCTC2(CTC2DatabaseLocation, "sync");
                } else {
                    log.appendText("\n\n Please select the correct CTC2 Database location");
                }
            }
        };
        new Thread(r).start();
    }

    Configuration loadFirst(String prefix, String fileNames) {
        try {
            Configuration cf = new PropertiesConfiguration(fileNames)
                    .interpolatedConfiguration();
            System.out.println("loaded properties from " + fileNames);
            return cf;
        } catch (ConfigurationException e) {
            e.printStackTrace();
            log.appendText("\n\nError Encountered : " + e.getMessage());
        }
        System.out.println("Cannot locate configuration: tried," + fileNames);
        // default to an empty configuration
        return null;
    }

    public void getFacilityConfig(String fileLocation) {
        try {
            db = DatabaseBuilder.open(new File(fileLocation));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Table table = null;
        try {
            table = db.getTable("tblConfig");
        } catch (IOException e) {
            e.printStackTrace();
            log.appendText("\n\nError Encountered : " + e.getMessage());
        }


        for (Row row : table) {
            regcode = row.getInt("RegionCode").toString();
            discode = row.getInt("DistrictCode").toString();
            facility = row.getInt("FacilityCode").toString();
            healthcentre = row.getInt("HealthCentreCode").toString();
            hfrCode = row.getString("HFRCode");
        }

        if (regcode.length() == 1) {
            regcode = "0" + regcode;
        }
        if (discode.length() == 1) {
            discode = "0" + discode;
        }
        if (facility.length() == 1) {
            facility = "0" + facility;
        }
        if (healthcentre.length() == 1) {
            healthcentre = "0" + healthcentre;
        }


    }

    public void setLocation() {
        // Button pressed logic goes here

        FileChooser fileChooser = new FileChooser();

        Stage primaryStage = new Stage();
        primaryStage.setTitle("Pick CTC2 DB Location");

        File selectedFile = fileChooser.showOpenDialog(primaryStage);

        System.out.println("Selected file: " + selectedFile.getAbsolutePath());

        try {
            createDefault(configurationFile, selectedFile.getAbsolutePath());
        } catch (Exception e1) {
            e1.printStackTrace();

            log.appendText("\n\nError Encountered : " + e1.getMessage());
        }
        loadFirst(TAG_CTC2_FILE_LOCAITON, configurationFile);
        CTC2DatabaseLocation = selectedFile.getAbsolutePath();
        getFacilityConfig(CTC2DatabaseLocation);
        facilityName.setText("File Location :" + CTC2DatabaseLocation);

        HFRCode.setText("Facility HFR Code  :  " + hfrCode);
    }


    private void ObtainDataFromCTC2(String fileLocation, String state) {
        try {
            System.out.println("CTC DATABASE Location = " + fileLocation);
            db = DatabaseBuilder.open(new File(fileLocation));
        } catch (IOException e) {
            e.printStackTrace();
            log.appendText("\n\nError Encountered : " + e.getMessage());
        }

        CTCPatientsModel ctcPatientsModel = new CTCPatientsModel();

        centrecode = regcode + "-" + discode + "-" + facility + "." + healthcentre;
        System.out.println("centrecode =  " + centrecode);

        Platform.runLater(() -> {
            log.setText("Clinic Centre CTC2 Code : " + centrecode);
            log.appendText("\nDate : " + Calendar.getInstance().getTime().toString());
            log.appendText("\n\n\nObtaining patient appointments from CTC2 database");
        });


        ctcPatientsModel.setFacilityCTC2Code(centrecode);
        ctcPatientsModel.setHfrCode(hfrCode);


        Table tblPatients = null;
        try {
            tblPatients = db.getTable("tblPatients");
        } catch (IOException e) {
            e.printStackTrace();
        }

        Table tblStatus = null;
        try {
            tblStatus = db.getTable("tblStatus");
        } catch (IOException e) {
            e.printStackTrace();
        }

        Table tblAppointments = null;
        try {
            tblAppointments = db.getTable("tblAppointments");
        } catch (IOException e) {
            e.printStackTrace();
        }

        Table tblVisits = null;
        try {
            tblVisits = db.getTable("tblVisits");
        } catch (IOException e) {
            e.printStackTrace();
        }

        Table tblPregnancies = null;
        try {
            tblPregnancies = db.getTable("tblPregnancies");
        } catch (IOException e) {
            e.printStackTrace();
        }


        //Obtaining data
        final java.util.List<CTCPatient> ctcLTFPatients = new ArrayList<>();
        final java.util.List<CTCPatient> ctcMissedAppointmentsPatients = new ArrayList<>();
        System.out.println("Patients Information");
        for (final Row patient : tblPatients) {
            CTCPatient ctcPatient = new CTCPatient();
            ctcPatient.setHealthFacilityCode(centrecode);
            try {
                ctcPatient.setFirstName(patient.getString("FirstName").split(" ")[0]);
                ctcPatient.setMiddleName(patient.getString("FirstName").split(" ")[1]);
                System.out.println("Middle Name : " + ctcPatient.getMiddleName());
            } catch (Exception e) {
                ctcPatient.setMiddleName("");
                e.printStackTrace();
            }

            ctcPatient.setSurname(patient.getString("SurName"));
            ctcPatient.setCtcNumber(patient.getString("PatientID"));
            ctcPatient.setPhoneNumber(patient.getString("Contact"));
            ctcPatient.setVillage(patient.getString("VillageMtaa"));
            ctcPatient.setWard(patient.getString("WardName"));
            ctcPatient.setCareTakerName(patient.getString("Helper"));
            ctcPatient.setCareTakerPhoneNumber(patient.getString("HelperContact"));
            ctcPatient.setDateOfBirth(patient.getDate("DateOfBirth").getTime());
            ctcPatient.setGender(patient.getString("Sex"));
            try {
                ctcPatient.setDateOfDeath(patient.getDate("DateOfDeath").getTime());
                continue;
            } catch (Exception e) {
                e.printStackTrace();
            }
            ctcPatient.setHivStatus(true);

            List<PatientAppointment> missedAppointments = new ArrayList<PatientAppointment>();
            List<PatientAppointment> ltfAppointments = new ArrayList<PatientAppointment>();
            int missedAppointmentCount = 0;
            int ltfAppointmentCount = 0;

            IndexCursor statusCursor = null;
            try {
                statusCursor = CursorBuilder.createCursor(tblStatus.getIndex("PatientID"));
            } catch (IOException e) {
                e.printStackTrace();
            }

            IndexCursor cursor = null;
            try {
                cursor = CursorBuilder.createCursor(tblAppointments.getIndex("PatientID"));
            } catch (IOException e) {
                e.printStackTrace();
            }

            Row appointment = null;
            for (Row app : cursor.newEntryIterable(patient.getString("PatientID"))) {
                appointment = app;
            }

            //Calculating the date of the last 28 days from now
            Date _28DaysAgo = new Date();
            Calendar c1 = Calendar.getInstance();

            c1.add(Calendar.DATE, -28);
            _28DaysAgo = c1.getTime();

            //Calculating the date of the last 3 days from now
            Date _3DaysAgo = new Date();
            Calendar c3 = Calendar.getInstance();

            c3.add(Calendar.DATE, -3);
            _3DaysAgo = c3.getTime();


            //Calculating the date of the last 28 days from now
            Date _1yearsAgo = new Date();
            Calendar c2 = Calendar.getInstance();
            c2.add(Calendar.YEAR, -1);
            _1yearsAgo = c2.getTime();

            try {

                //Obtaining all missed appointments in the last 3 days
                if (appointment.getDate("DateOfAppointment").before(_3DaysAgo) &&
                        appointment.getDate("DateOfAppointment").after(_28DaysAgo) &&
                        appointment.getInt("Cancelled") == 0) {
                    boolean hasVisited = checkIfTheClientHasVisitedTheFacility(appointment, patient, _3DaysAgo, tblVisits);
                    if (!hasVisited) {
                        PatientAppointment missedAppointment = createMissedAppointment(appointment, patient, ctcPatient, tblPregnancies);

                        //status of 1 = missed Appointment
                        missedAppointment.setStatus(1);
                        missedAppointments.add(missedAppointment);
                        missedAppointmentCount++;
                    }
                } else //Obtaining all LTF appointments in the last 28 days
                    if (appointment.getDate("DateOfAppointment").before(_28DaysAgo) &&
                            appointment.getDate("DateOfAppointment").after(_1yearsAgo) &&
                            appointment.getInt("Cancelled") == 0) {
                        boolean hasVisited = checkIfTheClientHasVisitedTheFacility(appointment, patient, _28DaysAgo, tblVisits);

                        if (!hasVisited) {
                            PatientAppointment ltfAppointment = createMissedAppointment(appointment, patient, ctcPatient, tblPregnancies);

                            //status of 2 = LTF
                            ltfAppointment.setStatus(2);
                            ltfAppointments.add(ltfAppointment);
                            ltfAppointmentCount++;
                        }
                    }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (missedAppointmentCount > 0 || ltfAppointmentCount > 0) {
                Row statusRow = null;
                for (Row tempRow : statusCursor.newEntryIterable(patient.getString("PatientID"))) {
                    if (statusRow == null) {
                        statusRow = tempRow;
                    } else if (statusRow.getDate("StatusDate") == null) {
                        statusRow = tempRow;
                    } else if (statusRow.getDate("StatusDate").before(tempRow.getDate("StatusDate")))
                        statusRow = tempRow;


                    System.out.println("Status : " + statusRow.getString("Status") + " Date : " + statusRow.getDate("StatusDate").toString());
                }


                while (true) {
                    try {
                        if (!statusCursor.findNextRow(Collections.singletonMap("PatientID", patient.getString("PatientID"))))
                            break;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    try {
                        if (statusRow == null) {
                            statusRow = statusCursor.getCurrentRow();
                        } else if (statusRow.getDate("StatusDate") == null) {
                            statusRow = statusCursor.getCurrentRow();
                        } else if (statusRow.getDate("StatusDate").before(statusCursor.getCurrentRow().getDate("StatusDate")))
                            statusRow = statusCursor.getCurrentRow();

                        System.out.println("Status : " + statusRow.getString("Status") + " Date : " + statusRow.getDate("StatusDate").toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

                if (statusRow != null && !statusRow.getString("Status").toLowerCase().contains("transferred") && !statusRow.getString("Status").toLowerCase().contains("died") && !statusRow.getString("Status").toLowerCase().contains("opted")) {
                    if (missedAppointmentCount > 0) {
                        ctcPatient.setPatientAppointments(missedAppointments);
                        ctcMissedAppointmentsPatients.add(ctcPatient);
                    } else if (ltfAppointmentCount > 0) {
                        ctcPatient.setPatientAppointments(ltfAppointments);
                        ctcLTFPatients.add(ctcPatient);
                    }

                    System.out.println("*****************************************************************************");
                    System.out.println("PatientID = " + patient.getString("PatientID"));
                    System.out.println("*****************************************************************************");


                    if (missedAppointmentCount > 0) {
                        Platform.runLater(() -> log.appendText("\nChecking Missed Appointment Patient = : " + patient.getString("PatientID")));
                    } else if (ltfAppointmentCount > 0) {
                        Platform.runLater(() -> log.appendText("\nChecking LTF Patient = : " + patient.getString("PatientID")));
                    }
                }


            }
        }

        java.util.List<CTCPatient> missedAndLTFAppointmentsPatients = new ArrayList<>();
        missedAndLTFAppointmentsPatients.addAll(ctcMissedAppointmentsPatients);
        missedAndLTFAppointmentsPatients.addAll(ctcLTFPatients);

        ctcPatientsModel.setCtcPatientsDTOS(missedAndLTFAppointmentsPatients);

        System.out.println("Patients found = " + missedAndLTFAppointmentsPatients.size());

        generateExcel(ctcMissedAppointmentsPatients,ctcLTFPatients);
        if (state.equalsIgnoreCase("sync")) {
            syncData(ctcPatientsModel);
        }
    }

    private boolean checkIfTheClientHasVisitedTheFacility(Row appointment, Row patient, Date visitAppointmentDate, Table tblVisits) {
        boolean hasVisited = false;

        IndexCursor visitsCursor = null;
        try {
            visitsCursor = CursorBuilder.createCursor(tblVisits.getIndex("PatientID"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (Row visit : visitsCursor.newEntryIterable(patient.getString("PatientID"))) {
            try {
                Date visitDate = visit.getDate("VisitDate");
                Calendar c = Calendar.getInstance();
                c.setTime(appointment.getDate("DateAppointmentGiven"));
                Date appDate = c.getTime();

                if ((visitDate.after(visitAppointmentDate) || visitDate.after(appDate)) &&
                        visitDate.before(todaysDate) &&
                        visit.getString("PatientID").equals(appointment.getString("PatientID"))) {
                    hasVisited = true;
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return hasVisited;
    }

    private PatientAppointment createMissedAppointment(Row appointment, Row patient, CTCPatient ctcPatient, Table tblPregnancies) {
        PatientAppointment missedAppointment = new PatientAppointment();
        missedAppointment.setDateOfAppointment(appointment.getDate("DateOfAppointment").getTime());
        missedAppointment.setStatus(-1);

        //setting the appointment type to be CTC appointment by default and updating it if the patient is a PMTCT case
        missedAppointment.setAppointmentType(1);

        //checking if the mother is pregnant, i.e has pregnancies that their due dates are after today
        if (ctcPatient.getGender().equalsIgnoreCase("female")) {

            IndexCursor pregnancyCursor = null;
            try {
                pregnancyCursor = CursorBuilder.createCursor(tblPregnancies.getIndex("PatientID"));
            } catch (IOException e) {
                e.printStackTrace();
            }

            for (Row pregnancy : pregnancyCursor.newEntryIterable(patient.getString("PatientID"))) {
                try {
                    Date dateOfBirth = pregnancy.getDate("DateOfBirth");
                    if (dateOfBirth == null
                            && pregnancy.getDate("DueDate").after(todaysDate)
                    ) {
                        //Pregnant mother found.
                        System.out.println("Pregnant mother found = " + new Gson().toJson(ctcPatient));
                        missedAppointment.setAppointmentType(2);
                        break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return missedAppointment;
    }

    private void syncData(CTCPatientsModel ctcPatientsModel) {
        System.out.println("Sending data to server");
        log.appendText("\n\nSending data to server");
        String json = new Gson().toJson(ctcPatientsModel);
        HttpClient httpClient = new DefaultHttpClient();
        String username = "admin";
        String password = "Admin123";

        byte[] encodedPassword = (username + ":" + password).getBytes();

        try {
            HttpPost request = new HttpPost("http://139.162.184.148:8080/opensrp/save-ctc-patients");
            StringEntity params = new StringEntity(json);
            request.addHeader("content-type", "application/json");
            request.addHeader("Accept", "application/json");
            request.addHeader("Authorization", "Basic " + Base64.encodeBase64String(encodedPassword));

            request.setEntity(params);
            HttpResponse response = httpClient.execute(request);

            //handle response here...
            System.out.println("Server response : " + response.getStatusLine());


            if (response.getStatusLine().getStatusCode() == 201)
                log.appendText("\nData sent successfully");
            else
                log.appendText("\nError sending data to the server");
        } catch (Exception ex) {
            ex.printStackTrace();
            log.appendText("\nError sending data");
        } finally {
            httpClient.getConnectionManager().shutdown();
        }
    }

    private void generateExcel(List<CTCPatient> missedAppointmentsCTCPatients,List<CTCPatient> ltfsCTCPatients) {

        Platform.runLater(() -> log.appendText("\n\nGenerating EXCEL export"));

        System.out.println("Generating EXCEL export");
        //Blank workbook
        XSSFWorkbook workbook = new XSSFWorkbook();

        createSheet(workbook,ltfsCTCPatients,"Extracted LTFs",true);
        createSheet(workbook,missedAppointmentsCTCPatients,"Patients with Missed Appointments",false);


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


    private void createSheet(XSSFWorkbook workbook,List<CTCPatient> patients,String sheetName, boolean isLTF){

        //Create a blank missed appointments sheet
        XSSFSheet missedAppointmentsSheet = workbook.createSheet(sheetName);

        //This data needs to be written (Object[])
        Map<String, Object[]> data = new TreeMap<>();
        data.put("1", new Object[]{"CTC-NUMBER", "NAME", "GENDER", "PHONE NUMBER", "VILLAGE", "WARD", "CARE TAKER NAME", "CARE TAKER PHONE NUMBER","APPOINTMENT DATE"});

        Calendar c1 = Calendar.getInstance();
        try {
            c1.setTimeInMillis(startDate.getTime());
            c1.add(Calendar.DATE, -28);
        }catch (Exception e){
            e.printStackTrace();
            c1.add(Calendar.YEAR, -1);
        }
        Date mStartDate = c1.getTime();

        Calendar c2 = Calendar.getInstance();
        try {
            c2.setTimeInMillis(endDate.getTime());
            c2.add(Calendar.DATE, -28);
        }catch (Exception e){
            e.printStackTrace();
        }

        Date mEndDate = c2.getTime();

        for (int i = 0; i < patients.size(); i++) {
            CTCPatient ctcPatient = patients.get(i);
            if(isLTF){
                Date aDate = new Date(ctcPatient.getPatientAppointments().get(0).getDateOfAppointment());
                if(aDate.after(mStartDate) && aDate.before(mEndDate)){
                    saveData(ctcPatient,i,data);
                }
            }else{
                Date aDate = new Date(ctcPatient.getPatientAppointments().get(0).getDateOfAppointment());
                if(aDate.after(startDate) && aDate.before(endDate)){
                    saveData(ctcPatient,i,data);
                }
            }

        }

        String summaryMessage;
        if(isLTF) {
            summaryMessage = "\n\nPatients with LTFs found = : ";
        }else{
            summaryMessage = "\nPatients with Missed Appointments found = : ";
        }

        Platform.runLater(() -> {
            log.appendText(summaryMessage + (data.size()-1));
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

    private void saveData(CTCPatient ctcPatient, int i, Map<String, Object[]> data ){

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

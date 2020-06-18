package com.softmed.ctc2extractor;

import com.google.gson.Gson;
import com.healthmarketscience.jackcess.CursorBuilder;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.IndexCursor;
import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDatePicker;
import com.softmed.ctc2extractor.model.CTCPatient;
import com.softmed.ctc2extractor.model.CTCPatientsModel;
import com.softmed.ctc2extractor.model.PatientAppointment;
import com.softmed.ctc2extractor.service.ExcelService;
import com.softmed.ctc2extractor.util.Utils;
import javafx.application.Platform;
import javafx.fxml.Initializable;
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

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import static com.softmed.ctc2extractor.util.Constants.BASE_SERVER_URL;
import static com.softmed.ctc2extractor.util.Constants.PASSWORD;
import static com.softmed.ctc2extractor.util.Constants.USERNAME;
import static com.softmed.ctc2extractor.service.OpenSrpService.generateClientEvent;

public class Controller implements Initializable {
    private static final String TAG_CTC2_FILE_LOCAITON = "CTC2FileLocation";
    private static String configurationFile = "helper.properties";
    private static String CTC2DatabaseLocation;
    private static Database db;
    private static String regcode = "", discode = "", facility = "", healthcentre = "", centrecode = "", hfrCode = "";
    private static Date todaysDate;

    public Label facilityName;
    public Label HFRCode;
    public TextArea log;
    public JFXDatePicker startDatePicker;
    public JFXDatePicker endDatePicker;
    public JFXButton exportToExcel;
    public JFXButton syncButton;
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
            configuration = loadFirst(configurationFile);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                createDefault(configurationFile, "");
                configuration = loadFirst(configurationFile);
            } catch (Exception e1) {
                e1.printStackTrace();
                log.appendText("\n\nError Encountered : " + e1.getMessage());
            }
        }
        Calendar c = Calendar.getInstance();
        todaysDate = c.getTime();
        endDate = c.getTime();

        Calendar myCalendar = Calendar.getInstance();
        myCalendar.add(Calendar.YEAR, -1);
        startDate = myCalendar.getTime();

        try {
            if (configuration != null) {
                CTC2DatabaseLocation = configuration.getString(TAG_CTC2_FILE_LOCAITON);
            }
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
        startDatePicker.setOnAction(e -> {
            // get the date picker value
            startDate = java.sql.Date.valueOf(startDatePicker.getValue());
            System.out.println("Start Date = " + startDate.toString());
        });

        // when datePicker is pressed
        endDatePicker.setOnAction(e -> {
            // get the date picker value
            endDate = java.sql.Date.valueOf(endDatePicker.getValue());
            System.out.println("End Date = " + endDate.toString());
        });


    }

    public void ExportToExcel() {
        Runnable r = () -> {
            if (CTC2DatabaseLocation != null && !hfrCode.equals("")) {
                ObtainDataFromCTC2(CTC2DatabaseLocation, "export");
            } else {
                log.appendText("\n\n Please select the correct CTC2 Database location");
            }
        };
        new Thread(r).start();
    }

    public void SyncData() {
        Runnable r = () -> {
            if (CTC2DatabaseLocation != null && !hfrCode.equals("")) {
                ObtainDataFromCTC2(CTC2DatabaseLocation, "sync");
            } else {
                log.appendText("\n\n Please select the correct CTC2 Database location");
            }
        };
        new Thread(r).start();
    }

    Configuration loadFirst(String fileNames) {
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
        loadFirst(configurationFile);
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
        if (tblPatients != null) {
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
                    if (tblStatus != null) {
                        statusCursor = CursorBuilder.createCursor(tblStatus.getIndex("PatientID"));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                IndexCursor cursor = null;
                try {
                    if (tblAppointments != null) {
                        cursor = CursorBuilder.createCursor(tblAppointments.getIndex("PatientID"));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Row appointment = null;
                if (cursor != null) {
                    for (Row app : cursor.newEntryIterable(patient.getString("PatientID"))) {
                        appointment = app;
                    }
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
                    Date appointmentDate = appointment.getDate("DateOfAppointment");

                    //Obtaining all missed appointments in the last 3 days
                    if (appointmentDate.before(_3DaysAgo) &&
                            appointment.getDate("DateOfAppointment").after(_28DaysAgo)) {

                        int cancelled = 0;
                        try {
                            cancelled = appointment.getInt("Cancelled");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if (cancelled == 0) {
                            boolean hasVisited = checkIfTheClientHasVisitedTheFacility(appointment, patient, _3DaysAgo, tblVisits);
                            if (!hasVisited) {
                                PatientAppointment missedAppointment = createMissedAppointment(appointment, patient, ctcPatient, tblPregnancies);

                                //status of 3 = missed Appointment
                                missedAppointment.setStatus(3);
                                missedAppointments.add(missedAppointment);
                                missedAppointmentCount++;
                            }
                        }
                    } else if (appointmentDate.before(_28DaysAgo) &&
                            appointment.getDate("DateOfAppointment").after(_1yearsAgo)) {  //Obtaining all LTF appointments in the last 28 days

                        int cancelled = 0;
                        try {
                            cancelled = appointment.getInt("Cancelled");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if (cancelled == 0) {
                            boolean hasVisited = checkIfTheClientHasVisitedTheFacility(appointment, patient, _28DaysAgo, tblVisits);

                            if (!hasVisited) {
                                PatientAppointment ltfAppointment = createMissedAppointment(appointment, patient, ctcPatient, tblPregnancies);

                                //status of 2 = LTF
                                ltfAppointment.setStatus(2);
                                ltfAppointments.add(ltfAppointment);
                                ltfAppointmentCount++;
                            }
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
        }

        java.util.List<CTCPatient> missedAndLTFAppointmentsPatients = new ArrayList<>();
        missedAndLTFAppointmentsPatients.addAll(Utils.getLTFBasedOnDateRange(ctcMissedAppointmentsPatients, false, startDate, endDate));
        missedAndLTFAppointmentsPatients.addAll(Utils.getLTFBasedOnDateRange(ctcLTFPatients, true, startDate, endDate));

        ctcPatientsModel.setCtcPatientsDTOS(missedAndLTFAppointmentsPatients);
        System.out.println("Patients found = " + missedAndLTFAppointmentsPatients.size());

        ExcelService excelService = new ExcelService(log, startDate, endDate);
        excelService.generateExcel(ctcMissedAppointmentsPatients, ctcLTFPatients);
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

        if (visitsCursor != null) {
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

        String json = generateClientEvent(ctcPatientsModel);

        System.out.println("Data = " + json);
        HttpClient httpClient = new DefaultHttpClient();

        byte[] encodedPassword = (USERNAME + ":" + PASSWORD).getBytes();

        try {
            HttpPost request = new HttpPost(BASE_SERVER_URL + "rest/event/add");
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
}

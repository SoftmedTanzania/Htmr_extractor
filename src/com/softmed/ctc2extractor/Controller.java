package com.softmed.ctc2extractor;

import com.google.gson.Gson;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDatePicker;
import com.softmed.ctc2extractor.Model.CTCPatient;
import com.softmed.ctc2extractor.Model.CTCPatientsModel;
import com.softmed.ctc2extractor.Model.PatientAppointment;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.*;

public class Controller implements Initializable {
    private static final String TAG_SQLSERVER_USERNAME = "sqlServerUsername";
    private static final String TAG_SQLSERVER_PASSWORD = "sqlServerPassword";
    private static final String TAG_SQLSERVER_DB = "sqlServerDB";
    private static String configurationFile = "helper.properties";
    private static String username,
            dbName,
            password;
    private static String regcode = "", discode = "", facility = "", healthcentre = "", centrecode = "", hfrCode = "";
    private static Date todaysDate;

    public Label DatabaseNameLabel;
    public Label HFRCode;
    public TextArea log;
    public JFXDatePicker startDatePicker;
    public JFXDatePicker endDatePicker;
    public JFXButton exportToExcel;
    public JFXButton syncButton;
    private Date startDate, endDate;


    private static void createDefault(String fileName, String sqlServerUsername, String sqlServerPassword, String sqlServerDB) throws Exception {
        File file = new File(fileName);
        if (file.exists()) {
            file.renameTo(new File(fileName + ".bak"));
        }
        file.createNewFile();
        PropertiesConfiguration config = new PropertiesConfiguration(
                fileName);
        config.setProperty(TAG_SQLSERVER_USERNAME, sqlServerUsername);
        config.setProperty(TAG_SQLSERVER_PASSWORD, sqlServerPassword);
        config.setProperty(TAG_SQLSERVER_DB, sqlServerDB);
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
                createDefault(configurationFile, "", "", "");
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
            username = configuration.getString(TAG_SQLSERVER_USERNAME);
            password = configuration.getString(TAG_SQLSERVER_PASSWORD);
            dbName = configuration.getString(TAG_SQLSERVER_USERNAME);


            dbName = "CTC2data107872_4";
            username = "extractor";
            password = "123";

            try {
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            String connectionUrl = "jdbc:sqlserver://192.168.1.110\\CTC2NSTANCE:1433;databaseName=" + dbName + ";user=" + username + ";password=" + password;
            try (Connection con = DriverManager.getConnection(connectionUrl); Statement stmt = con.createStatement();) {
                getFacilityConfig(stmt);
            } catch (Exception e) {
                e.printStackTrace();
            }


        } catch (Exception e) {
            e.printStackTrace();
        }


        if (username == null || username.equals("")) {
            username = "Please set username the Computer in settings";
        }

        DatabaseNameLabel.setText("CTC2 File Location : " + username);

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
                if (username != null && !hfrCode.equals("")) {
                    ObtainDataFromCTC2(username, "export");
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
                if (username != null && !hfrCode.equals("")) {
                    ObtainDataFromCTC2(username, "sync");
                } else {
                    log.appendText("\n\n Please select the correct CTC2 Database location");
                }
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

    public void getFacilityConfig(Statement stmt) {

        ResultSet rsConfig;
        String SqlConfig = "SELECT * FROM dbo.tblConfig ";


        try {
            rsConfig = stmt.executeQuery(SqlConfig);
            rsConfig.next();

            regcode = rsConfig.getString("RegionCode");
            discode = rsConfig.getString("DistrictCode");
            facility = rsConfig.getString("FacilityCode");
            healthcentre = rsConfig.getString("HealthCentreCode");
            hfrCode = rsConfig.getString("HFRCode");

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
        } catch (Exception e) {
            e.printStackTrace();
            log.appendText("\n\nError Encountered : " + e.getMessage());
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
            createDefault(configurationFile, selectedFile.getAbsolutePath(), "", "");
        } catch (Exception e1) {
            e1.printStackTrace();

            log.appendText("\n\nError Encountered : " + e1.getMessage());
        }
        loadFirst(configurationFile);


        dbName = "CTC2data107872_4";
        username = "extractor";
        password = "123";

        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        String connectionUrl = "jdbc:sqlserver://192.168.1.110\\CTC2NSTANCE:1433;databaseName=" + dbName + ";user=" + username + ";password=" + password;
        try (Connection con = DriverManager.getConnection(connectionUrl); Statement stmt = con.createStatement();) {
            getFacilityConfig(stmt);
        } catch (Exception e) {
            e.printStackTrace();
        }


        DatabaseNameLabel.setText("File Location :" + username);

        HFRCode.setText("Facility HFR Code  :  " + hfrCode);
    }


    private void ObtainDataFromCTC2(String fileLocation, String state) {

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

        //Obtaining data
        final java.util.List<CTCPatient> ctcLTFPatients = new ArrayList<>();
        final java.util.List<CTCPatient> ctcMissedAppointmentsPatients = new ArrayList<>();
        System.out.println("Patients Information");

        String connectionUrl = "jdbc:sqlserver://192.168.1.110\\CTC2NSTANCE:1433;databaseName=" + dbName + ";user=" + username + ";password=" + password;
        try (Connection con = DriverManager.getConnection(connectionUrl); Statement stmt = con.createStatement();) {
            String SQL = "SELECT  * FROM dbo.tblPatients";
            ResultSet rsPatient = stmt.executeQuery(SQL);


            // Iterate through the data in the result set and display it.
            while (rsPatient.next()) {

                CTCPatient ctcPatient = new CTCPatient();
                ctcPatient.setHealthFacilityCode(centrecode);
                try {
                    ctcPatient.setFirstName(rsPatient.getString("FirstName").split(" ")[0]);
                    ctcPatient.setMiddleName(rsPatient.getString("FirstName").split(" ")[1]);
                } catch (Exception e) {
                    ctcPatient.setMiddleName("");
                    e.printStackTrace();
                }

                ctcPatient.setSurname(rsPatient.getString("SurName"));
                ctcPatient.setCtcNumber(rsPatient.getString("PatientID"));
                ctcPatient.setPhoneNumber(rsPatient.getString("Contact"));
                ctcPatient.setVillage(rsPatient.getString("VillageMtaa"));
                ctcPatient.setWard(rsPatient.getString("WardName"));
                ctcPatient.setCareTakerName(rsPatient.getString("Helper"));
                ctcPatient.setCareTakerPhoneNumber(rsPatient.getString("HelperContact"));
                ctcPatient.setDateOfBirth(rsPatient.getDate("DateOfBirth").getTime());
                ctcPatient.setGender(rsPatient.getString("Sex"));
                try {
                    ctcPatient.setDateOfDeath(rsPatient.getDate("DateOfDeath").getTime());
                    continue;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                ctcPatient.setHivStatus(true);

                List<PatientAppointment> missedAppointments = new ArrayList<PatientAppointment>();
                List<PatientAppointment> ltfAppointments = new ArrayList<PatientAppointment>();
                int missedAppointmentCount = 0;
                int ltfAppointmentCount = 0;


                String SqlLastAppointment = "SELECT TOP(1) * FROM dbo.tblAppointments WHERE PatientID='" + ctcPatient.getCtcNumber() + "' ORDER BY DateAppointmentGiven DESC";
                ResultSet rsAppointment = stmt.executeQuery(SqlLastAppointment);
                rsAppointment.next();

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
                    Date appointmentDate = rsAppointment.getDate("DateOfAppointment");

                    //Obtaining all missed appointments in the last 3 days
                    if (appointmentDate.before(_3DaysAgo) &&
                            rsAppointment.getDate("DateOfAppointment").after(_28DaysAgo)) {

                        int cancelled = 0;
                        try {
                            cancelled = rsAppointment.getInt("Cancelled");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if (cancelled == 0) {
                            boolean hasVisited = checkIfTheClientHasVisitedTheFacility(rsAppointment, ctcPatient, _3DaysAgo, stmt);
                            if (!hasVisited) {
                                PatientAppointment missedAppointment = createMissedAppointment(appointmentDate, ctcPatient, stmt);

                                //status of 3 = missed Appointment
                                missedAppointment.setStatus(3);
                                missedAppointments.add(missedAppointment);
                                missedAppointmentCount++;
                            }
                        }
                    } else if (appointmentDate.before(_28DaysAgo) &&
                            rsAppointment.getDate("DateOfAppointment").after(_1yearsAgo)) {  //Obtaining all LTF appointments in the last 28 days

                        int cancelled = 0;
                        try {
                            cancelled = rsAppointment.getInt("Cancelled");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if (cancelled == 0) {
                            boolean hasVisited = checkIfTheClientHasVisitedTheFacility(rsAppointment, ctcPatient, _28DaysAgo, stmt);

                            if (!hasVisited) {
                                PatientAppointment ltfAppointment = createMissedAppointment(appointmentDate, ctcPatient, stmt);

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

                    String SqlStatus = "SELECT TOP(1) * FROM dbo.tblStatus WHERE PatientID='" + ctcPatient.getCtcNumber() + "' ORDER BY StatusDate DESC";
                    ResultSet rsStatus = stmt.executeQuery(SqlStatus);
                    rsStatus.next();


                    if (!rsStatus.getString("Status").toLowerCase().contains("transferred") && !rsStatus.getString("Status").toLowerCase().contains("died") && !rsStatus.getString("Status").toLowerCase().contains("opted")) {
                        if (missedAppointmentCount > 0) {
                            ctcPatient.setPatientAppointments(missedAppointments);
                            ctcMissedAppointmentsPatients.add(ctcPatient);
                        } else {
                            ctcPatient.setPatientAppointments(ltfAppointments);
                            ctcLTFPatients.add(ctcPatient);
                        }

                        System.out.println("*****************************************************************************");
                        System.out.println("PatientID = " + rsPatient.getString("PatientID"));
                        System.out.println("*****************************************************************************");


                        if (missedAppointmentCount > 0) {
                            Platform.runLater(() -> log.appendText("\nChecking Missed Appointment Patient = : " + ctcPatient.getCtcNumber()));
                        } else if (ltfAppointmentCount > 0) {
                            Platform.runLater(() -> log.appendText("\nChecking LTF Patient = : " + ctcPatient.getCtcNumber()));
                        }
                    }


                    rsStatus.close();


                }

            }

        }
        // Handle any errors that may have occurred.
        catch (SQLException e) {
            e.printStackTrace();
            log.appendText("\n\nError Encountered : " + e.getMessage());
        }

        java.util.List<CTCPatient> missedAndLTFAppointmentsPatients = new ArrayList<>();
        missedAndLTFAppointmentsPatients.addAll(ctcMissedAppointmentsPatients);
        missedAndLTFAppointmentsPatients.addAll(ctcLTFPatients);

        ctcPatientsModel.setCtcPatientsDTOS(missedAndLTFAppointmentsPatients);

        System.out.println("Patients found = " + missedAndLTFAppointmentsPatients.size());

        generateExcel(ctcMissedAppointmentsPatients, ctcLTFPatients);
        if (state.equalsIgnoreCase("sync")) {
            syncData(ctcPatientsModel);
        }
    }

    private boolean checkIfTheClientHasVisitedTheFacility(ResultSet appointment, CTCPatient patient, Date visitAppointmentDate, Statement stmt) {
        boolean hasVisited = false;


        String SqlLastVisit = "SELECT TOP(1) * FROM dbo.tblVisits WHERE PatientID='" + patient.getCtcNumber() + "' ORDER BY VisitDate DESC";

        ResultSet rsVisit = null;
        try {
            rsVisit = stmt.executeQuery(SqlLastVisit);
            rsVisit.next();
            Date visitDate = rsVisit.getDate("VisitDate");
            Calendar c = Calendar.getInstance();
            c.setTime(appointment.getDate("DateAppointmentGiven"));
            Date appDate = c.getTime();

            if ((visitDate.after(visitAppointmentDate) || visitDate.after(appDate)) &&
                    visitDate.before(todaysDate)) {
                hasVisited = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                rsVisit.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return hasVisited;
    }

    private PatientAppointment createMissedAppointment(Date dateOfAppointment, CTCPatient ctcPatient, Statement stmt) {
        PatientAppointment missedAppointment = new PatientAppointment();
        missedAppointment.setDateOfAppointment(dateOfAppointment.getTime());
        missedAppointment.setStatus(-1);

        //setting the appointment type to be CTC appointment by default and updating it if the patient is a PMTCT case
        missedAppointment.setAppointmentType(1);

        //checking if the mother is pregnant, i.e has pregnancies that their due dates are after today
        if (ctcPatient.getGender().equalsIgnoreCase("female")) {


            ResultSet rsPregnancy = null;
            String SqlLastPregnancy = "SELECT TOP(1) * FROM dbo.tblPregnancies WHERE PatientID='" + ctcPatient.getCtcNumber() + "' ORDER BY DueDate DESC";


            try {
                rsPregnancy = stmt.executeQuery(SqlLastPregnancy);
                rsPregnancy.next();

                Date dateOfBirth = rsPregnancy.getDate("DateOfBirth");
                if (dateOfBirth == null && rsPregnancy.getDate("DueDate").after(todaysDate)) {
                    //Pregnant mother found.
                    System.out.println("Pregnant mother found = " + new Gson().toJson(ctcPatient));
                    missedAppointment.setAppointmentType(2);
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    rsPregnancy.close();
                } catch (SQLException e) {
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

        System.out.println("Data = " + json);
        HttpClient httpClient = new DefaultHttpClient();
        String username = "username";
        String password = "password";

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

    private void generateExcel(List<CTCPatient> missedAppointmentsCTCPatients, List<CTCPatient> ltfsCTCPatients) {

        Platform.runLater(() -> log.appendText("\n\nGenerating EXCEL export"));

        System.out.println("Generating EXCEL export");
        //Blank workbook
        XSSFWorkbook workbook = new XSSFWorkbook();

        createSheet(workbook, ltfsCTCPatients, "Extracted LTFs", true);
        createSheet(workbook, missedAppointmentsCTCPatients, "Patients with Missed Appointments", false);


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
                    saveData(ctcPatient, i, data);
                }
            } else {
                Date aDate = new Date(ctcPatient.getPatientAppointments().get(0).getDateOfAppointment());
                if (aDate.after(startDate) && aDate.before(endDate)) {
                    saveData(ctcPatient, i, data);
                }
            }

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

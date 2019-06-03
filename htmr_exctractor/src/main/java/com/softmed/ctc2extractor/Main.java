package com.softmed.ctc2extractor;

import com.google.gson.Gson;
import com.healthmarketscience.jackcess.*;
import com.softmed.ctc2extractor.Model.CTCPatient;
import com.softmed.ctc2extractor.Model.CTCPatientsModel;
import com.softmed.ctc2extractor.Model.PatientAppointment;
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
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class Main {

    private static final String TAG_CTC2_FILE_LOCAITON = "CTC2FileLocation";
    private static String configurationFile = "helper.properties";
    private static String CTC2DatabaseLocation;
    private static Database db;
    private static JTextArea log;
    private static String regcode = "", discode = "", facility = "", healthcentre = "", centrecode = "", hfrCode = "";
    private static Date todaysDate;

    public static void main(String[] s) {
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
                log.append("\n\nError Encountered : " + e1.getMessage());
            }
        }
        Calendar c = Calendar.getInstance();


        todaysDate = c.getTime();


        final JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));

        final JFrame frame = new JFrame("CTC Extractor tool");

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JMenuBar menuBar = new JMenuBar();
        JMenu menu;
        JMenuItem menuItem;

        menu = new JMenu("Settings");
        menu.setMnemonic(KeyEvent.VK_A);
        menu.getAccessibleContext().setAccessibleDescription("The only menu in this program that has menu items");
        menuBar.add(menu);

        JLabel label = new JLabel(" ");
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel label1 = new JLabel("  Welcome to the simple CTC data extractor tool!!");
        label1.setAlignmentX(Component.LEFT_ALIGNMENT);

        try {
            CTC2DatabaseLocation = configuration.getString(TAG_CTC2_FILE_LOCAITON);
            getFacilityConfig(CTC2DatabaseLocation);
        } catch (Exception e) {
            e.printStackTrace();
        }

        final JLabel label2 = new JLabel("  Location of the CTC2 files:");
        label2.setAlignmentX(Component.LEFT_ALIGNMENT);

        if (CTC2DatabaseLocation == null || CTC2DatabaseLocation.equals("")) {
            CTC2DatabaseLocation = "Please select the CTC Database location in settings";
        }
        final JLabel label3 = new JLabel("  " + CTC2DatabaseLocation);
        label3.setAlignmentX(Component.LEFT_ALIGNMENT);


        final JLabel label4 = new JLabel();
        if (hfrCode.equals("")) {
            label4.setText(" ");
        } else {
            label4.setText("  HFR Code =  " + hfrCode);
        }

        label4.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton button = new JButton();
        button.setText("Start Data Synchronization");

        button.setAlignmentX(Component.LEFT_ALIGNMENT);

        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Runnable r = new Runnable() {
                    public void run() {
                        if (CTC2DatabaseLocation != null && !hfrCode.equals(""))
                            ObtainDataFromCTC2(CTC2DatabaseLocation);
                        else {
                            log.append("\n\n Please select the correct CTC2 Database location");
                        }
                    }
                };
                new Thread(r).start();
            }
        });


        log = new JTextArea(10, 10);
        log.setEditable(false);
        log.setMargin(new Insets(10, 10, 10, 10));

        DefaultCaret caret = (DefaultCaret) log.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);


        final JScrollPane scroll = new JScrollPane(log);
        java.awt.Dimension size = new java.awt.Dimension(400, 150);
        scroll.setMaximumSize(size);
        scroll.setMinimumSize(size);
        scroll.setPreferredSize(size);
        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);


        panel.add(label1);
        panel.add(label);
        panel.add(label2);
        panel.add(label3);
        panel.add(label4);
        panel.add(button);
        panel.add(scroll);

        frame.setJMenuBar(menuBar);
        frame.add(panel);
        frame.setSize(410, 310);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        menuItem = new JMenuItem(new AbstractAction("Set CTC Database Location") {
            public void actionPerformed(ActionEvent e) {
                // Button pressed logic goes here

                int result = fileChooser.showOpenDialog(frame);
                if (result == JFileChooser.APPROVE_OPTION) {
                    // user selects a file

                    File selectedFile = fileChooser.getSelectedFile();
                    System.out.println("Selected file: " + selectedFile.getAbsolutePath());

                    try {
                        createDefault(configurationFile, selectedFile.getAbsolutePath());
                    } catch (Exception e1) {
                        e1.printStackTrace();

                        log.append("\n\nError Encountered : " + e1.getMessage());
                    }
                    loadFirst(TAG_CTC2_FILE_LOCAITON, configurationFile);
                    CTC2DatabaseLocation = selectedFile.getAbsolutePath();
                    getFacilityConfig(CTC2DatabaseLocation);


                    label3.setText("  " + CTC2DatabaseLocation);
                    label4.setText("  HFR Code =  " + hfrCode);
                }
            }
        });

        menuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_1, ActionEvent.ALT_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription(
                "This doesn't really do anything");
        menu.add(menuItem);


    }

    public static void createDefault(String fileName, String sourceFile) throws Exception {
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


    static Configuration loadFirst(String prefix, String fileNames) {
        try {
            Configuration cf = new PropertiesConfiguration(fileNames)
                    .interpolatedConfiguration();
            System.out.println("loaded properties from " + fileNames);
            return cf;
        } catch (ConfigurationException e) {
            e.printStackTrace();
            log.append("\n\nError Encountered : " + e.getMessage());
        }
        System.out.println("Cannot locate configuration: tried," + fileNames);
        // default to an empty configuration
        return null;
    }

    public static void getFacilityConfig(String fileLocation) {
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
            log.append("\n\nError Encountered : " + e.getMessage());
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

    public static void ObtainDataFromCTC2(String fileLocation) {
        int numberOfPatientsWithMissedAppointments = 0;
        try {
            System.out.println("CTC DATABASE Location = " + fileLocation);
            db = DatabaseBuilder.open(new File(fileLocation));
        } catch (IOException e) {
            e.printStackTrace();
            log.append("\n\nError Encountered : " + e.getMessage());
        }

        CTCPatientsModel ctcPatientsModel = new CTCPatientsModel();

        centrecode = regcode + "-" + discode + "-" + facility + "." + healthcentre;
        System.out.println("centrecode =  " + centrecode);
        log.setText("Clinic Centre CTC2 Code : " + centrecode);
        log.append("\nDate : " + Calendar.getInstance().getTime().toString());
        ctcPatientsModel.setFacilityCTC2Code(centrecode);
        ctcPatientsModel.setHfrCode(hfrCode);


        log.append("\n\n\nObtaining patient appointments from CTC2 database");


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
        java.util.List<CTCPatient> ctcPatients = new ArrayList<CTCPatient>();
        int count = 0;
        System.out.println("Patients Information");
        for (Row patient : tblPatients) {
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


            List<PatientAppointment> appointments = new ArrayList<PatientAppointment>();
            int missedAppointmentCount = 0;

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

            //Calculating the date of the last 3 month from now
            Date _28DaysAgo = new Date();
            Calendar c1 = Calendar.getInstance();

            c1.add(Calendar.DATE, -28);
            _28DaysAgo = c1.getTime();


            try {
                //Obtaining all LTF appointments in the last 28 days
                if (appointment.getDate("DateOfAppointment").before(_28DaysAgo) &&
                        appointment.getInt("Cancelled") == 0) {
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
                            c.setTime(appointment.getDate("DateOfAppointment"));
                            c.add(Calendar.DATE,-1);
                            Date appDate = c.getTime();

                            if ((visitDate.after(_28DaysAgo) || visitDate.after(appDate)) &&
                                    visitDate.before(todaysDate) &&
                                    visit.getString("PatientID").equals(appointment.getString("PatientID"))) {
                                hasVisited = true;
                                break;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if(!hasVisited) {
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

                        appointments.add(missedAppointment);
                        missedAppointmentCount++;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (missedAppointmentCount > 0) {
                numberOfPatientsWithMissedAppointments++;
            }

            if (missedAppointmentCount > 0) {
                String patientStatus = "";
                for(Row statusRow:statusCursor.newEntryIterable(patient.getString("PatientID"))){
                    patientStatus=statusRow.getString("Status");
                    System.out.println("Status : "+patientStatus+" Date : "+statusRow.getDate("StatusDate").toString());
                }

                if (!patientStatus.toLowerCase().contains("transferred") && !patientStatus.toLowerCase().contains("died") && !patientStatus.toLowerCase().contains("opted")) {
                    ctcPatient.setPatientAppointments(appointments);
                    ctcPatients.add(ctcPatient);
                    count++;

                    System.out.println("*****************************************************************************");
                    System.out.println("PatientID = " + patient.getString("PatientID"));
                    System.out.println("*****************************************************************************");

                    log.append("\nObtained LTF Patient = : " + patient.getString("PatientID")+"  Status : "+patientStatus);
                    System.out.println();
                }


            }
        }

        ctcPatientsModel.setCtcPatientsDTOS(ctcPatients);
        Gson gson = new Gson();
        String json = gson.toJson(ctcPatientsModel);

        HttpClient httpClient = new DefaultHttpClient();

        System.out.println("Patients data = " + json);
        System.out.println("Patients found = " + count);

        log.append("\n\nPatients with LTFs found = : " + count);

        generateExcel(ctcPatients);
        System.out.println("Sending data to server");
        log.append("\n\nSending data to server");

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

            log.append("\nData sent successfully");
        } catch (Exception ex) {
            ex.printStackTrace();
            log.append("\nError sending data");
        } finally {
            httpClient.getConnectionManager().shutdown();
        }

    }

    public static void generateExcel(List<CTCPatient> ctcPatients){
        //Blank workbook
        XSSFWorkbook workbook = new XSSFWorkbook();

        //Create a blank sheet
        XSSFSheet sheet = workbook.createSheet("Employee Data");

        //This data needs to be written (Object[])
        Map<String, Object[]> data = new TreeMap<String, Object[]>();
        data.put("1", new Object[] {"SN","CTC-NUMBER", "NAME", "GENDER","PHONE NUMBER","VILLAGE","WARD","CARE TAKER NAME","CARE TAKER PHONE NUMBER"});

        int i=1;
        for(CTCPatient ctcPatient:ctcPatients){
            data.put(i+"", new Object[] {i+""
                    ,ctcPatient.getCtcNumber()
                    ,ctcPatient.getFirstName()+" "+ctcPatient.getMiddleName()+" "+ctcPatient.getSurname()
                    ,ctcPatient.getGender()
                    ,ctcPatient.getPhoneNumber()
                    ,ctcPatient.getVillage()
                    ,ctcPatient.getWard()
                    ,ctcPatient.getCareTakerName()
                    ,ctcPatient.getCareTakerPhoneNumber()
            });
        }

        //Iterate over data and write to sheet
        Set<String> keyset = data.keySet();
        int rownum = 0;
        for (String key : keyset)
        {
            XSSFRow row = sheet.createRow(rownum++);
            Object [] objArr = data.get(key);
            int cellnum = 0;
            for (Object obj : objArr)
            {
                Cell cell = row.createCell(cellnum++);
                if(obj instanceof String)
                    cell.setCellValue((String)obj);
                else if(obj instanceof Integer)
                    cell.setCellValue((Integer)obj);
            }
        }
        try
        {
            //Write the workbook in file system
            FileOutputStream out = new FileOutputStream(new File("howtodoinjava_demo.xlsx"));
            workbook.write(out);
            out.close();
            System.out.println("howtodoinjava_demo.xlsx written successfully on disk.");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}

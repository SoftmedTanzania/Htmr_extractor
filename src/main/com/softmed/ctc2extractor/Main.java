package main.com.softmed.ctc2extractor;

import com.google.gson.Gson;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
import main.com.softmed.ctc2extractor.Model.CTCPatient;
import main.com.softmed.ctc2extractor.Model.CTCPatientsModel;
import main.com.softmed.ctc2extractor.Model.PatientAppointment;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.codec.binary.Base64;

public class Main {

    private static final String TAG_CTC2_FILE_LOCAITON = "CTC2FileLocation";
    private static String configurationFile = "helper.properties";
    private static String CTC2DatabaseLocation;
    private static Database db;
    private static JTextArea log;
    private static String regcode = "",discode = "",facility = "",healthcentre = "",centrecode = "", hfrCode = "";

    public static void main(String s[]) {
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
                log.append("\n\nError Encountered : "+e1.getMessage());
            }
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));

        JFrame frame = new JFrame("CTC Extractor tool");

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

        JLabel label2 = new JLabel("  Location of the CTC2 files:");
        label2.setAlignmentX(Component.LEFT_ALIGNMENT);

        if(CTC2DatabaseLocation==null || CTC2DatabaseLocation.equals("")){
            CTC2DatabaseLocation = "Please select the CTC Database location in settings";
        }
        JLabel label3 = new JLabel("  "+CTC2DatabaseLocation);
        label3.setAlignmentX(Component.LEFT_ALIGNMENT);


        JLabel label4 = new JLabel();
        if(hfrCode.equals("")){
            label4.setText(" ");
        }else{
            label4.setText("  HFR Code =  "+hfrCode);
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
                        else{
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

        DefaultCaret caret = (DefaultCaret)log.getCaret();
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

                        log.append("\n\nError Encountered : "+e1.getMessage());
                    }
                    loadFirst(TAG_CTC2_FILE_LOCAITON, configurationFile);
                    CTC2DatabaseLocation = selectedFile.getAbsolutePath();
                    getFacilityConfig(CTC2DatabaseLocation);


                    label3.setText("  " + CTC2DatabaseLocation);
                    label4.setText("  HFR Code =  "+hfrCode);
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
            log.append("\n\nError Encountered : "+e.getMessage());
        }
        System.out.println("Cannot locate configuration: tried," + fileNames);
        // default to an empty configuration
        return null;
    }

    public static void getFacilityConfig(String fileLocation){
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
            log.append("\n\nError Encountered : "+e.getMessage());
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

    public  static void ObtainDataFromCTC2(String fileLocation) {
        int numberOfPatientsWithUpcomingAppointments = 0;
        int numberOfPatientsWithMissedAppointments = 0;
        try {
            System.out.println("CTC DATABASE Location = " + fileLocation);
            db = DatabaseBuilder.open(new File(fileLocation));
        } catch (IOException e) {
            e.printStackTrace();
            log.append("\n\nError Encountered : "+e.getMessage());
        }

        CTCPatientsModel ctcPatientsModel = new CTCPatientsModel();

        centrecode = regcode + "-" + discode + "-" + facility + "." + healthcentre;
        System.out.println("centrecode =  " + centrecode);
        log.setText("Clinic Centre CTC2 Code : " + centrecode);
        log.append("\nDate : " + Calendar.getInstance().getTime().toString());
        ctcPatientsModel.setFacilityCTC2Code(centrecode);
        ctcPatientsModel.setHfrCode(hfrCode);


        log.append("\n\n\nObtaining patient appointments from CTC2 database");


        Calendar calendar = Calendar.getInstance();

        Table tblAppointments = null;
        try {
            tblAppointments = db.getTable("tblAppointments");
        } catch (IOException e) {
            e.printStackTrace();
        }

        Table tblPatients = null;
        try {
            tblPatients = db.getTable("tblPatients");
        } catch (IOException e) {
            e.printStackTrace();
        }

        java.util.List<CTCPatient> ctcPatients = new ArrayList<>();
        int count = 0;
        System.out.println("Patients Information");
        for (Row row : tblPatients) {
            CTCPatient ctcPatient = new CTCPatient();

            ctcPatient.setHealthFacilityCode(centrecode);

            try {
                ctcPatient.setFirstName(row.getString("FirstName").split(" ")[0]);
                ctcPatient.setMiddleName(row.getString("FirstName").split(" ")[1]);
                System.out.println("Middle Name : " + ctcPatient.getMiddleName());
            } catch (Exception e) {
                ctcPatient.setMiddleName("");
                e.printStackTrace();
            }

            ctcPatient.setSurname(row.getString("SurName"));
            ctcPatient.setCtcNumber(row.getString("PatientID"));

            ctcPatient.setPhoneNumber(row.getString("Contact"));
            ctcPatient.setVillage(row.getString("VillageMtaa"));
            ctcPatient.setWard(row.getString("WardName"));
            ctcPatient.setCareTakerName(row.getString("Helper"));
            ctcPatient.setCareTakerPhoneNumber(row.getString("HelperContact"));

            ctcPatient.setDateOfBirth(row.getDate("DateOfBirth").getTime());
            ctcPatient.setGender(row.getString("Sex"));
            try {
                ctcPatient.setDateOfDeath(row.getDate("DateOfDeath").getTime());
                continue;
            } catch (Exception e) {
            }


            ctcPatient.setHivStatus(true);



            List<PatientAppointment> appointments = new ArrayList<>();
            int missedAppointmentCount=0,upcomingAppointmentCount=0;
            for (Row row1 : tblAppointments) {

                //Calculating the date of 1 month from now
                Date aMonthFromNow = new Date();
                Calendar c = Calendar.getInstance();
                c.add(Calendar.MONTH, 1);
                aMonthFromNow = c.getTime();

                try {
                    //Obtaining all patient upcoming appointments in the upcoming month
                    if (row1.getDate("DateOfAppointment").after(calendar.getTime()) &&
                            row1.getDate("DateOfAppointment").before(aMonthFromNow) &&
                            row1.getInt("Cancelled") == 0 &&
                            row1.getString("PatientID").equals(row.getString("PatientID"))) {
                        PatientAppointment appointment = new PatientAppointment();
                        appointment.setDateOfAppointment(row1.getDate("DateOfAppointment").getTime());
                        appointment.setStatus(0);
                        appointments.add(appointment);
                        upcomingAppointmentCount++;
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }


                //Calculating the date of the last 3 month from now
                Date threeMonthsAgo = new Date();
                Calendar c1 = Calendar.getInstance();
                c1.add(Calendar.MONTH, -3);
                threeMonthsAgo = c1.getTime();

                try {
                    //Obtaining all missed patient appointments in the last 3 month
                    if (row1.getDate("DateOfAppointment").after(threeMonthsAgo) &&
                            row1.getDate("DateOfAppointment").before(calendar.getTime()) &&
                            row1.getDate("DateOfAppointmentGiven") == null &&
                            row1.getInt("Cancelled") == 0 &&
                            row1.getString("PatientID").equals(row.getString("PatientID"))) {
                        PatientAppointment missedAppointment = new PatientAppointment();
                        missedAppointment.setDateOfAppointment(row1.getDate("DateOfAppointment").getTime());
                        missedAppointment.setStatus(-1);
                        appointments.add(missedAppointment);
                        missedAppointmentCount++;
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

            }

            if (upcomingAppointmentCount > 0) {
                numberOfPatientsWithUpcomingAppointments++;
            }

            if (missedAppointmentCount > 0) {
                numberOfPatientsWithMissedAppointments++;
            }

            if(upcomingAppointmentCount > 0 || missedAppointmentCount >0) {
                ctcPatient.setPatientAppointments(appointments);
                ctcPatients.add(ctcPatient);
                count++;

                log.append("\nObtained Patient = : " + row.getString("PatientID"));
                System.out.println("*****************************************************************************");
                System.out.println("PatientID = " + row.getString("PatientID"));
                System.out.println("*****************************************************************************");
                System.out.println("");
            }
        }

        ctcPatientsModel.setCtcPatientsDTOS(ctcPatients);
        Gson gson = new Gson();
        String json = gson.toJson(ctcPatientsModel);

        HttpClient httpClient = new DefaultHttpClient();

        System.out.println("Patients data = " + json);
        System.out.println("Patients found = " + count);

        log.append("\n\nPatients found = : " + count);
        log.append("\nPatients with upcoming appointments = : " + numberOfPatientsWithUpcomingAppointments);
        log.append("\nPatients with missed appointments = : " + numberOfPatientsWithMissedAppointments);
        System.out.println("Sending data to server");
        log.append("\n\nSending data to server");

        String username = "admin";
        String password = "Admin123";

        byte[] encodedPassword = (username + ":" + password).getBytes();

        try {
            HttpPost request = new HttpPost("http://45.56.90.103:8080/opensrp/save-ctc-patients");
            StringEntity params = new StringEntity(json);
            request.addHeader("content-type", "application/json");
            request.addHeader("Accept", "application/json");
            request.addHeader("Authorization", "Basic " + Base64.encodeBase64String(encodedPassword));

            request.setEntity(params);
			HttpResponse response = httpClient.execute(request);

             //handle response here...
			System.out.println("Server response : "+response.getStatusLine());

            log.append("\nData sent successfully");
        } catch (Exception ex) {
            ex.printStackTrace();
            log.append("\nError sending data");
        } finally {
            httpClient.getConnectionManager().shutdown();
        }


    }
}

package com.softmed.ctc2extractor.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.softmed.ctc2extractor.Model.Address;
import com.softmed.ctc2extractor.Model.CTCPatient;
import com.softmed.ctc2extractor.Model.CTCPatientsModel;
import com.softmed.ctc2extractor.Model.Client;
import com.softmed.ctc2extractor.Model.ClientEvents;
import com.softmed.ctc2extractor.Model.Event;
import com.softmed.ctc2extractor.Model.Obs;
import com.softmed.ctc2extractor.Model.PatientAppointment;
import com.softmed.ctc2extractor.util.DateTimeTypeConverter;
import org.joda.time.DateTime;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class OpensrpService {

    //These are testing server chw user credentials.
    //these are temporarily hardcoded here for now but later will be refactored to be obtained from the opensrp server during authentication
    private static final String locationID = "7504f24d-6b6f-4a7c-a8a2-60ab491678a6";
    private static final String providerId = "johnjamesdoe";
    private static final String teamId = "7d69862f-dde4-4ca0-bc24-27aff12e253a";
    private static final String team = "Masana Teams";


    private static final int clientDatabaseVersion = 17;
    private static final int clientApplicationVersion = 2;

    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    private static Obs getStartOb() {
        return new Obs("concept", "start", "163137AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "", Arrays.asList(new Object[]{new Date()}), null, null, "start");
    }

    private static Obs getEndOb() {
        return new Obs("concept", "end", "163138AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "", Arrays.asList(new Object[]{new Date()}), null, null, "end");
    }


    public static Client getClientEvent(CTCPatient patient) {
        Client familyClient = new Client(UUID.randomUUID().toString());
        familyClient.setFirstName(patient.getSurname());
        familyClient.setLastName("Family");
        familyClient.setBirthdate(new Date(0));
        familyClient.setBirthdateApprox(false);
        familyClient.setDeathdateApprox(false);
        familyClient.setGender(patient.getGender());
        familyClient.setClientApplicationVersion(clientApplicationVersion);
        familyClient.setClientDatabaseVersion(clientDatabaseVersion);
        familyClient.setType("Client");
        familyClient.setId(UUID.randomUUID().toString());
        familyClient.setDateCreated(new Date());
        familyClient.setAttributes(new HashMap<>());
        setAddress(familyClient, patient.getVillage());


        return familyClient;
    }

    public static void setAddress(Client client, String village) {
        List<Address> addresses = new ArrayList<>();
        Address villageAddress = new Address();
        villageAddress.setAddressType("village");
        villageAddress.setCityVillage(village);
        villageAddress.setAddressFields(new HashMap<>());

        addresses.add(villageAddress);

        client.setAddresses(addresses);
    }

    public static Client getFamilyHeadClientEvent(CTCPatient patient) {
        Client ctcClient = new Client(UUID.randomUUID().toString());
        try {
            ctcClient.setFirstName(patient.getFirstName());
            ctcClient.setMiddleName(patient.getMiddleName());
        } catch (Exception e) {
            ctcClient.setMiddleName("");
            e.printStackTrace();
        }

        ctcClient.setLastName(patient.getSurname());
        ctcClient.setGender(patient.getGender());
        ctcClient.setBirthdate(new Date(patient.getDateOfBirth()));
        ctcClient.setBirthdateApprox(false);
        ctcClient.setType("Client");
        ctcClient.setDeathdateApprox(false);
        ctcClient.setClientApplicationVersion(clientApplicationVersion);
        ctcClient.setClientDatabaseVersion(clientDatabaseVersion);

        Map<String, Object> attributes = new HashMap<>();
        List<String> id_available = new ArrayList<>();
        id_available.add("chk_none");
        attributes.put("id_avail", new Gson().toJson(id_available));
        attributes.put("Community_Leader", new Gson().toJson(id_available));
        attributes.put("Health_Insurance_Type", "None");

        ctcClient.setAttributes(attributes);
        setAddress(ctcClient, patient.getVillage());

        return ctcClient;
    }

    public static Event getFamilyRegistrationEvent(Client client, CTCPatient patient) {
        Event familyRegistrationEvent = new Event();
        familyRegistrationEvent.setBaseEntityId(client.getBaseEntityId());
        familyRegistrationEvent.setEventType("Family Registration");
        familyRegistrationEvent.setEntityType("ec_family");
        setMetaData(familyRegistrationEvent);
        familyRegistrationEvent.addObs(new Obs("formsubmissionField", "text", "last_interacted_with", "", Arrays.asList(new Object[]{String.valueOf(Calendar.getInstance().getTimeInMillis())}), null, null, "last_interacted_with"));
        familyRegistrationEvent.addObs(new Obs("concept", "phonenumber", "163152AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "", Arrays.asList(new Object[]{patient.getPhoneNumber()}), null, null, "phonenumber"));
        return familyRegistrationEvent;
    }


    public static Event getFamilyMemberRegistrationEvent(Client client, CTCPatient patient) {
        Event familyMemberRegistrationEvent = new Event();
        familyMemberRegistrationEvent.setBaseEntityId(client.getBaseEntityId());
        familyMemberRegistrationEvent.setEventType("Family Member Registration");
        familyMemberRegistrationEvent.setEntityType("ec_family_member");
        familyMemberRegistrationEvent.addObs(new Obs("concept", "phonenumber", "163152AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "", Arrays.asList(new Object[]{patient.getPhoneNumber()}), null, null, "phonenumber"));
        familyMemberRegistrationEvent.addObs(new Obs("formsubmissionField", "text", "1542AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "1542AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", Arrays.asList(new Object[]{"164369AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"}), Arrays.asList(new Object[]{"None"}), null, "service_provider"));
        familyMemberRegistrationEvent.addObs(new Obs("formsubmissionField", "text", "id_avail", "", Arrays.asList(new Object[]{"None"}), null, null, "id_avail"));
        familyMemberRegistrationEvent.addObs(new Obs("formsubmissionField", "text", "leader", "", Arrays.asList(new Object[]{"None"}), null, null, "leader"));
        familyMemberRegistrationEvent.addObs(new Obs("formsubmissionField", "text", "last_interacted_with", "", Arrays.asList(new Object[]{String.valueOf(Calendar.getInstance().getTimeInMillis())}), null, null, "last_interacted_with"));
        familyMemberRegistrationEvent.addObs(new Obs("concept", "text", "", "", Arrays.asList(new Object[]{client.getLastName()}), null, null, "surname"));
        setMetaData(familyMemberRegistrationEvent);
        return familyMemberRegistrationEvent;
    }

    public static Event getHIVFollowupEvent(Client client, CTCPatient patient, PatientAppointment patientAppointment) {
        Event hivFollowupEvent = new Event();
        hivFollowupEvent.setBaseEntityId(client.getBaseEntityId());
        hivFollowupEvent.setEventType("HIV Community Followup");
        hivFollowupEvent.setEntityType("ec_hiv_community_followup");
        hivFollowupEvent.addObs(new Obs("concept", "text", "ctc_number", "", Arrays.asList(new Object[]{patient.getCtcNumber()}), null, null, "ctc_number"));
        hivFollowupEvent.addObs(new Obs("concept", "text", "last_client_visit_date", "", Arrays.asList(new Object[]{patientAppointment.getDateOfAppointment()}), null, null, "last_client_visit_date"));
        hivFollowupEvent.addObs(new Obs("concept", "text", "reasons_for_issuing_community_referral", "", Arrays.asList(new Object[]{patientAppointment.getStatus() == 2 ? "lost_to_followup" : "missed_appointment"}), Arrays.asList(new Object[]{patientAppointment.getStatus() == 2 ? "Lost to followup client" : "Missed Appointment"}), null, "clinic_appointment_type"));
        hivFollowupEvent.addObs(new Obs("concept", "text", "hiv_community_referral_date", "", Arrays.asList(new Object[]{Calendar.getInstance().getTimeInMillis()}), null, null, "hiv_community_referral_date"));
        setMetaData(hivFollowupEvent);
        return hivFollowupEvent;
    }


    private static void setMetaData(Event event) {
        event.setLocationId(locationID);
        event.setProviderId(providerId); //TODO extract this
        event.setTeamId(teamId); //TODO extract this
        event.setTeam(team); //TODO extract this
        event.setType("Event");
        event.setFormSubmissionId(UUID.randomUUID().toString());
        event.setEventDate(new Date());
        event.setDateCreated(new Date());
        event.addObs(OpensrpService.getStartOb());
        event.addObs(OpensrpService.getEndOb());
        event.setClientApplicationVersion(clientApplicationVersion);
        event.setClientDatabaseVersion(clientDatabaseVersion);
        event.setDuration(0);
        event.setIdentifiers(new HashMap<>());
    }


    public static String generateClientEvent(CTCPatientsModel ctcPatientsModel) {

        List<Client> clients = new ArrayList<>();
        List<Event> events = new ArrayList<>();

        int i = 0;
        for (CTCPatient patient : ctcPatientsModel.getCtcPatientsDTOS()) {
            if (i == 5)
                break;

            i++;
            Client familyClient = getClientEvent(patient);
            Client ctcClient = getFamilyHeadClientEvent(patient);

            Map<String, List<String>> familyRelationships = new HashMap<>();
            familyRelationships.put("family_head", Collections.singletonList(ctcClient.getBaseEntityId()));
            familyRelationships.put("primary_caregiver", Collections.singletonList(ctcClient.getBaseEntityId()));
            familyClient.setRelationships(familyRelationships);


            Map<String, List<String>> ctcClientRelations = new HashMap<>();
            ctcClientRelations.put("family", Collections.singletonList(familyClient.getBaseEntityId()));
            ctcClient.setRelationships(ctcClientRelations);


            //Generate family registration event
            Event familyRegistrationEvent = getFamilyRegistrationEvent(familyClient, patient);

            //Generate family Member registration event
            Event familyMemberRegistrationEvent = getFamilyMemberRegistrationEvent(ctcClient, patient);

            //Generate HIV community followup event
            Event hivCommunityFollowupEvent = getHIVFollowupEvent(ctcClient, patient, patient.getPatientAppointments().get(patient.getPatientAppointments().size() - 1));


            clients.add(familyClient);
            clients.add(ctcClient);
            events.add(familyRegistrationEvent);
            events.add(familyMemberRegistrationEvent);
            events.add(hivCommunityFollowupEvent);
        }

        ClientEvents clientEvents = new ClientEvents();
        clientEvents.setClients(clients);
        clientEvents.setEvents(events);
        clientEvents.setNo_of_events(events.size());

        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
                .registerTypeAdapter(DateTime.class, new DateTimeTypeConverter()).create();


        return gson.toJson(clientEvents);

    }
}

package com.softmed.ctc2extractor.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.softmed.ctc2extractor.model.Address;
import com.softmed.ctc2extractor.model.CTCPatient;
import com.softmed.ctc2extractor.model.CTCPatientsModel;
import com.softmed.ctc2extractor.model.Client;
import com.softmed.ctc2extractor.model.ClientEvents;
import com.softmed.ctc2extractor.model.Event;
import com.softmed.ctc2extractor.model.Obs;
import com.softmed.ctc2extractor.model.PatientAppointment;
import com.softmed.ctc2extractor.util.DateTimeTypeConverter;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.softmed.ctc2extractor.Constants.locationID;
import static com.softmed.ctc2extractor.Constants.providerId;
import static com.softmed.ctc2extractor.Constants.team;
import static com.softmed.ctc2extractor.Constants.teamId;

public class OpenSrpService {

    private static final int clientDatabaseVersion = 17;
    private static final int clientApplicationVersion = 2;

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
        setAddress(familyClient, patient.getVillage(), patient.getWard());


        return familyClient;
    }

    public static void setAddress(Client client, String village, String ward) {
        List<Address> addresses = new ArrayList<>();

        if (village != null && !village.isEmpty()) {
            Address villageAddress = new Address();
            villageAddress.setAddressType("village");
            villageAddress.setCityVillage(village);
            villageAddress.setAddressFields(new HashMap<>());
            addresses.add(villageAddress);
        }

        if (ward != null && !ward.isEmpty()) {
            Address wardAddress = new Address();
            wardAddress.setAddressType("ward");
            wardAddress.setCityVillage(ward);
            wardAddress.setAddressFields(new HashMap<>());
            addresses.add(wardAddress);
        }

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
        setAddress(ctcClient, patient.getVillage(), patient.getWard());
        return ctcClient;
    }

    public static Event getFamilyRegistrationEvent(Client client, CTCPatient patient) {
        Event familyRegistrationEvent = new Event();
        familyRegistrationEvent.setBaseEntityId(client.getBaseEntityId());
        familyRegistrationEvent.setEventType("Family Registration");
        familyRegistrationEvent.setEntityType("ec_independent_client");
        setMetaData(familyRegistrationEvent);
        familyRegistrationEvent.addObs(new Obs("formsubmissionField", "text", "last_interacted_with", "", Arrays.asList(new Object[]{String.valueOf(Calendar.getInstance().getTimeInMillis())}), null, null, "last_interacted_with"));
        familyRegistrationEvent.addObs(new Obs("concept", "phonenumber", "163152AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "", Arrays.asList(new Object[]{patient.getPhoneNumber()}), null, null, "phonenumber"));
        return familyRegistrationEvent;
    }


    public static Event getFamilyMemberRegistrationEvent(Client client, CTCPatient patient) {
        Event familyMemberRegistrationEvent = new Event();
        familyMemberRegistrationEvent.setBaseEntityId(client.getBaseEntityId());
        familyMemberRegistrationEvent.setEventType("Family Member Registration");
        familyMemberRegistrationEvent.setEntityType("ec_independent_client");
        familyMemberRegistrationEvent.addObs(new Obs("concept", "phonenumber", "163152AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "", Arrays.asList(new Object[]{patient.getPhoneNumber()}), null, null, "phonenumber"));
        familyMemberRegistrationEvent.addObs(new Obs("formsubmissionField", "text", "1542AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "1542AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", Arrays.asList(new Object[]{"164369AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"}), Arrays.asList(new Object[]{"None"}), null, "service_provider"));
        familyMemberRegistrationEvent.addObs(new Obs("formsubmissionField", "text", "id_avail", "", Arrays.asList(new Object[]{"None"}), null, null, "id_avail"));
        familyMemberRegistrationEvent.addObs(new Obs("formsubmissionField", "text", "leader", "", Arrays.asList(new Object[]{"None"}), null, null, "leader"));
        familyMemberRegistrationEvent.addObs(new Obs("formsubmissionField", "text", "last_interacted_with", "", Arrays.asList(new Object[]{String.valueOf(Calendar.getInstance().getTimeInMillis())}), null, null, "last_interacted_with"));
        familyMemberRegistrationEvent.addObs(new Obs("concept", "text", "", "", Arrays.asList(new Object[]{client.getLastName()}), null, null, "surname"));

        if (patient.getCareTakerName() != null && !patient.getCareTakerName().isEmpty()) {
            familyMemberRegistrationEvent.addObs(new Obs("concept", "text", "Has_Primary_Caregiver", "", Arrays.asList(new Object[]{"Yes"}), Arrays.asList(new Object[]{"Yes"}), null, "has_primary_caregiver"));
            familyMemberRegistrationEvent.addObs(new Obs("concept", "text", "Primary_Caregiver_Name", "", Arrays.asList(new Object[]{patient.getCareTakerName()}), null, null, "primary_caregiver_name"));
            familyMemberRegistrationEvent.addObs(new Obs("concept", "text", "5622AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "159635AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", Arrays.asList(new Object[]{patient.getCareTakerPhoneNumber()}), null, null, "other_phone_number"));
        }


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
        event.setProviderId(providerId);
        event.setTeamId(teamId);
        event.setTeam(team);
        event.setType("Event");
        event.setFormSubmissionId(UUID.randomUUID().toString());
        event.setEventDate(new Date());
        event.setDateCreated(new Date());
        event.addObs(OpenSrpService.getStartOb());
        event.addObs(OpenSrpService.getEndOb());
        event.setClientApplicationVersion(clientApplicationVersion);
        event.setClientDatabaseVersion(clientDatabaseVersion);
        event.setDuration(0);
        event.setIdentifiers(new HashMap<>());
    }


    public static String generateClientEvent(CTCPatientsModel ctcPatientsModel) {

        List<Client> clients = new ArrayList<>();
        List<Event> events = new ArrayList<>();

        for (CTCPatient patient : ctcPatientsModel.getCtcPatientsDTOS()) {
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
        clientEvents.setNoOfEvents(events.size());

        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
                .registerTypeAdapter(DateTime.class, new DateTimeTypeConverter()).create();


        return gson.toJson(clientEvents);

    }
}

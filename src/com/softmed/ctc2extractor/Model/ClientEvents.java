package com.softmed.ctc2extractor.Model;

import java.util.List;

public class ClientEvents {
    List<Client> clients;
    List<Event> events;
    int no_of_events;

    public List<Client> getClients() {
        return clients;
    }

    public void setClients(List<Client> clients) {
        this.clients = clients;
    }

    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }

    public int getNo_of_events() {
        return no_of_events;
    }

    public void setNo_of_events(int no_of_events) {
        this.no_of_events = no_of_events;
    }
}


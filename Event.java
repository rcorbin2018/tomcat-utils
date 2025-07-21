package com.cigna.emu.model;

import java.util.Date;

public class Event {
    private String component;
    private String namespace;
    private Date timestamp; // Changed from LocalDateTime
    private String outcome;
    private String event;
    private String eventDetails;
    private String message;

    public Event(String component, String namespace, Date timestamp, String outcome, String event, String eventDetails, String message) {
        this.component = component;
        this.namespace = namespace;
        this.timestamp = timestamp;
        this.outcome = outcome;
        this.event = event;
        this.eventDetails = eventDetails;
        this.message = message;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getOutcome() {
        return outcome;
    }

    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getEventDetails() {
        return eventDetails;
    }

    public void setEventDetails(String eventDetails) {
        this.eventDetails = eventDetails;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

package com.cigna.emu.model;

import java.time.LocalDateTime;

public class Event {
    private String component;
    private String namespace;
    private LocalDateTime timestamp;
    private String outcome;
    private String event;
    private String eventDetails;
    private String message;

    // Constructor
    public Event(String component, String namespace, LocalDateTime timestamp, String outcome,
                 String event, String eventDetails, String message) {
        this.component = component;
        this.namespace = namespace;
        this.timestamp = timestamp;
        this.outcome = outcome;
        this.event = event;
        this.eventDetails = eventDetails;
        this.message = message;
    }

    // Getters and Setters
    public String getComponent() { return component; }
    public String getNamespace() { return namespace; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getOutcome() { return outcome; }
    public String getEvent() { return event; }
    public String getEventDetails() { return eventDetails; }
    public String getMessage() { return message; }
}

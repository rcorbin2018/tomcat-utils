package com.cigna.emu.servlet;

import com.cigna.emu.model.Event;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/detail")
public class EventDetailServlet extends HttpServlet {
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
    private static final DateTimeFormatter FALLBACK_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private MongoClient mongoClient;
    private MongoCollection<Document> collection;

    @Override
    public void init() throws ServletException {
        mongoClient = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase database = mongoClient.getDatabase("kevents");
        collection = database.getCollection("esnaps");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String component = req.getParameter("component");
        String namespace = req.getParameter("namespace");
        String outcome = req.getParameter("outcome");

        List<Event> events = new ArrayList<>();
        Document query = new Document();
        if (component != null && !component.isEmpty() && !"Unknown".equals(component)) {
            query.append("component", component);
        }
        if (namespace != null && !namespace.isEmpty() && !"Unknown".equals(namespace)) {
            query.append("namespace", namespace);
        }
        if (outcome != null && !outcome.isEmpty() && !"Unknown".equals(outcome)) {
            query.append("outcome", outcome);
        }
        // Filter for last 24 hours
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime yesterday = now.minusHours(24);
        query.append("timestamp",
                new Document("$gte", yesterday.format(ISO_FORMATTER))
                        .append("$lte", now.format(ISO_FORMATTER)));

        for (Document doc : collection.find(query)) {
            Event event = parseEvent(doc);
            if (event == null) {
                System.out.println("Skipping document with _id: " + doc.get("_id") + " due to parsing error");
                continue;
            }
            events.add(event);
        }

        req.setAttribute("events", events);
        req.getRequestDispatcher("/WEB-INF/views/detail.jsp").forward(req, resp);
    }

    private Event parseEvent(Document doc) {
        try {
            LocalDateTime timestamp = null;
            String timestampStr = doc.getString("timestamp") != null ? doc.getString("timestamp") : doc.getString("Timestamp");
            if (timestampStr != null) {
                try {
                    timestamp = LocalDateTime.parse(timestampStr, ISO_FORMATTER);
                } catch (DateTimeParseException e) {
                    try {
                        timestamp = LocalDateTime.parse(timestampStr, FALLBACK_FORMATTER);
                    } catch (DateTimeParseException e2) {
                        System.err.println("Failed to parse timestamp '" + timestampStr + "' for document _id: " + doc.get("_id"));
                    }
                }
            } else {
                System.err.println("Timestamp field missing or null for document _id: " + doc.get("_id"));
            }
            String component = doc.getString("component") != null ? doc.getString("component") : "Unknown";
            String namespace = doc.getString("namespace") != null ? doc.getString("namespace") : "Unknown";
            String outcome = doc.getString("outcome") != null ? doc.getString("outcome") : "Unknown";
            String event = doc.getString("event") != null ? doc.getString("event") : "Unknown";
            String eventDetails = doc.getString("eventDetails") != null ? doc.getString("eventDetails") : "Unknown";
            String message = doc.getString("message") != null ? doc.getString("message") : "Unknown";

            return new Event(component, namespace, timestamp, outcome, event, eventDetails, message);
        } catch (Exception e) {
            System.err.println("Error parsing document with _id: " + doc.get("_id") + " - " + e.getMessage());
            return null;
        }
    }

    @Override
    public void destroy() {
        mongoClient.close();
    }
}

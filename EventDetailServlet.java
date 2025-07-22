package com.cigna.emu.servlet;

import com.cigna.emu.model.Event;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

@WebServlet("/detail")
public class EventDetailServlet extends HttpServlet {
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
    private static final DateTimeFormatter FALLBACK_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private static final DateTimeFormatter SIMPLE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final DateTimeFormatter MINIMAL_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter NANO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS'Z'");
    private static final ZoneId UTC_ZONE = ZoneId.of("UTC");
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
        String id = req.getParameter("id");
        Event event = null;
        if (id != null && ObjectId.isValid(id)) {
            Document query = new Document("_id", new ObjectId(id));
            System.out.println("Executing detail query: " + query.toJson());
            Document doc = collection.find(query).first();
            if (doc != null) {
                System.out.println("Raw document: " + doc.toJson());
                event = parseEvent(doc);
                if (event == null) {
                    System.out.println("Failed to parse document with _id: " + id);
                }
            } else {
                System.out.println("No document found with _id: " + id);
            }
        } else {
            System.out.println("Invalid or missing id parameter: " + id);
        }
        req.setAttribute("event", event);
        req.getRequestDispatcher("/WEB-INF/views/detail.jsp").forward(req, resp);
    }

    private Event parseEvent(Document doc) {
        try {
            Date timestamp = null;
            String timestampStr = doc.getString("timestamp") != null ? doc.getString("timestamp") :
                                 doc.getString("timeStamp") != null ? doc.getString("timeStamp") :
                                 doc.getString("Timestamp");
            if (timestampStr != null) {
                try {
                    ZonedDateTime zdt = ZonedDateTime.parse(timestampStr, ISO_FORMATTER);
                    timestamp = Date.from(zdt.toInstant());
                } catch (DateTimeParseException e1) {
                    try {
                        LocalDateTime ldt = LocalDateTime.parse(timestampStr, FALLBACK_FORMATTER);
                        timestamp = Date.from(ldt.atZone(UTC_ZONE).toInstant());
                    } catch (DateTimeParseException e2) {
                        try {
                            LocalDateTime ldt = LocalDateTime.parse(timestampStr, SIMPLE_FORMATTER);
                            timestamp = Date.from(ldt.atZone(UTC_ZONE).toInstant());
                        } catch (DateTimeParseException e3) {
                            try {
                                LocalDateTime ldt = LocalDateTime.parse(timestampStr, MINIMAL_FORMATTER);
                                timestamp = Date.from(ldt.atZone(UTC_ZONE).toInstant());
                            } catch (DateTimeParseException e4) {
                                try {
                                    LocalDateTime ldt = LocalDateTime.parse(timestampStr, NANO_FORMATTER);
                                    timestamp = Date.from(ldt.atZone(UTC_ZONE).toInstant());
                                } catch (DateTimeParseException e5) {
                                    System.err.println("Failed to parse timestamp '" + timestampStr + "' for document _id: " + doc.get("_id") +
                                                       ", errors: ISO=" + e1.getMessage() + ", Fallback=" + e2.getMessage() +
                                                       ", Simple=" + e3.getMessage() + ", Minimal=" + e4.getMessage() +
                                                       ", Nano=" + e5.getMessage());
                                }
                            }
                        }
                    }
                }
            } else {
                System.err.println("Timestamp field (timestamp/timeStamp/Timestamp) missing or null for document _id: " + doc.get("_id"));
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

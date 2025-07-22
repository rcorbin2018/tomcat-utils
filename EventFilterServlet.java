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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@WebServlet("/filteredEvents")
public class EventFilterServlet extends HttpServlet {
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
    private static final DateTimeFormatter FALLBACK_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private static final DateTimeFormatter SIMPLE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final DateTimeFormatter MINIMAL_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter INPUT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
    private static final DateTimeFormatter MINUTE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final ZoneId EST_ZONE = ZoneId.of("America/New_York");
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
        String component = req.getParameter("component");
        String outcome = req.getParameter("outcome");
        String minute = req.getParameter("minute");
        String startDatetimeParam = req.getParameter("startDatetime");
        String endDatetimeParam = req.getParameter("endDatetime");
        String limitParam = req.getParameter("limit");
        
        System.out.println("Request parameters: component=" + component + ", outcome=" + outcome + 
                           ", minute=" + minute + ", startDatetime=" + startDatetimeParam + 
                           ", endDatetime=" + endDatetimeParam + ", limit=" + limitParam);

        LocalDateTime now = LocalDateTime.now(EST_ZONE).withSecond(0).withNano(0);
        LocalDateTime startLocal = startDatetimeParam != null ? LocalDateTime.parse(startDatetimeParam, INPUT_FORMATTER) : now.minusHours(1);
        LocalDateTime endLocal = endDatetimeParam != null ? LocalDateTime.parse(endDatetimeParam, INPUT_FORMATTER) : now;
        
        if (endLocal.isBefore(startLocal) || endLocal.equals(startLocal)) {
            endLocal = startLocal.plusHours(1);
        }
        
        // Convert EST to UTC
        ZonedDateTime startZoned = startLocal.atZone(EST_ZONE).withZoneSameInstant(UTC_ZONE);
        ZonedDateTime endZoned = endLocal.atZone(EST_ZONE).withZoneSameInstant(UTC_ZONE);
        LocalDateTime startUtc = startZoned.toLocalDateTime();
        LocalDateTime endUtc = endZoned.toLocalDateTime();

        int limit = limitParam != null ? Integer.parseInt(limitParam) : 5000;
        if (limit <= 0) limit = 5000;

        List<Event> events = new ArrayList<>();
        Document query = new Document();

        // Apply filters based on parameters
        if (minute != null && !minute.isEmpty()) {
            try {
                // Parse minute as EST (yyyy-MM-dd HH:mm), convert to UTC
                LocalDateTime minuteLocal = LocalDateTime.parse(minute + ":00", MINUTE_FORMATTER);
                ZonedDateTime minuteZonedEst = minuteLocal.atZone(EST_ZONE);
                ZonedDateTime minuteZonedUtc = minuteZonedEst.withZoneSameInstant(UTC_ZONE);
                LocalDateTime startMinuteUtc = minuteZonedUtc.toLocalDateTime().withSecond(0).withNano(0);
                LocalDateTime endMinuteUtc = startMinuteUtc.plusMinutes(1).minusNanos(1);
                String startMinuteStr = startMinuteUtc.format(ISO_FORMATTER);
                String endMinuteStr = endMinuteUtc.format(ISO_FORMATTER);
                query.append("timestamp", new Document("$gte", startMinuteStr)
                                             .append("$lte", endMinuteStr));
                System.out.println("Minute filter applied: minute=" + minute + ", EST=" + minuteZonedEst + 
                                   ", UTC range=[" + startMinuteStr + ", " + endMinuteStr + "]");
            } catch (DateTimeParseException e) {
                System.err.println("Error parsing minute parameter: " + minute + " - " + e.getMessage());
            }
        } else if (component != null && !component.isEmpty() && !"Unknown".equals(component)) {
            query.append("component", component);
            System.out.println("Component filter applied: " + component);
        } else if (outcome != null && !outcome.isEmpty() && !"Unknown".equals(outcome)) {
            query.append("outcome", outcome);
            System.out.println("Outcome filter applied: " + outcome);
        }

        // Apply time range filter for component or outcome filtering
        if (minute == null || minute.isEmpty()) {
            LocalDateTime startOfRange = startUtc.withSecond(0).withNano(0);
            LocalDateTime endOfRange = endUtc.withSecond(59).withNano(999999999);
            String startRangeStr = startOfRange.format(ISO_FORMATTER);
            String endRangeStr = endOfRange.format(ISO_FORMATTER);
            query.append("timestamp",
                    new Document("$gte", startRangeStr)
                            .append("$lte", endRangeStr));
            System.out.println("Time range filter applied: UTC range=[" + startRangeStr + ", " + endRangeStr + "]");
        }

        System.out.println("Executing query: " + query.toJson());
        for (Document doc : collection.find(query).limit(limit)) {
            System.out.println("Raw document: " + doc.toJson());
            Event event = parseEvent(doc);
            if (event == null) {
                System.out.println("Skipping document with _id: " + doc.get("_id") + " due to parsing error");
                continue;
            }
            events.add(event);
            System.out.println("Found event: _id=" + doc.get("_id") + ", timestamp=" + event.getTimestamp() + 
                               ", raw_timestamp=" + doc.getString("timestamp"));
        }
        System.out.println("Total events found: " + events.size());

        req.setAttribute("events", events);
        req.setAttribute("filterType", component != null ? "Component: " + component :
                                     outcome != null ? "Outcome: " + outcome :
                                     minute != null ? "Minute: " + minute : "Filtered Events");
        req.setAttribute("startDatetime", startLocal.format(INPUT_FORMATTER));
        req.setAttribute("endDatetime", endLocal.format(INPUT_FORMATTER));
        req.setAttribute("limit", limit);
        req.getRequestDispatcher("/WEB-INF/views/filteredEvents.jsp").forward(req, resp);
    }

    private Event parseEvent(Document doc) {
        try {
            Date timestamp = null;
            String timestampStr = doc.getString("timestamp") != null ? doc.getString("timestamp") : doc.getString("Timestamp");
            if (timestampStr != null) {
                try {
                    // Try parsing as ISO 8601 with nanoseconds
                    ZonedDateTime zdt = ZonedDateTime.parse(timestampStr, ISO_FORMATTER);
                    timestamp = Date.from(zdt.toInstant());
                } catch (DateTimeParseException e1) {
                    try {
                        // Fallback for simpler ISO format
                        LocalDateTime ldt = LocalDateTime.parse(timestampStr, FALLBACK_FORMATTER);
                        timestamp = Date.from(ldt.atZone(UTC_ZONE).toInstant());
                    } catch (DateTimeParseException e2) {
                        try {
                            // Try millisecond precision
                            LocalDateTime ldt = LocalDateTime.parse(timestampStr, SIMPLE_FORMATTER);
                            timestamp = Date.from(ldt.atZone(UTC_ZONE).toInstant());
                        } catch (DateTimeParseException e3) {
                            try {
                                // Try minimal format without milliseconds
                                LocalDateTime ldt = LocalDateTime.parse(timestampStr, MINIMAL_FORMATTER);
                                timestamp = Date.from(ldt.atZone(UTC_ZONE).toInstant());
                            } catch (DateTimeParseException e4) {
                                // Try additional format with nanoseconds
                                try {
                                    LocalDateTime ldt = LocalDateTime.parse(timestampStr, 
                                        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS'Z'"));
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

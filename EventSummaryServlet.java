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
import java.util.Map;
import java.util.TreeMap;

@WebServlet("/summary")
public class EventSummaryServlet extends HttpServlet {
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
    private static final DateTimeFormatter FALLBACK_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private static final DateTimeFormatter SIMPLE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final DateTimeFormatter MINIMAL_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter NANO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS'Z'");
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
        String startDatetimeParam = req.getParameter("startDatetime");
        String endDatetimeParam = req.getParameter("endDatetime");
        String limitParam = req.getParameter("limit");

        System.out.println("Summary request parameters: startDatetime=" + startDatetimeParam +
                           ", endDatetime=" + endDatetimeParam + ", limit=" + limitParam);

        LocalDateTime now = LocalDateTime.now(EST_ZONE).withSecond(0).withNano(0);
        LocalDateTime startLocal = startDatetimeParam != null && !startDatetimeParam.isEmpty() ?
                LocalDateTime.parse(startDatetimeParam, INPUT_FORMATTER) : now.minusHours(1);
        LocalDateTime endLocal = endDatetimeParam != null && !endDatetimeParam.isEmpty() ?
                LocalDateTime.parse(endDatetimeParam, INPUT_FORMATTER) : now;

        if (endLocal.isBefore(startLocal) || endLocal.equals(startLocal)) {
            endLocal = startLocal.plusHours(1);
        }

        ZonedDateTime startZoned = startLocal.atZone(EST_ZONE).withZoneSameInstant(UTC_ZONE);
        ZonedDateTime endZoned = endLocal.atZone(EST_ZONE).withZoneSameInstant(UTC_ZONE);
        LocalDateTime startUtc = startZoned.toLocalDateTime();
        LocalDateTime endUtc = endZoned.toLocalDateTime();

        int limit = limitParam != null && !limitParam.isEmpty() ? Integer.parseInt(limitParam) : 5000;
        if (limit <= 0) limit = 5000;

        Map<String, Integer> componentCounts = new TreeMap<>();
        Map<String, Integer> outcomeCounts = new TreeMap<>();
        Map<String, Integer> minuteCounts = new TreeMap<>();

        String startRangeStr = startUtc.withSecond(0).withNano(0).format(ISO_FORMATTER);
        String endRangeStr = endUtc.withSecond(59).withNano(999999999).format(ISO_FORMATTER);
        Document query = new Document("$or", List.of(
                new Document("timestamp", new Document("$gte", startRangeStr).append("$lte", endRangeStr)),
                new Document("timeStamp", new Document("$gte", startRangeStr).append("$lte", endRangeStr))
        ));

        System.out.println("Executing summary query: " + query.toJson());
        for (Document doc : collection.find(query).limit(limit)) {
            System.out.println("Raw document: " + doc.toJson());
            Event event = parseEvent(doc);
            if (event == null) {
                System.out.println("Skipping document with _id: " + doc.get("_id") + " due to parsing error");
                continue;
            }
            String component = event.getComponent();
            String outcome = event.getOutcome();
            Date timestamp = event.getTimestamp();
            if (timestamp != null) {
                LocalDateTime localDateTime = LocalDateTime.ofInstant(timestamp.toInstant(), UTC_ZONE)
                        .atZone(UTC_ZONE).withZoneSameInstant(EST_ZONE).toLocalDateTime();
                String minuteKey = localDateTime.format(MINUTE_FORMATTER);
                minuteCounts.merge(minuteKey, 1, Integer::sum);
            }
            componentCounts.merge(component, 1, Integer::sum);
            outcomeCounts.merge(outcome, 1, Integer::sum);
            System.out.println("Processed event: _id=" + doc.get("_id") + ", timestamp=" + event.getTimestamp() +
                               ", raw_timestamp=" + doc.getString("timestamp") +
                               ", raw_timeStamp=" + doc.getString("timeStamp"));
        }
        System.out.println("Total events processed: " + (componentCounts.values().stream().mapToInt(Integer::intValue).sum()));

        req.setAttribute("componentCounts", componentCounts);
        req.setAttribute("outcomeCounts", outcomeCounts);
        req.setAttribute("minuteCounts", minuteCounts);
        req.setAttribute("startDatetime", startLocal.format(INPUT_FORMATTER));
        req.setAttribute("endDatetime", endLocal.format(INPUT_FORMATTER));
        req.setAttribute("limit", limit);
        req.getRequestDispatcher("/WEB-INF/views/summary.jsp").forward(req, resp);
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

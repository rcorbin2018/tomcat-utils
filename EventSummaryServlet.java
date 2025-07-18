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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/summary")
public class EventSummaryServlet extends HttpServlet {
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
        String dateParam = req.getParameter("date");
        String limitParam = req.getParameter("limit");
        LocalDate selectedDate = dateParam != null ? LocalDate.parse(dateParam) : LocalDate.now();
        int limit = limitParam != null ? Integer.parseInt(limitParam) : 5000;
        if (limit <= 0) limit = 5000;

        LocalDateTime startOfDay = selectedDate.atStartOfDay();
        LocalDateTime endOfDay = selectedDate.atTime(23, 59, 59, 999999999);

        Map<String, Integer> componentCounts = new HashMap<>();
        Map<String, Integer> namespaceCounts = new HashMap<>();
        Map<String, Integer> outcomeCounts = new HashMap<>();
        Map<String, Integer> hourlyCounts = new HashMap<>();
        List<Event> recentEvents = new ArrayList<>();

        Document query = new Document("timestamp",
                new Document("$gte", startOfDay.format(ISO_FORMATTER))
                        .append("$lte", endOfDay.format(ISO_FORMATTER)));

        for (Document doc : collection.find(query).limit(limit)) {
            Event event = parseEvent(doc);
            if (event == null) {
                System.out.println("Skipping document with _id: " + doc.get("_id") + " due to parsing error");
                continue;
            }
            recentEvents.add(event);
            componentCounts.merge(event.getComponent(), 1, Integer::sum);
            namespaceCounts.merge(event.getNamespace(), 1, Integer::sum);
            outcomeCounts.merge(event.getOutcome(), 1, Integer::sum);
            if (event.getTimestamp() != null) {
                String hour = event.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH"));
                hourlyCounts.merge(hour, 1, Integer::sum);
            }
        }

        req.setAttribute("componentCounts", componentCounts);
        req.setAttribute("namespaceCounts", namespaceCounts);
        req.setAttribute("outcomeCounts", outcomeCounts);
        req.setAttribute("hourlyCounts", hourlyCounts);
        req.setAttribute("recentEvents", recentEvents);
        req.setAttribute("selectedDate", selectedDate.toString());
        req.setAttribute("limit", limit);

        req.getRequestDispatcher("/WEB-INF/views/summary.jsp").forward(req, resp);
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

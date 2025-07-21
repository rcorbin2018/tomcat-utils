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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/transaction")
public class TransactionReportServlet extends HttpServlet {
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
        String datetimeParam = req.getParameter("datetime");
        String limitParam = req.getParameter("limit");
        LocalDateTime selectedDateTime = datetimeParam != null ? LocalDateTime.parse(datetimeParam, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")) : LocalDateTime.now().withSecond(0).withNano(0);
        int limit = limitParam != null ? Integer.parseInt(limitParam) : 5000;
        if (limit <= 0) limit = 5000;

        Map<String, List<Event>> transactions = new HashMap<>();
        LocalDateTime startOfHour = selectedDateTime.withSecond(0).withNano(0);
        LocalDateTime endOfHour = startOfHour.plusHours(1).minusNanos(1);
        Document query = new Document("timestamp",
                new Document("$gte", startOfHour.format(ISO_FORMATTER))
                        .append("$lte", endOfHour.format(ISO_FORMATTER)));

        for (Document doc : collection.find(query).limit(limit)) {
            Event event = parseEvent(doc);
            if (event == null) {
                System.out.println("Skipping document with _id: " + doc.get("_id") + " due to parsing error");
                continue;
            }
            String transactionId = extractTransactionId(event.getEventDetails());
            transactions.computeIfAbsent(transactionId, k -> new ArrayList<>()).add(event);
        }

        req.setAttribute("transactions", transactions);
        req.setAttribute("selectedDateTime", selectedDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")));
        req.setAttribute("limit", limit);
        req.getRequestDispatcher("/WEB-INF/views/transaction.jsp").forward(req, resp);
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

    private String extractTransactionId(String eventDetails) {
        return eventDetails != null && eventDetails.contains("tx:") 
                ? eventDetails.split("tx:")[1].split("\\s")[0] 
                : "unknown";
    }

    @Override
    public void destroy() {
        mongoClient.close();
    }
}

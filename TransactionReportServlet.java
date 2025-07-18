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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/transaction")
public class TransactionReportServlet extends HttpServlet {
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
        Map<String, List<Event>> transactions = new HashMap<>();

        for (Document doc : collection.find().limit(100)) {
            Event event = parseEvent(doc);
            if (event == null) {
                System.out.println("Skipping document with _id: " + doc.get("_id") + " due to parsing error");
                continue;
            }
            // Assume transaction ID is part of eventDetails (e.g., "tx:ID")
            String transactionId = extractTransactionId(event.getEventDetails());
            transactions.computeIfAbsent(transactionId, k -> new ArrayList<>()).add(event);
        }

        req.setAttribute("transactions", transactions);
        req.getRequestDispatcher("/WEB-INF/views/transaction.jsp").forward(req, resp);
    }

    private Event parseEvent(Document doc) {
        try {
            LocalDateTime timestamp = null;
            if (doc.getDate("timestamp") != null) {
                timestamp = doc.getDate("timestamp").toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
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
        // Simplified: assume eventDetails contains "tx:ID"
        return eventDetails != null && eventDetails.contains("tx:") 
            ? eventDetails.split("tx:")[1].split("\\s")[0] 
            : "unknown";
    }

    @Override
    public void destroy() {
        mongoClient.close();
    }
}

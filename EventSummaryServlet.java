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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/summary")
public class EventSummaryServlet extends HttpServlet {
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
        // Aggregate data
        Map<String, Integer> componentCounts = new HashMap<>();
        Map<String, Integer> namespaceCounts = new HashMap<>();
        Map<String, Integer> outcomeCounts = new HashMap<>();
        Map<String, Integer> hourlyCounts = new HashMap<>();
        List<Event> recentEvents = new ArrayList<>();

        // Query MongoDB
        for (Document doc : collection.find().limit(100)) {
            Event event = parseEvent(doc);
            recentEvents.add(event);

            // Component counts
            componentCounts.merge(event.getComponent(), 1, Integer::sum);
            // Namespace counts
            namespaceCounts.merge(event.getNamespace(), 1, Integer::sum);
            // Outcome counts
            outcomeCounts.merge(event.getOutcome(), 1, Integer::sum);
            // Hourly counts
            String hour = event.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH"));
            hourlyCounts.merge(hour, 1, Integer::sum);
        }

        // Set attributes for JSP
        req.setAttribute("componentCounts", componentCounts);
        req.setAttribute("namespaceCounts", namespaceCounts);
        req.setAttribute("outcomeCounts", outcomeCounts);
        req.setAttribute("hourlyCounts", hourlyCounts);
        req.setAttribute("recentEvents", recentEvents);

        req.getRequestDispatcher("/WEB-INF/views/summary.jsp").forward(req, resp);
    }

    private Event parseEvent(Document doc) {
        return new Event(
            doc.getString("component"),
            doc.getString("namespace"),
            doc.getDate("timestamp").toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime(),
            doc.getString("outcome"),
            doc.getString("event"),
            doc.getString("eventDetails"),
            doc.getString("message")
        );
    }

    @Override
    public void destroy() {
        mongoClient.close();
    }
}

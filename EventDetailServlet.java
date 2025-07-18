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
import java.util.ArrayList;
import java.util.List;

@WebServlet("/detail")
public class EventDetailServlet extends HttpServlet {
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
        if (component != null && !component.isEmpty()) query.append("component", component);
        if (namespace != null && !namespace.isEmpty()) query.append("namespace", namespace);
        if (outcome != null && !outcome.isEmpty()) query.append("outcome", outcome);

        for (Document doc : collection.find(query).limit(100)) {
            events.add(new Event(
                doc.getString("component"),
                doc.getString("namespace"),
                doc.getDate("timestamp").toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime(),
                doc.getString("outcome"),
                doc.getString("event"),
                doc.getString("eventDetails"),
                doc.getString("message")
            ));
        }

        req.setAttribute("events", events);
        req.getRequestDispatcher("/WEB-INF/views/detail.jsp").forward(req, resp);
    }

    @Override
    public void destroy() {
        mongoClient.close();
    }
}

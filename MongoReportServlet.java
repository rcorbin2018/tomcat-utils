package com.example;

import com.mongodb.client.*;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Projections;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@WebServlet("/report")
public class MongoReportServlet extends HttpServlet {
    private MongoClient mongoClient;

    @Override
    public void init() throws ServletException {
        // Initialize MongoDB client
        mongoClient = MongoClients.create("mongodb://localhost:27017");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            // Access database and collection
            MongoDatabase database = mongoClient.getDatabase("myDB");
            MongoCollection<Document> collection = database.getCollection("myCollection");

            // Aggregation pipeline to group by day, namespace, and component
            List<Bson> pipeline = Arrays.asList(
                // Project to extract date components
                Aggregates.project(Projections.fields(
                    Projections.include("namespace", "component"),
                    Projections.computed("date", new Document("$dateToString", 
                        new Document("format", "%Y-%m-%d")
                            .append("date", "$timestamp"))
                    )
                )),
                // Group by date, namespace, and component
                Aggregates.group(
                    new Document()
                        .append("date", "$date")
                        .append("namespace", "$namespace")
                        .append("component", "$component"),
                    Accumulators.sum("count", 1) // Use Accumulators helper
                ),
                // Sort by date, namespace, component
                Aggregates.sort(new Document("_id.date", 1)
                    .append("_id.namespace", 1)
                    .append("_id.component", 1))
            );

            // Execute aggregation
            List<Document> results = collection.aggregate(pipeline).into(new ArrayList<>());

            // Pass results to JSP
            req.setAttribute("results", results);
            req.getRequestDispatcher("/report.jsp").forward(req, resp);

        } catch (Exception e) {
            throw new ServletException("Error querying MongoDB", e);
        }
    }

    @Override
    public void destroy() {
        // Close MongoDB client
        if (mongoClient != null) {
            mongoClient.close();
        }
    }
}

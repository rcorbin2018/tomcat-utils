package com.example;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Projections;
import org.bson.Document;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@WebServlet("/report")
public class ReportServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Connect to MongoDB
        try (MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017")) {
            MongoDatabase database = mongoClient.getDatabase("mydb"); // Replace with your database name
            MongoCollection<Document> collection = database.getCollection("mycollection"); // Replace with your collection name

            // Aggregation pipeline
            List<Document> results = collection.aggregate(Arrays.asList(
                // Group by date (day), namespace, and component
                Aggregates.group(
                    new Document()
                        .append("day", new Document("$dateToString", new Document("format", "%Y-%m-%d").append("date", "$timestamp")))
                        .append("namespace", "$namespace")
                        .append("component", "$component"),
                    new Document("count", new Document("$sum", 1))
                ),
                // Project to shape the output
                Aggregates.project(
                    Projections.fields(
                        Projections.excludeId(),
                        Projections.computed("day", "$_id.day"),
                        Projections.computed("namespace", "$_id.namespace"),
                        Projections.computed("component", "$_id.component"),
                        Projections.include("count")
                    )
                ),
                // Sort by day, namespace, component
                Aggregates.sort(new Document("day", 1).append("namespace", 1).append("component", 1))
            )).into(new ArrayList<>());

            // Pass results to JSP
            request.setAttribute("results", results);
            request.getRequestDispatcher("/report.jsp").forward(request, response);
        } catch (Exception e) {
            throw new ServletException("Error querying MongoDB", e);
        }
    }
}

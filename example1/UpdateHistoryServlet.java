package com.nibroc.classcatalog;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@WebServlet("/updateHistory")
@MultipartConfig
public class UpdateHistoryServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        List<Map<String, String>> history = (List<Map<String, String>>) session.getAttribute("reloadHistory");
        if (history == null) {
            history = new ArrayList<>();
            session.setAttribute("reloadHistory", history);
            System.out.println("Initialized empty history, session ID: " + session.getId());
        }

        System.out.println("Received updateHistory, session ID: " + session.getId());
        int transactionCount = Integer.parseInt(request.getParameter("transactionCount"));
        for (int i = 1; i <= transactionCount; i++) {
            Map<String, String> historyEntry = new HashMap<>();
            historyEntry.put("timestamp", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            historyEntry.put("transaction", String.valueOf(i));
            historyEntry.put("serviceAName", request.getParameter("serviceAName_" + i));
            historyEntry.put("serviceBName", request.getParameter("serviceBName_" + i));
            historyEntry.put("serviceCName", request.getParameter("serviceCName_" + i));
            historyEntry.put("statusA", request.getParameter("statusA_" + i));
            historyEntry.put("statusB", request.getParameter("statusB_" + i));
            historyEntry.put("statusC", request.getParameter("statusC_" + i));
            historyEntry.put("delayA", request.getParameter("delayA"));
            historyEntry.put("delayB", request.getParameter("delayB"));
            historyEntry.put("delayC", request.getParameter("delayC"));
            history.add(0, historyEntry);
            System.out.println("Added history entry for T" + i + ": " + historyEntry);
        }
        session.setAttribute("reloadHistory", history);
        System.out.println("Updated session: history size=" + history.size());
        response.setContentType("text/plain");
        response.getWriter().print("History updated");
    }
}
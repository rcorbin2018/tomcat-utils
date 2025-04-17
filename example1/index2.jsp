<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, java.text.SimpleDateFormat" %>
<%
// Set session timeout to 30 minutes
session.setMaxInactiveInterval(1800);

// Handle clear history action
if ("clear".equals(request.getParameter("action"))) {
    session.removeAttribute("reloadHistory");
    System.out.println("History cleared, session ID: " + session.getId());
}

// Initialize history if needed
List<Map<String, String>> history = (List<Map<String, String>>) session.getAttribute("reloadHistory");
if (history == null) {
    history = new ArrayList<>();
    session.setAttribute("reloadHistory", history);
    System.out.println("Initialized empty history, session ID: " + session.getId());
}

// Number of transaction rows
int transactionCount = 2;

// Get current configuration
String refreshInterval = request.getParameter("refreshInterval") != null ? request.getParameter("refreshInterval") : "60";
String delayA = request.getParameter("delayA") != null ? request.getParameter("delayA") : "1000";
String delayB = request.getParameter("delayB") != null ? request.getParameter("delayB") : "2000";
String delayC = request.getParameter("delayC") != null ? request.getParameter("delayC") : "3000";

// Get transaction-specific service names
String[] serviceANames = new String[transactionCount + 1];
String[] serviceBNames = new String[transactionCount + 1];
String[] serviceCNames = new String[transactionCount + 1];
for (int i = 1; i <= transactionCount; i++) {
    serviceANames[i] = request.getParameter("serviceAName_" + i) != null ? request.getParameter("serviceAName_" + i) : "Service A T" + i;
    serviceBNames[i] = request.getParameter("serviceBName_" + i) != null ? request.getParameter("serviceBName_" + i) : "Service B T" + i;
    serviceCNames[i] = request.getParameter("serviceCName_" + i) != null ? request.getParameter("serviceCName_" + i) : "Service C T" + i;
}
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta http-equiv="refresh" content="<%= refreshInterval %>">
    <title>Service Rectangles</title>
    <style>
        .rectangle {
            width: 100px;
            height: 100px;
            display: inline-block;
            margin: 10px 40px;
            border: 2px solid black;
            text-align: center;
            line-height: 100px;
            color: white;
            font-weight: bold;
            vertical-align: middle;
            background-color: #CCCC00; /* Muted yellow default */
        }
        .transaction-table {
            margin: 20px auto;
            border-collapse: collapse;
            width: 80%;
        }
        .transaction-table tr {
            border-bottom: 1px solid rgba(200, 200, 200, 0.5);
        }
        .transaction-table tr:last-child {
            border-bottom: none;
        }
        .transaction-table td {
            text-align: center;
            vertical-align: middle;
            padding: 10px;
        }
        .transaction-table th {
            background-color: #f2f2f2;
            padding: 10px;
        }
        .arrow {
            display: inline-block;
            vertical-align: middle;
            margin: 0 -30px;
        }
        .config-form {
            margin: 20px;
            text-align: center;
        }
        .config-form label {
            margin-right: 10px;
        }
        .config-form input {
            margin: 5px;
            padding: 5px;
        }
        .history-table-t1, .history-table-t2 {
            margin: 20px auto;
            border-collapse: collapse;
            width: 80%;
        }
        .history-table-t1 th, .history-table-t1 td,
        .history-table-t2 th, .history-table-t2 td {
            border: 1px solid black;
            padding: 8px;
            text-align: center;
        }
        .history-table-t1 th, .history-table-t2 th {
            background-color: #f2f2f2;
        }
        .status-good {
            background-color: #008000; /* Muted green */
            color: white;
        }
        .status-bad {
            background-color: #800000; /* Muted red */
            color: white;
        }
        .clear-history {
            margin: 10px;
            padding: 10px;
            background-color: #d3d3d3;
            border: none;
            cursor: pointer;
        }
        .clear-history:hover {
            background-color: #b0b0b0;
        }
    </style>
    <script>
        // Configurable variables
        const transactionCount = <%= transactionCount %>;
        const serviceNames = [
            {},
            <% for (int i = 1; i <= transactionCount; i++) { %>
                {
                    serviceA: "<%= serviceANames[i] %>",
                    serviceB: "<%= serviceBNames[i] %>",
                    serviceC: "<%= serviceCNames[i] %>"
                }<%= i < transactionCount ? "," : "" %>
            <% } %>
        ];
        const delays = {
            serviceA: <%= delayA %>,
            serviceB: <%= delayB %>,
            serviceC: <%= delayC %>
        };

        // Set service names and handle color changes
        window.onload = function() {
            console.log('Page loaded, starting status checks');
            const transactionStatuses = {};

            for (let i = 1; i <= transactionCount; i++) {
                transactionStatuses[i] = {
                    statusA: null,
                    statusB: null,
                    statusC: null
                };

                // Set service names
                const serviceAElement = document.getElementById('serviceA_' + i);
                const serviceBElement = document.getElementById('serviceB_' + i);
                const serviceCElement = document.getElementById('serviceC_' + i);
                if (serviceAElement && serviceBElement && serviceCElement) {
                    serviceAElement.innerText = serviceNames[i].serviceA;
                    serviceBElement.innerText = serviceNames[i].serviceB;
                    serviceCElement.innerText = serviceNames[i].serviceC;
                } else {
                    console.error('Service elements not found for transaction ' + i);
                }

                // Simulate health checks (REST commented out, assume Good)
                setTimeout(function() {
                    if (serviceAElement) {
                        serviceAElement.style.backgroundColor = '#008000'; // Muted green
                        transactionStatuses[i].statusA = 'Good';
                        console.log('Service A T' + i + ': Good, set to green');
                    }
                }, delays.serviceA);

                setTimeout(function() {
                    if (serviceBElement) {
                        serviceBElement.style.backgroundColor = '#008000';
                        transactionStatuses[i].statusB = 'Good';
                        console.log('Service B T' + i + ': Good, set to green');
                    }
                }, delays.serviceB);

                setTimeout(function() {
                    if (serviceCElement) {
                        serviceCElement.style.backgroundColor = '#008000';
                        transactionStatuses[i].statusC = 'Good';
                        console.log('Service C T' + i + ': Good, set to green');
                    }

                    // Submit history after all delays
                    if (i === transactionCount) {
                        setTimeout(function() {
                            console.log('Calling submitHistory');
                            submitHistory(transactionStatuses);
                        }, Math.max(delays.serviceA, delays.serviceB, delays.serviceC) - delays.serviceC + 100);
                    }
                }, delays.serviceC);
            }

            // Submit history and update tables
            function submitHistory(statuses) {
                console.log('submitHistory called with statuses:', statuses);
                const formData = new FormData();
                formData.append('action', 'updateHistory');
                formData.append('transactionCount', transactionCount);
                formData.append('delayA', delays.serviceA);
                formData.append('delayB', delays.serviceB);
                formData.append('delayC', delays.serviceC);

                // Format timestamp as YYYY-MM-DD HH:MM:SS
                const now = new Date();
                const timestamp = now.getFullYear() + '-' +
                    String(now.getMonth() + 1).padStart(2, '0') + '-' +
                    String(now.getDate()).padStart(2, '0') + ' ' +
                    String(now.getHours()).padStart(2, '0') + ':' +
                    String(now.getMinutes()).padStart(2, '0') + ':' +
                    String(now.getSeconds()).padStart(2, '0');

                for (let i = 1; i <= transactionCount; i++) {
                    formData.append('serviceAName_' + i, serviceNames[i].serviceA || '');
                    formData.append('serviceBName_' + i, serviceNames[i].serviceB || '');
                    formData.append('serviceCName_' + i, serviceNames[i].serviceC || '');
                    formData.append('statusA_' + i, statuses[i].statusA || 'Good');
                    formData.append('statusB_' + i, statuses[i].statusB || 'Good');
                    formData.append('statusC_' + i, statuses[i].statusC || 'Good');

                    const tbody = document.querySelector('.history-table-t' + i + ' tbody');
                    if (!tbody) {
                        console.error('History table tbody for T' + i + ' not found');
                        continue;
                    }

                    const row = document.createElement('tr');
                    const statusAClass = statuses[i].statusA === 'Good' ? 'status-good' : 'status-bad';
                    const statusBClass = statuses[i].statusB === 'Good' ? 'status-good' : 'status-bad';
                    const statusCClass = statuses[i].statusC === 'Good' ? 'status-good' : 'status-bad';
                    row.innerHTML =
                        '<td>' + timestamp + '</td>' +
                        '<td>' + (serviceNames[i].serviceA || '') + '</td>' +
                        '<td class="' + statusAClass + '">' + (statuses[i].statusA || 'Good') + '</td>' +
                        '<td>' + (serviceNames[i].serviceB || '') + '</td>' +
                        '<td class="' + statusBClass + '">' + (statuses[i].statusB || 'Good') + '</td>' +
                        '<td>' + (serviceNames[i].serviceC || '') + '</td>' +
                        '<td class="' + statusCClass + '">' + (statuses[i].statusC || 'Good') + '</td>' +
                        '<td>' + delays.serviceA + '</td>' +
                        '<td>' + delays.serviceB + '</td>' +
                        '<td>' + delays.serviceC + '</td>';

                    try {
                        tbody.insertBefore(row, tbody.firstChild);
                        console.log('Added history row for Transaction ' + i);
                    } catch (e) {
                        console.error('Failed to add history row for T' + i + ':', e);
                        tbody.appendChild(row);
                    }
                }

                // Fetch to servlet
                const url = window.location.origin + '<%= request.getContextPath() %>/updateHistory';
                console.log('Fetching:', url);
                fetch(url, {
                    method: 'POST',
                    body: formData
                })
                .then(response => {
                    if (!response.ok) throw new Error('Network response was not ok: ' + response.status);
                    return response.text();
                })
                .then(data => {
                    console.log('Server response:', data);
                })
                .catch(error => {
                    console.error('Error updating history:', error);
                });
            }
        };
    </script>
</head>
<body>
    <h2>Service Status</h2>
    <!-- Configuration form -->
    <div class="config-form">
        <form action="index2.jsp" method="post">
            <div>
                <label for="refreshInterval">Refresh Interval (seconds):</label>
                <input type="number" id="refreshInterval" name="refreshInterval" value="<%= refreshInterval %>" min="1" required>
            </div>
            <% for (int i = 1; i <= transactionCount; i++) { %>
                <h4>Transaction <%= i %> Services</h4>
                <div>
                    <label for="serviceAName_<%= i %>">Service A Name:</label>
                    <input type="text" id="serviceAName_<%= i %>" name="serviceAName_<%= i %>" value="<%= serviceANames[i] %>" required>
                </div>
                <div>
                    <label for="serviceBName_<%= i %>">Service B Name:</label>
                    <input type="text" id="serviceBName_<%= i %>" name="serviceBName_<%= i %>" value="<%= serviceBNames[i] %>" required>
                </div>
                <div>
                    <label for="serviceCName_<%= i %>">Service C Name:</label>
                    <input type="text" id="serviceCName_<%= i %>" name="serviceCName_<%= i %>" value="<%= serviceCNames[i] %>" required>
                </div>
            <% } %>
            <div>
                <label for="delayA">Service A Delay (ms):</label>
                <input type="number" id="delayA" name="delayA" value="<%= delayA %>" min="0" required>
            </div>
            <div>
                <label for="delayB">Service B Delay (ms):</label>
                <input type="number" id="delayB" name="delayB" value="<%= delayB %>" min="0" required>
            </div>
            <div>
                <label for="delayC">Service C Delay (ms):</label>
                <input type="number" id="delayC" name="delayC" value="<%= delayC %>" min="0" required>
            </div>
            <div>
                <input type="submit" value="Update Settings">
            </div>
        </form>
    </div>
    <!-- Transaction table -->
    <div>
        <h3>Transactions</h3>
        <table class="transaction-table">
            <tr>
                <th>Transaction</th>
                <th>Services</th>
            </tr>
            <% for (int i = 1; i <= transactionCount; i++) { %>
                <tr>
                    <td>Transaction <%= i %></td>
                    <td>
                        <div class="rectangle" id="serviceA_<%= i %>"></div>
                        <svg class="arrow" width="40" height="20">
                            <defs>
                                <marker id="arrowhead" markerWidth="10" markerHeight="7" refX="10" refY="3.5" orient="auto">
                                    <polygon points="0 0, 10 3.5, 0 7" fill="black" />
                                </marker>
                            </defs>
                            <line x1="0" y1="10" x2="40" y2="10" stroke="black" stroke-width="2" marker-end="url(#arrowhead)" />
                        </svg>
                        <div class="rectangle" id="serviceB_<%= i %>"></div>
                        <svg class="arrow" width="40" height="20">
                            <line x1="0" y1="10" x2="40" y2="10" stroke="black" stroke-width="2" marker-end="url(#arrowhead)" />
                        </svg>
                        <div class="rectangle" id="serviceC_<%= i %>"></div>
                    </td>
                </tr>
            <% } %>
        </table>
    </div>
    <!-- History tables -->
    <div>
        <h3>Reload History</h3>
        <p>Session ID: <%= session.getId() %></p>
        <form action="index2.jsp" method="post">
            <input type="hidden" name="action" value="clear">
            <input type="submit" value="Clear History" class="clear-history">
        </form>
        <% for (int i = 1; i <= transactionCount; i++) { %>
            <h4>Transaction <%= i %> History</h4>
            <table class="history-table-t<%= i %>">
                <thead>
                    <tr>
                        <th>Timestamp</th>
                        <th>Service A Name</th>
                        <th>Service A Status</th>
                        <th>Service B Name</th>
                        <th>Service B Status</th>
                        <th>Service C Name</th>
                        <th>Service C Status</th>
                        <th>Delay A (ms)</th>
                        <th>Delay B (ms)</th>
                        <th>Delay C (ms)</th>
                    </tr>
                </thead>
                <tbody>
                    <% 
                    for (Map<String, String> row : history) {
                        if (String.valueOf(i).equals(row.get("transaction"))) { %>
                            <tr>
                                <td><%= row.get("timestamp") != null ? row.get("timestamp") : "" %></td>
                                <td><%= row.get("serviceAName") != null ? row.get("serviceAName") : "" %></td>
                                <td class="<%= row.get("statusA") != null && row.get("statusA").equals("Good") ? "status-good" : "status-bad" %>">
                                    <%= row.get("statusA") != null ? row.get("statusA") : "" %>
                                </td>
                                <td><%= row.get("serviceBName") != null ? row.get("serviceBName") : "" %></td>
                                <td class="<%= row.get("statusB") != null && row.get("statusB").equals("Good") ? "status-good" : "status-bad" %>">
                                    <%= row.get("statusB") != null ? row.get("statusB") : "" %>
                                </td>
                                <td><%= row.get("serviceCName") != null ? row.get("serviceCName") : "" %></td>
                                <td class="<%= row.get("statusC") != null && row.get("statusC").equals("Good") ? "status-good" : "status-bad" %>">
                                    <%= row.get("statusC") != null ? row.get("statusC") : "" %>
                                </td>
                                <td><%= row.get("delayA") != null ? row.get("delayA") : "" %></td>
                                <td><%= row.get("delayB") != null ? row.get("delayB") : "" %></td>
                                <td><%= row.get("delayC") != null ? row.get("delayC") : "" %></td>
                            </tr>
                        <% }
                    } %>
                </tbody>
            </table>
        <% } %>
    </div>
</body>
</html>
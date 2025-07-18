<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Event Summary Report</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/css/styles.css" rel="stylesheet">
    <script src="https://cdn.jsdelivr.net/npm/chart.js@3.9.1/dist/chart.min.js"></script>
</head>
<body>
    <div class="container">
        <h1 class="my-4">Event Summary Report</h1>
        
        <!-- Navigation -->
        <ul class="nav nav-tabs mb-4">
            <li class="nav-item"><a class="nav-link active" href="summary">Summary</a></li>
            <li class="nav-item"><a class="nav-link" href="detail">Details</a></li>
            <li class="nav-item"><a class="nav-link" href="transaction">Transactions</a></li>
        </ul>

        <!-- Charts -->
        <div class="row">
            <div class="col-md-6">
                <h3>Events by Component</h3>
                <canvas id="componentChart"></canvas>
            </div>
            <div class="col-md-6">
                <h3>Events by Outcome</h3>
                <canvas id="outcomeChart"></canvas>
            </div>
        </div>
        <div class="row mt-4">
            <div class="col-md-12">
                <h3>Events by Hour</h3>
                <canvas id="hourlyChart"></canvas>
            </div>
        </div>

        <!-- Recent Events Table -->
        <h3 class="mt-4">Recent Events</h3>
        <table class="table table-striped">
            <thead>
                <tr>
                    <th>Timestamp</th>
                    <th>Component</th>
                    <th>Namespace</th>
                    <th>Event</th>
                    <th>Outcome</th>
                    <th>Message</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="event" items="${recentEvents}">
                    <tr>
                        <td><c:out value="${event.timestamp != null ? event.timestamp : 'N/A'}" /></td>
                        <td><c:out value="${event.component != null ? event.component : 'N/A'}" /></td>
                        <td><c:out value="${event.namespace != null ? event.namespace : 'N/A'}" /></td>
                        <td><c:out value="${event.event != null ? event.event : 'N/A'}" /></td>
                        <td class="${event.outcome == 'fail' ? 'text-danger' : event.outcome == 'pass' ? 'text-success' : ''}">
                            <c:out value="${event.outcome != null ? event.outcome : 'N/A'}" />
                        </td>
                        <td><c:out value="${event.message != null ? event.message : 'N/A'}" /></td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
    </div>

    <script src="${pageContext.request.contextPath}/js/charts.js"></script>
    <script>
        // Component Chart
        new Chart(document.getElementById('componentChart'), {
            type: 'pie',
            data: {
                labels: [<c:forEach var="entry" items="${componentCounts}">'${entry.key}',</c:forEach>],
                datasets: [{
                    data: [<c:forEach var="entry" items="${componentCounts}">${entry.value},</c:forEach>],
                    backgroundColor: ['#FF6384', '#36A2EB', '#FFCE56', '#4BC0C0', '#9966FF']
                }]
            }
        });

        // Outcome Chart
        new Chart(document.getElementById('outcomeChart'), {
            type: 'bar',
            data: {
                labels: [<c:forEach var="entry" items="${outcomeCounts}">'${entry.key}',</c:forEach>],
                datasets: [{
                    label: 'Event Count',
                    data: [<c:forEach var="entry" items="${outcomeCounts}">${entry.value},</c:forEach>],
                    backgroundColor: ['#FF6384', '#36A2EB']
                }]
            }
        });

        // Hourly Chart
        new Chart(document.getElementById('hourlyChart'), {
            type: 'line',
            data: {
                labels: [<c:forEach var="entry" items="${hourlyCounts}">'${entry.key}',</c:forEach>],
                datasets: [{
                    label: 'Events per Hour',
                    data: [<c:forEach var="entry" items="${hourlyCounts}">${entry.value},</c:forEach>],
                    borderColor: '#36A2EB',
                    fill: false
                }]
            }
        });
    </script>
</body>
</html>

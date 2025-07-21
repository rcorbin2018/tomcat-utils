<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
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
        <h1 class="my-4">Event Summary Report - Between <c:out value="${startDatetime}" /> and <c:out value="${endDatetime}" /> (EST)</h1>
        
        <!-- Navigation -->
        <ul class="nav nav-tabs mb-4">
            <li class="nav-item"><a class="nav-link active" href="summary?startDatetime=${startDatetime}&endDatetime=${endDatetime}&limit=${limit}">Summary</a></li>
            <li class="nav-item"><a class="nav-link" href="detail?startDatetime=${startDatetime}&endDatetime=${endDatetime}&limit=${limit}">Details</a></li>
            <li class="nav-item"><a class="nav-link" href="transaction?startDatetime=${startDatetime}&endDatetime=${endDatetime}&limit=${limit}">Transactions</a></li>
        </ul>

        <!-- DateTime and Limit Form -->
        <form class="mb-4" method="get" action="summary">
            <div class="row">
                <div class="col-md-3">
                    <label for="startDatetime" class="form-label">Start Date and Time (EST)</label>
                    <input type="datetime-local" name="startDatetime" id="startDatetime" class="form-control" value="${startDatetime}" required step="60">
                </div>
                <div class="col-md-3">
                    <label for="endDatetime" class="form-label">End Date and Time (EST)</label>
                    <input type="datetime-local" name="endDatetime" id="endDatetime" class="form-control" value="${endDatetime}" required step="60">
                </div>
                <div class="col-md-3">
                    <label for="limit" class="form-label">Event Limit</label>
                    <input type="number" name="limit" id="limit" class="form-control" value="${limit}" min="1" placeholder="5000">
                </div>
                <div class="col-md-3 align-self-end">
                    <button type="submit" class="btn btn-primary">Apply</button>
                </div>
            </div>
        </form>

        <!-- No Events Message -->
        <c:if test="${empty recentEvents}">
            <div class="alert alert-info">No events found between <c:out value="${startDatetime}" /> and <c:out value="${endDatetime}" /> (EST).</div>
        </c:if>

        <!-- Charts -->
        <c:if test="${not empty recentEvents}">
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
                    <h3>Events by Minute (EST)</h3>
                    <canvas id="minuteChart"></canvas>
                </div>
            </div>

            <!-- Recent Events Table -->
            <h3 class="mt-4">Recent Events</h3>
            <table class="table table-striped">
                <thead>
                    <tr>
                        <th>Timestamp (EST)</th>
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
                            <td>
                                <c:choose>
                                    <c:when test="${event.timestamp != null}">
                                        <%-- Truncate to milliseconds by formatting to string and re-parsing --%>
                                        <fmt:formatDate value="${event.timestamp}" pattern="yyyy-MM-dd'T'HH:mm:ss.SSS" var="truncatedTimestamp" />
                                        <fmt:parseDate value="${truncatedTimestamp}" pattern="yyyy-MM-dd'T'HH:mm:ss.SSS" var="parsedDate" />
                                        <fmt:formatDate value="${parsedDate}" pattern="yyyy-MM-dd HH:mm:ss" timeZone="America/New_York" />
                                    </c:when>
                                    <c:otherwise>N/A</c:otherwise>
                                </c:choose>
                            </td>
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
        </c:if>
    </div>

    <script src="${pageContext.request.contextPath}/js/charts.js"></script>
    <script>
        <c:if test="${not empty recentEvents}">
            // Component Chart
            new Chart(document.getElementById('componentChart'), {
                type: 'pie',
                data: {
                    labels: [<c:forEach var="entry" items="${componentCounts}">'${entry.key}',</c:forEach>],
                    datasets: [{
                        data: [<c:forEach var="entry" items="${componentCounts}">${entry.value},</c:forEach>],
                        backgroundColor: ['#FF6384', '#36A2EB', '#FFCE56', '#4BC0C0', '#9966FF']
                    }]
                },
                options: {
                    onClick: (e, elements) => {
                        if (elements.length > 0) {
                            const index = elements[0].index;
                            const component = e.chart.data.labels[index];
                            const startDatetime = '${startDatetime}';
                            const endDatetime = '${endDatetime}';
                            const limit = '${limit}';
                            window.location.href = 'filteredEvents?component=' + encodeURIComponent(component) + '&startDatetime=' + startDatetime + '&endDatetime=' + endDatetime + '&limit=' + limit;
                        }
                    }
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
                },
                options: {
                    onClick: (e, elements) => {
                        if (elements.length > 0) {
                            const index = elements[0].index;
                            const outcome = e.chart.data.labels[index];
                            const startDatetime = '${startDatetime}';
                            const endDatetime = '${endDatetime}';
                            const limit = '${limit}';
                            window.location.href = 'filteredEvents?outcome=' + encodeURIComponent(outcome) + '&startDatetime=' + startDatetime + '&endDatetime=' + endDatetime + '&limit=' + limit;
                        }
                    }
                }
            });

            // Minute Chart
            new Chart(document.getElementById('minuteChart'), {
                type: 'line',
                data: {
                    labels: [<c:forEach var="entry" items="${minuteCounts}">'${entry.key}',</c:forEach>],
                    datasets: [{
                        label: 'Events per Minute',
                        data: [<c:forEach var="entry" items="${minuteCounts}">${entry.value},</c:forEach>],
                        borderColor: '#36A2EB',
                        fill: false
                    }]
                },
                options: {
                    onClick: (e, elements) => {
                        if (elements.length > 0) {
                            const index = elements[0].index;
                            const minute = e.chart.data.labels[index];
                            const startDatetime = '${startDatetime}';
                            const endDatetime = '${endDatetime}';
                            const limit = '${limit}';
                            window.location.href = 'filteredEvents?minute=' + encodeURIComponent(minute) + '&startDatetime=' + startDatetime + '&endDatetime=' + endDatetime + '&limit=' + limit;
                        }
                    }
                }
            });
        </c:if>
    </script>
</body>
</html>

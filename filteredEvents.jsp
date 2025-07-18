<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Filtered Events</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/css/styles.css" rel="stylesheet">
</head>
<body>
    <div class="container">
        <h1 class="my-4">Filtered Events - <c:out value="${filterType}" /> - <c:out value="${selectedDate}" /></h1>
        
        <!-- Navigation -->
        <ul class="nav nav-tabs mb-4">
            <li class="nav-item"><a class="nav-link" href="summary?date=${selectedDate}&limit=${limit}">Summary</a></li>
            <li class="nav-item"><a class="nav-link" href="detail?date=${selectedDate}&limit=${limit}">Details</a></li>
            <li class="nav-item"><a class="nav-link" href="transaction?date=${selectedDate}&limit=${limit}">Transactions</a></li>
            <li class="nav-item"><a class="nav-link active" href="#">Filtered Events</a></li>
        </ul>

        <!-- Date and Limit Form -->
        <form class="mb-4" method="get" action="filteredEvents">
            <div class="row">
                <div class="col-md-3">
                    <label for="date" class="form-label">Select Date</label>
                    <input type="date" name="date" id="date" class="form-control" value="${selectedDate}" required>
                </div>
                <div class="col-md-3">
                    <label for="limit" class="form-label">Event Limit</label>
                    <input type="number" name="limit" id="limit" class="form-control" value="${limit}" min="1" placeholder="5000">
                </div>
                <c:if test="${not empty param.component}">
                    <input type="hidden" name="component" value="${param.component}">
                </c:if>
                <c:if test="${not empty param.outcome}">
                    <input type="hidden" name="outcome" value="${param.outcome}">
                </c:if>
                <c:if test="${not empty param.hour}">
                    <input type="hidden" name="hour" value="${param.hour}">
                </c:if>
                <div class="col-md-3 align-self-end">
                    <button type="submit" class="btn btn-primary">Apply</button>
                </div>
            </div>
        </form>

        <!-- No Events Message -->
        <c:if test="${empty events}">
            <div class="alert alert-info">No events found for <c:out value="${filterType}" /> on <c:out value="${selectedDate}" />.</div>
        </c:if>

        <!-- Filtered Events Table -->
        <c:if test="${not empty events}">
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
                    <c:forEach var="event" items="${events}">
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
        </c:if>
    </div>
</body>
</html>

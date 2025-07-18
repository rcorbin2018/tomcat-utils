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
        <h1 class="my-4">Filtered Events - <c:out value="${filterType}" /></h1>
        
        <!-- Navigation -->
        <ul class="nav nav-tabs mb-4">
            <li class="nav-item"><a class="nav-link" href="summary">Summary</a></li>
            <li class="nav-item"><a class="nav-link" href="detail">Details</a></li>
            <li class="nav-item"><a class="nav-link" href="transaction">Transactions</a></li>
            <li class="nav-item"><a class="nav-link active" href="#">Filtered Events</a></li>
        </ul>

        <!-- No Events Message -->
        <c:if test="${empty events}">
            <div class="alert alert-info">No events found for <c:out value="${filterType}" /> in the last 24 hours.</div>
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

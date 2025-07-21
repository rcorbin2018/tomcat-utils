<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Event Details</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/css/styles.css" rel="stylesheet">
</head>
<body>
    <div class="container">
        <h1 class="my-4">Event Details - <c:out value="${selectedDateTime}" /></h1>
        
        <!-- Navigation -->
        <ul class="nav nav-tabs mb-4">
            <li class="nav-item"><a class="nav-link" href="summary?datetime=${selectedDateTime}&limit=${limit}">Summary</a></li>
            <li class="nav-item"><a class="nav-link active" href="detail?datetime=${selectedDateTime}&limit=${limit}">Details</a></li>
            <li class="nav-item"><a class="nav-link" href="transaction?datetime=${selectedDateTime}&limit=${limit}">Transactions</a></li>
        </ul>

        <!-- Filter Form -->
        <form class="mb-4" method="get" action="detail">
            <div class="row">
                <div class="col-md-3">
                    <label for="datetime" class="form-label">Select Date and Time</label>
                    <input type="datetime-local" name="datetime" id="datetime" class="form-control" value="${selectedDateTime}" required step="60">
                </div>
                <div class="col-md-3">
                    <label for="limit" class="form-label">Event Limit</label>
                    <input type="number" name="limit" id="limit" class="form-control" value="${limit}" min="1" placeholder="5000">
                </div>
                <div class="col-md-3">
                    <input type="text" name="component" class="form-control" placeholder="Component" value="${param.component}">
                </div>
                <div class="col-md-3">
                    <input type="text" name="namespace" class="form-control" placeholder="Namespace" value="${param.namespace}">
                </div>
                <div class="col-md-3">
                    <select name="outcome" class="form-control">
                        <option value="">All Outcomes</option>
                        <option value="pass" ${param.outcome == 'pass' ? 'selected' : ''}>Pass</option>
                        <option value="fail" ${param.outcome == 'fail' ? 'selected' : ''}>Fail</option>
                    </select>
                </div>
                <div class="col-md-3 align-self-end">
                    <button type="submit" class="btn btn-primary">Filter</button>
                </div>
            </div>
        </form>

        <!-- No Events Message -->
        <c:if test="${empty events}">
            <div class="alert alert-info">No events found for <c:out value="${selectedDateTime}" />.</div>
        </c:if>

        <!-- Detailed Table -->
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

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
        <h1 class="my-4">Event Details</h1>
        
        <!-- Navigation -->
        <ul class="nav nav-tabs mb-4">
            <li class="nav-item"><a class="nav-link" href="summary">Summary</a></li>
            <li class="nav-item"><a class="nav-link active" href="detail">Details</a></li>
            <li class="nav-item"><a class="nav-link" href="transaction">Transactions</a></li>
        </ul>

        <!-- Filter Form -->
        <form class="mb-4">
            <div class="row">
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
                <div class="col-md-3">
                    <button type="submit" class="btn btn-primary">Filter</button>
                </div>
            </div>
        </form>

        <!-- Detailed Table -->
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
                        <td>${event.timestamp}</td>
                        <td>${event.component}</td>
                        <td>${event.namespace}</td>
                        <td>${event.event}</td>
                        <td class="${event.outcome == 'fail' ? 'text-danger' : 'text-success'}">${event.outcome}</td>
                        <td>${event.message}</td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
    </div>
</body>
</html>

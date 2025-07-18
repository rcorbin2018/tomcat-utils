<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Transaction Report</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/css/styles.css" rel="stylesheet">
</head>
<body>
    <div class="container">
        <h1 class="my-4">Transaction Report</h1>
        
        <!-- Navigation -->
        <ul class="nav nav-tabs mb-4">
            <li class="nav-item"><a class="nav-link" href="summary">Summary</a></li>
            <li class="nav-item"><a class="nav-link" href="detail">Details</a></li>
            <li class="nav-item"><a class="nav-link active" href="transaction">Transactions</a></li>
        </ul>

        <!-- Transaction Tables -->
        <c:forEach var="entry" items="${transactions}">
            <h3>Transaction ID: ${entry.key}</h3>
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
                    <c:forEach var="event" items="${entry.value}">
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
        </c:forEach>
    </div>
</body>
</html>

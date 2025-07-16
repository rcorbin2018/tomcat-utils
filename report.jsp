<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
<head>
    <title>MongoDB Report</title>
    <style>
        table {
            border-collapse: collapse;
            width: 100%;
        }
        th, td {
            border: 1px solid black;
            padding: 8px;
            text-align: left;
        }
        th {
            background-color: #f2f2f2;
        }
    </style>
</head>
<body>
    <h2>MongoDB Report by Day, Namespace, and Component</h2>
    <table>
        <tr>
            <th>Day</th>
            <th>Namespace</th>
            <th>Component</th>
            <th>Count</th>
        </tr>
        <c:forEach var="result" items="${results}">
            <tr>
                <td>${result.day}</td>
                <td>${result.namespace}</td>
                <td>${result.component}</td>
                <td>${result.count}</td>
            </tr>
        </c:forEach>
    </table>
</body>
</html>

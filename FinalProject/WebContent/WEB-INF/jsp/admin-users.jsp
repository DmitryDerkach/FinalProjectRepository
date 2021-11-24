<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
</head>
<body>
<form action="/admin-users" method="post">
    <c:forEach var="users" items="${requestScope.users}">
        <fieldset>
            <h3>${users.id} ${users.name} ${users.email} ${users.role}</h3>
            <input type="hidden" name="id" value="${users.id}"/>
            <input type="hidden" name="name" value="${users.name}"/>
            <input type="hidden" name="email" value="${users.email}"/>
            <input type="hidden" name="role" value="${users.role}"/>
        </fieldset>
    </c:forEach>
</form>

</body>
</html>
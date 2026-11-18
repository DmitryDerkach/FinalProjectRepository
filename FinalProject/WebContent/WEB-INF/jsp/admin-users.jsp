<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%-- Описание (2026): список пользователей для администратора (AdminUserServlet кладёт его в атрибут запроса
     "users"). Форма со скрытыми полями ничего не отправляет: кнопки в ней нет, а POST /admin-users не обработан. --%>
<html>
<head>
    <title>Users list:</title>
</head>
<body>
<%@ include file="header.jsp"%>
<h3><fmt:message key="users.db.list" /></h3>
<form action="${pageContext.request.contextPath}/admin-users" method="post">
    <c:forEach var="user" items="${requestScope.users}">
        <h3>
            <%-- fix 2026: c:out / fn:escapeXml - имя с тегами, введённое при регистрации, выполнялось у администратора (XSS) --%>
            Id:${user.id}, <c:out value="${user.name}"/> - [<c:out value="${user.email}"/>] - ${user.role}
            <input type="hidden" name="id" value="${user.id}"/>
            <input type="hidden" name="name" value="${fn:escapeXml(user.name)}"/>
            <input type="hidden" name="email" value="${fn:escapeXml(user.email)}"/>
            <input type="hidden" name="role" value="${user.role}"/>
        </h3>
    </c:forEach>
</form>
</body>
</html>
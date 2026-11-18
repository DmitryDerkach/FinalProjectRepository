<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%-- Описание (2026): форма входа. Её показывает LoginServlet (GET /login), отправляет - POST /login.
     ?error - вход не удался, ?registered - только что зарегистрировались. --%>
<html>
<head>
    <title>Login</title>
</head>
<body>
<%@ include file="header.jsp"%>
<br>
<br>

<%-- 2026: подсказка для посетителей демо (аккаунты из db/02_data.sql) и сообщение после регистрации --%>
<p><fmt:message key="message.demo" /></p>
<c:if test="${param.registered != null}">
    <div style="color: green">
        <span><fmt:message key="message.registered" /></span>
    </div>
</c:if>

<form action="${pageContext.request.contextPath}/login" method="post">
    <label for="emailId"><fmt:message key="user.email" />
        <%-- 2026: после неудачного входа email остаётся в поле (fn:escapeXml - без него кавычка в адресе
             страницы ломала бы HTML) --%>
        <input type="text" name="eMail" id="emailId" value="${fn:escapeXml(param.email)}">
    </label>
    <br>
    <br>
    <label for="passwordId"><fmt:message key="user.password" />
        <input type="password" name="password" id="passwordId">
    </label>
    <br>
    <br>
    <button type="reset"><fmt:message key="button.reset" /></button>
    <button type="submit"><fmt:message key="button.submit" /></button>
    <br>
    <c:if test="${param.error !=null}">
        <div style="color: red">
            <span><fmt:message key="error" /></span>
        </div>
    </c:if>
</form>
<h3>
    <%-- fix 2026: было http://localhost:8080/registration - на другом порту ссылка вела в чужое приложение --%>
    <a href="${pageContext.request.contextPath}/registration"><fmt:message key="message.first.time"/></a>
</h3>
</body>
</html>
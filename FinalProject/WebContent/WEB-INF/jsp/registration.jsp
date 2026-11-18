<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%-- Описание (2026): форма регистрации (GET /registration), отправляется POST /registration. Закомментированный
     блок - первая версия без перевода (подписи только на английском), ниже - она же с fmt:message. --%>
<html>
<head>
    <%-- fix 2026: заголовок вкладки был "Title" --%>
    <title>Registration</title>
</head>
<body>
<%@ include file="header.jsp"%>
<!-- <br>
<br>
<form action="/registration" method="post">
    <label for="nameId">Name:
        <input type="text" name="userName" id="nameId">
    </label>
    <br>
    <br>
    <label for="emailId">Email:
        <input type="text" name="eMail" id="emailId">
    </label>
    <br>
    <br>
    <label for="passwordId">Password:
        <input type="password" name="password" id="passwordId">
    </label>
    <br>
    <br>
    <button type="reset">Reset</button>
    <button type="submit">Submit</button>
    <br>

</form>
</body>
</html> -->

<%-- 2026: ошибки формы (ключи переводов от UserService), введённое остаётся в полях --%>
<c:if test="${not empty requestScope.errors}">
    <div style="color: red">
        <c:forEach var="error" items="${requestScope.errors}">
            <p><fmt:message key="${error}" /></p>
        </c:forEach>
    </div>
</c:if>

<form action="/registration" method="post">
    <label for="nameId"><fmt:message key="user.name" />
        <input type="text" name="userName" id="nameId" value="${fn:escapeXml(param.userName)}">
    </label>
    <br>
    <br>
    <label for="emailId"><fmt:message key="user.email" />
        <input type="text" name="eMail" id="emailId" value="${fn:escapeXml(param.eMail)}">
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
</form>
</body>
</html>
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<html>
<head>
    <title>Login</title>
</head>
<body>
<%@ include file="header.jsp"%>
<br>
<br>


<form action="${pageContext.request.contextPath}/login" method="post">
    <label for="emailId"><fmt:message key="user.email" />
        <input type="text" name="eMail" id="emailId">
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
    <a href="http://localhost:8080/registration"><fmt:message key="message.first.time"/></a>
</h3>
</body>
</html>
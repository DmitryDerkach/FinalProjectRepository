<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>Login</title>
</head>
<body>
<%@ include file="header.jsp"%>
<form action="${pageContext.request.contextPath}/login" method="post">
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
    <c:if test="${param.error !=null}">
        <div style="color: red">
            <span>Email or password is not correct.</span>
        </div>
    </c:if>
</form>
</body>
</html>
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>Title</title>
</head>
<body>
<%@ include file="header.jsp"%>
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
<%--     <%@ include file="footer.jsp"%> --%>
</form>
</body>
</html>
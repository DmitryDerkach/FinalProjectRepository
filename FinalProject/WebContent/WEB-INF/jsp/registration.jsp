<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<html>
<head>
    <title>Title</title>
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

<form action="/registration" method="post">
    <label for="nameId"><fmt:message key="user.name" />
        <input type="text" name="userName" id="nameId">
    </label>
    <br>
    <br>
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
</form>
</body>
</html>
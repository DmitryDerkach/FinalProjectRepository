<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>Cart</title>
</head>
<body>
<form action="${pageContext.request.contextPath}/cart-servlet" method="post">
    <c:forEach var="medicine" items="${sessionScope.medicines}">
        <p>${medicine.name} ${medicine.price} ${medicine.countryOfProduction} 
            <input type="number" name="quantity" placeholder="количество" value="1">
            <input type="hidden" name="id"  value=${medicine.id}> </p>
    </c:forEach>
    <button type="submit">Подтвердить покупку</button>
</form>
</body>
</html>
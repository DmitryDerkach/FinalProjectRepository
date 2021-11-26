<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>Cart</title>
</head>
<body>
<%@ include file="header.jsp"%>
<form action="${pageContext.request.contextPath}/final-servlet" method="post">
    <c:forEach var="index" begin="0" end="${sessionScope.medicines.size()-1}">
     <h3><label>
            ${sessionScope.medicines.get(index).name}
                (${sessionScope.medicines.get(index).countryOfProduction}) -
                    ${sessionScope.medicines.get(index).price} USD:
                ${sessionScope.quantity[index]} items
                <br>
                Total price for this medicine: ${sessionScope.totalPriceEachMedicine.get(index)} USD
    </label></h3>
    </c:forEach>
    	<h3>
        Total price: ${sessionScope.totalPrice}
        </h3>
    <button type="submit" name="submitReject" value="1">Отменить покупку</button>
    <button type="submit" name="submitReject" value="2">Подтвердить покупку</button>
</form>
</body>
</html>
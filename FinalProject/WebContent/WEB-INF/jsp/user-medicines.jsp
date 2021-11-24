<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>User medicines list</title>
</head>
<body>
<form action="${pageContext.request.contextPath}/user-medicines" method="post">
    <c:forEach var="medicine" items="${sessionScope.medicines}">
        <fieldset>
            <h3><label>
                <input type="checkbox" name="medicineId" value="${medicine.id}"/>
                    ${medicine.name} ${medicine.countryOfProduction} ${medicine.price} USD<br>
                Количество лекарства к покупке:<input type="number" name="quantity" value="0"/>
            </label></h3>
        </fieldset>
    </c:forEach>
    <button type="submit">Добавить лекарства в корзину</button>
</form>
</body>
</html>
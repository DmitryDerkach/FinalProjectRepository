<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%-- Описание (2026): витрина покупателя (UserMedicineServlet): сообщения о прошлой покупке и об ошибке
     количества (одноразовые: sessionScope.remove убирает их из сессии сразу после показа), затем список лекарств
     с галочкой и количеством - форма уходит в корзину (/user-cart). Закомментированный блок - первая версия
     с русскими подписями, до перевода на fmt:message. --%>
<html>
<head>
    <title>User medicines list</title>
</head>
<body>
<%@ include file="header.jsp"%>
<%-- <c:if test="${not empty sessionScope.resultRejected}">
	<div style ="color: red">
		<span> Покупка была отменена!</span>
	</div>
</c:if>

<c:if test="${not empty sessionScope.resultFine}">
	<div style ="color: green;">
		<span> Покупка была совершена!</span>
	</div>
</c:if>

<form action="${pageContext.request.contextPath}/user-medicines" method="post">
    <c:forEach var="medicine" items="${sessionScope.medicines}">
            <h3><label>
                <input type="checkbox" name="medicineId" value="${medicine.id}"/>
                    ${medicine.name} (${medicine.countryOfProduction}) - ${medicine.price} USD<br>
                Количество лекарства к покупке:<input type="number" name="quantity" value="0"/>
            </label></h3>
    </c:forEach>
    <button type="submit">Добавить лекарства в корзину</button>
</form>
</body>
</html> --%>

<c:if test="${not empty sessionScope.request && sessionScope.request.equals('Покупка была отменена!')}">
    <div style ="color: red">
        <p><fmt:message key="error.purchase"/> </p>
        ${sessionScope.remove('request')}
    </div>
</c:if>

<c:if test="${not empty sessionScope.request && sessionScope.request.equals('Покупка была совершена!')}">
    <div style ="color: blue">
        <p><fmt:message key="message.purchase"/></p>
        ${sessionScope.remove('request')}
    </div>
</c:if>

<c:if test="${not empty sessionScope.quantityError}">
    <div style ="color: red">
        <p><fmt:message key="error.quantity" /></p>
        ${sessionScope.remove('quantityError')}
    </div>
</c:if>

<form action="${pageContext.request.contextPath}/user-cart" method="post">
    <c:forEach var="medicine" items="${sessionScope.medicines}">
        <h3>
            <label>
                <input type="checkbox" name="medicineId" value="${medicine.id}"/>
                    <%-- fix 2026: c:out - название с тегами выполнялось в браузере (XSS) --%>
                    <c:out value="${medicine.name}"/> (<c:out value="${medicine.countryOfProduction}"/>) - ${medicine.price}$ <br>
                <%-- fix 2026: rowId - id лекарства этой строки, чтобы корзина брала количество из той же строки,
                     что и галочку (раньше - по порядку среди ненулевых полей); min="0" - без отрицательных чисел --%>
                <input type="hidden" name="rowId" value="${medicine.id}"/>
                <fmt:message key="medicine.quantity" /><input type="number" name="quantity" value="0" min="0"/>
            </label>
        </h3>
    </c:forEach>
    <button type="submit"><fmt:message key="button.medicines.add.to.cart" /></button>
</form>

</body>
</html>
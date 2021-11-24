<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>Admin medicines</title>
</head>
<body>

<%-- <c:if test="${requestScope.result !=null}">
    <div style="color: blue">
        <span>${requestScope.result}</span>
    </div>
</c:if> --%>

<form action="/admin-medicines" method="post">
	<fieldset>
		<label id="add"><h3>Форма добавляения лекарства в БД:</h3>
		    <input type="text" placeholder="Insert name here" name="name"/>
		    <input type="text" placeholder="Insert country here" name="countryOfProduction"/>
		    <input type="text" placeholder="Insert price here" name="price"/>
		    <input type="hidden" name="medicineAddition" value="medicineAddition"/>
		    <button type="submit">Добавить</button>
	    </label>
    </fieldset>
</form>

<form action="/admin-medicines" method="post">
    <c:forEach var="medicine" items="${sessionScope.medicines}">
        <fieldset>
            <h3>${medicine.name} ${medicine.countryOfProduction} ${medicine.price}$ <button type="submit">Удалить</button></h3>
            <input type="hidden" name="id" value="${medicine.id}"/>
            <input type="hidden" name="name" value="${medicine.name}"/>
            <input type="hidden" name="country" value="${medicine.countryOfProduction}"/>
            <input type="hidden" name="price" value="${medicine.price}"/>
        </fieldset>
    </c:forEach>
</form>
</body>
</html>
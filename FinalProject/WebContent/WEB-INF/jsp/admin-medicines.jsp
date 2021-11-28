<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<html>
<head>
    <title>Admin medicines</title>
</head>
<body>
<%@ include file="header.jsp"%>

<%-- 
<c:if test="${not empty sessionScope.result}">
	<div style ="color: red"> 
		<span>${sessionScope.result}</span>
	</div>
</c:if>
<form action="/admin-medicines" method="post">
		<label id="add"><h3>Форма добавляения лекарства в БД:</h3>
		    <input type="text" placeholder="Insert name here" name="name"/>
		    <input type="text" placeholder="Insert country here" name="countryOfProduction"/>
		    <input type="number" placeholder="Insert price here" name="price"/>
		    <input type="hidden" name="medicineAddition" value="medicineAddition"/>
		    <button type="submit">Добавить</button>
	    </label>
</form>
<br>

<h3>Список лекарств из базы данных:</h3>
<ol>
    <c:forEach var="medicine" items="${sessionScope.medicines}">
        <form action="${pageContext.request.contextPath}/admin-medicines" method="post">
            <input type="hidden" name="id" required="required" value="${medicine.id}"/>
            <input type="hidden" name="name" required="required" value="${medicine.name}"/>
            <input type="hidden" name="country" required="required" value="${medicine.countryOfProduction}"/>
            <input type="hidden" name="price" value="${medicine.price}"/>
            <li>
                <h3>${medicine.name} (${medicine.countryOfProduction}) - ${medicine.price}$
                    <button type="submit">Удалить</button>
                </h3>
            </li>
        </form>
    </c:forEach>
</ol>
</body>
</html> --%>

<c:if test="${not empty sessionScope.result && (sessionScope.result eq ('Saved') || sessionScope.result eq ('Not saved'))}">
    <div style ="color: blue">
        <p>${sessionScope.result}</p>
    </div>
</c:if>

<c:if test="${not empty sessionScope.result && (sessionScope.result eq ('Succsessfuly deleted') || sessionScope.result eq ('Was not deleted'))}">
    <div style ="color: red">
        <p>${sessionScope.result}</p>
    </div>
</c:if>

<form action="${pageContext.request.contextPath}/admin-medicines" method="post">
    <h3>
        <label id="add"><fmt:message key="button.medicine.add" /><br>
            <input type="text" placeholder="<fmt:message key="medicine.name"/>" required name="name"/>
            <input type="text" placeholder="<fmt:message key="medicine.country"/>" required name="countryOfProduction"/>
            <input type="number" placeholder="<fmt:message key="medicine.price"/>" required name="price"/>
            <input type="hidden" name="medicineAddition" value="medicineAddition"/>
            <button type="submit"><fmt:message key="button.add" /></button>
        </label>
    </h3>
</form>

<h3><fmt:message key="medicines.db.list" /></h3>
<ol>
    <c:forEach var="medicine" items="${sessionScope.medicines}">
        <li>
            <form action="${pageContext.request.contextPath}/admin-medicines" method="post">
                <input type="hidden" name="id" value="${medicine.id}"/>
                <input type="hidden" name="name" value="${medicine.name}"/>
                <input type="hidden" name="country" value="${medicine.countryOfProduction}"/>
                <input type="hidden" name="price" value="${medicine.price}"/>
                <h3>${medicine.name} (${medicine.countryOfProduction}) - ${medicine.price}$
                    <button type="submit"><fmt:message key="button.delete" /></button>
                </h3>
            </form>
        </li>
    </c:forEach>
</ol>
<br>
</body>
</html>
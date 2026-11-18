<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%-- Описание (2026): склад лекарств администратора (AdminMedicineServlet): результат последнего действия, форма
     добавления и список с кнопкой "Удалить" у каждого лекарства. Закомментированный блок - первая версия
     с русскими подписями, до перевода на fmt:message. --%>
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
		<label id="add"><h3>Форма добавления лекарства в БД:</h3>
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

<%-- fix 2026: сообщения были строками сервиса на английском при любом языке страницы - теперь переводятся
     (опечатка "Succsessfuly deleted" исправлена в MedicineService); "Invalid medicine" - ошибка формы 2026 --%>
<c:if test="${not empty sessionScope.result && (sessionScope.result eq ('Saved') || sessionScope.result eq ('Not saved'))}">
    <div style ="color: blue">
        <p><fmt:message key="${sessionScope.result eq 'Saved' ? 'message.medicine.saved' : 'message.medicine.not.saved'}" /></p>
    </div>
</c:if>

<c:if test="${not empty sessionScope.result && (sessionScope.result eq ('Successfully deleted') || sessionScope.result eq ('Was not deleted'))}">
    <div style ="color: red">
        <p><fmt:message key="${sessionScope.result eq 'Successfully deleted' ? 'message.medicine.deleted' : 'message.medicine.not.deleted'}" /></p>
    </div>
</c:if>

<c:if test="${sessionScope.result eq 'Invalid medicine'}">
    <div style ="color: red">
        <p><fmt:message key="error.medicine" /></p>
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
                <%-- fix 2026: fn:escapeXml / c:out - название с тегами (<script>...) выполнялось в браузере (XSS) --%>
                <input type="hidden" name="id" value="${medicine.id}"/>
                <input type="hidden" name="name" value="${fn:escapeXml(medicine.name)}"/>
                <input type="hidden" name="country" value="${fn:escapeXml(medicine.countryOfProduction)}"/>
                <input type="hidden" name="price" value="${medicine.price}"/>
                <h3><c:out value="${medicine.name}"/> (<c:out value="${medicine.countryOfProduction}"/>) - ${medicine.price}$
                    <button type="submit"><fmt:message key="button.delete" /></button>
                </h3>
            </form>
        </li>
    </c:forEach>
</ol>
<br>
</body>
</html>
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<fmt:setLocale value="${sessionScope.lang != null ? sessionScope.lang : (param.lang !=null ? param.lang : 'en_US')}"/>
<fmt:setBundle basename="translations"/>

<div>
	<c:if test="${not empty sessionScope.user}">
	<form action="${pageContext.request.contextPath}/logout" method="post">
		<button type="submit"><fmt:message key="pharmacy.logout"/></button>
	</form>
	</c:if>
</div>

<div>
    <form action="${pageContext.request.contextPath}/locale" method="post">
        <button type="submit" name="lang" value="ru_RU">Русский</button>
        <button type="submit" name="lang" value="en_EN">English</button>
    </form>
</div>


<h3><fmt:message key="author.team"/>
    <a href="https://github.com/DmitryDerkach/FinalProjectRepository"><fmt:message key="derkach" /></a>,
    <a href="https://github.com/DmitryPoverov/Pharmacy"><fmt:message key="poverov" /></a>
    <br>
    <img alt="" height="220" src="https://pbs.twimg.com/media/DojsED4VsAAdoiz.jpg">
    <br>
</h3>
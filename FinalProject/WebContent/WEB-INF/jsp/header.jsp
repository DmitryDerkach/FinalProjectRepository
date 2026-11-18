<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%-- Описание (2026): шапка всех страниц (<%@ include file="header.jsp"%>): язык страницы (fmt:setLocale из сессии,
     которую заполняет LocaleServlet) и файл переводов (fmt:setBundle -> translations*.properties), кнопка
     выхода для вошедшего пользователя, кнопки языка, авторы и логотип. --%>

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
        <%-- fix 2026: было en_EN - такого языка нет; en_US --%>
        <button type="submit" name="lang" value="en_US">English</button>
    </form>
</div>


<h3><fmt:message key="author.team"/>
    <a href="https://github.com/DmitryDerkach/FinalProjectRepository"><fmt:message key="derkach" /></a>,
    <%-- fix 2026: ссылка вела на репозиторий DmitryPoverov/Pharmacy, которого больше нет (404) - теперь на профиль --%>
    <a href="https://github.com/DmitryPoverov"><fmt:message key="poverov" /></a>
    <br>
    <%-- fix 2026: была чужая картинка (логотип другой компании), загружаемая с pbs.twimg.com - теперь свой
         логотип из этого репозитория (WebContent/images/logo.svg, открыт для всех в AuthorizationFilter) --%>
    <img alt="Pharmacy" height="220" src="${pageContext.request.contextPath}/images/logo.svg">
    <br>
</h3>
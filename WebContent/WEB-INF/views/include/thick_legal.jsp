<%@ page language="java" pageEncoding="UTF-8"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<fmt:setLocale value="${sessionScope.language}"/>
<fmt:setBundle basename="org.ulpmm.univrav.language.messages"/>

<!doctype html>
<html>
	<head>
        <meta charset="utf-8">
		<title>Legal information</title>
	</head>
	<body>
		<br>
		<p><fmt:message key="legal_information"/></p>
	</body>
</html>
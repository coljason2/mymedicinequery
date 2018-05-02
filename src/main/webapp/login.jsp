<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html lang="en">
<head>
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>藥品查詢</title>
<link rel="stylesheet"
	href="<c:url value="/resources/bootstrap.min.css"/>">
<link rel="stylesheet" href="<c:url value="/resources/style.css"/>">
</head>
<body>
	<div class="wrapper">
		<form class="form-signin" method="post"
			action="<c:url value="/signin"/>">
			<h2 class="form-signin-heading" align="center">請登入</h2>
			<input type="text" class="form-control" name="username"
				placeholder="輸入帳號" required="" autofocus="" /> <input
				type="password" class="form-control" name="password"
				placeholder="輸入密碼" required="" /> <input type="hidden"
				name="${_csrf.parameterName}" value="${_csrf.token}" />
			<button class="btn btn-lg btn-primary btn-block" type="submit">登入</button>
			<br />
			<c:if test="${not empty sessionScope.message}">
				<span style="color: red"><c:out
						value="${sessionScope.message}" /></span>
				<c:remove var="message" scope="session" />
				<br>
			</c:if>
			<span style="color: green">測試帳號密碼：test</span>
		</form>
	</div>
</body>
</html>
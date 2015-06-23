<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page import="java.util.List,medquery.comman.MedEntity"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<jsp:include page="/layout/meta.jsp" />
<title>查詢結果</title>
</head>
<body>
	<jsp:include page="/layout/nav.jsp" />
	<div class="container">
		<div class="jumbotron">${test}</div>
	</div>
	REAL PATH:
	<%
		application.getRealPath("/").toString();
	%>
	<jsp:include page="/layout/foot.jsp" />
</body>
</html>
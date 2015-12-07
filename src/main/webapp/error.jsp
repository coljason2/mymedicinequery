<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<jsp:include page="/layout/meta.jsp" />
<title>頁面錯誤</title>
</head>
<body>
	<%@ page isErrorPage="true"%>
	<jsp:include page="/layout/nav.jsp" />
	<div class="container">
		<div class="jumbotron">
			<h2>網頁錯誤@@!請重新查詢</h2>
			<p>
				<%
					exception.printStackTrace(new java.io.PrintWriter(out));
				%>
			<p>
		</div>
	</div>
	<jsp:include page="/layout/foot.jsp" />
</body>
</html>
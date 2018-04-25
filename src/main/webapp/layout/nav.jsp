<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<nav class="navbar navbar-inverse navbar-fixed-top">
	<div class="container">
		<div class="navbar-header">
			<a class="navbar-brand" href="<c:url value="/" />">藥品查詢</a>
			<form class="navbar-form navbar-left" action="query" method="POST">
				<div class="input-group">
					<input type="text" name="querystring" placeholder="輸入要查詢藥名"
						class="form-control"> <span class="input-group-btn">
						<button id="submit" type="submit" class="btn btn-success">查詢</button>
					</span> <input type="hidden" name="${_csrf.parameterName}"
						value="${_csrf.token}" />
				</div>
			</form>

			<c:url value="/logout" var="logoutUrl" />
			<form class="navbar-form navbar-right" id="logout"
				action="${logoutUrl}" method="post">
				<input type="hidden" name="${_csrf.parameterName}"
					value="${_csrf.token}" />
				<button id="submit" type="submit" class="btn btn-info">登出</button>
			</form>
		</div>
	</div>
</nav>
<script>
	// 	$(document).ready(function() {
	// 		$('#submit').on('click', function() {
	// 			var med = $("input[name='querystring']").val()
	// 			var ctx = "<c:url value="/" />" + "query/";
	// 			window.location = ctx + med;
	// 		});
	// 	});
</script>
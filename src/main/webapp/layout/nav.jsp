<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<nav class="navbar navbar-inverse navbar-fixed-top">
	<div class="container">
		<div class="navbar-header">
			<a class="navbar-brand" href="<%=request.getContextPath()%>/">藥品查詢</a>
			<form class="navbar-form navbar-left" action="query" method="get">
				<div class="input-group">
					<input type="text" name="querystring" placeholder="輸入要查詢藥名"
						class="form-control"> <span class="input-group-btn">
						<button type="submit" class="btn btn-success">查詢</button>
					</span>
				</div>
			</form>
		</div>
	</div>
</nav>
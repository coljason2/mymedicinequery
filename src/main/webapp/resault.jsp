<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page import="java.util.List,medquery.comman.MedEntity"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<jsp:include page="/layout/meta.jsp" />
<script type="text/javascript"
	src="https://cdn.datatables.net/1.10.16/js/jquery.dataTables.min.js"></script>
<script type="text/javascript"
	src="https://cdn.datatables.net/buttons/1.5.1/js/dataTables.buttons.min.js"></script>
<script type="text/javascript"
	src="https://cdnjs.cloudflare.com/ajax/libs/jszip/3.1.3/jszip.min.js"></script>
<script type="text/javascript"
	src="https://cdnjs.cloudflare.com/ajax/libs/pdfmake/0.1.32/pdfmake.min.js"></script>
<script type="text/javascript"
	src="https://cdnjs.cloudflare.com/ajax/libs/pdfmake/0.1.32/vfs_fonts.js"></script>
<script type="text/javascript"
	src="https://cdn.datatables.net/buttons/1.5.1/js/buttons.html5.min.js"></script>
<link rel="stylesheet" type="text/css"
	href="https://cdn.datatables.net/1.10.16/css/jquery.dataTables.css">
<script
	src="https://cdn.datatables.net/buttons/1.4.2/js/buttons.print.min.js"></script>
<title>【${querystring}】查詢結果</title>
</head>
<jsp:include page="/layout/nav.jsp" />
<body>
	<div class="container">
		<table id="exportdata" class="table table-striped" width="100%"
			cellspacing="0">
			<thead>
				<tr>
					<td>藥名</td>
					<td>藥價/庫存</td>
					<td>健保碼</td>
					<td>健保價</td>
				</tr>
			</thead>
			<tbody>
				<c:forEach items="${meds}" var="item">
					<tr>
						<td>${item.name}</td>
						<td>${item.isenough}</td>
						<td><c:choose>
								<c:when test="${item.oid == '無健保給付'}">
								${item.oid}
							</c:when>
								<c:otherwise>
									<a
										href="https://www.nhi.gov.tw/query/query1_list.aspx?Q1ID=${item.oid}"
										target="_blank">${item.oid}</a>
								</c:otherwise>
							</c:choose></td>
						<td>${item.oidprice}</td>
					</tr>
				</c:forEach>
			</tbody>
		</table>
	</div>
</body>
<script>
	$(document).ready(function() {
		$('#exportdata').dataTable({
			"bLengthChange" : true,
			"scrollCollapse" : true,
			"jQueryUI" : true,
			dom : 'Bfrtip',
			buttons : [ 'copyHtml5', 'excelHtml5', 'csvHtml5', 'print' ]
		});
		$('#exportdata_wrapper').find('a').trigger('click');
	});
</script>
<jsp:include page="/layout/foot.jsp" />
</html>
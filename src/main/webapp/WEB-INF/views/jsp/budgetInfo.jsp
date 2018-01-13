<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ page import="java.math.BigDecimal"%>
<%@ page import="com.financetracker.model.PaymentType" %>
<%@ taglib prefix = "c" uri = "http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri = "http://java.sun.com/jsp/jstl/functions" prefix = "fn" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
		<title>Budget Info | Finance Tracker</title>
		<link href="<c:url value="/img/favicon.ico" />" rel="icon" type="image/x-icon">
	</head>
	<body>
		<c:if test="${ sessionScope.user == null }">
			<c:redirect url="login.jsp"></c:redirect>
		</c:if>
	
		<div>
			<jsp:include page="left.jsp"></jsp:include>
		</div>
		<div>
			<jsp:include page="header.jsp"></jsp:include>
		</div>
		
		<div class="content-wrapper">
			<section class="content">
				<div style="margin-bottom: 25px">
					<div class="row">
						<div class="col-sm-3">
							<a href="<c:url value='/budgets/${ budgetId }/editBudget'></c:url>" type="button" class="btn btn-block btn-primary btn-lg"><i class="fa fa-edit"></i> Edit budget</a>
						</div>
						<div class="col-sm-3">
							<%--<a href="<c:url value='/budgets'></c:url>" type="button" class="btn btn-block btn-default btn-lg"><i class="ion ion-android-arrow-back"></i> Back</a>--%>
							<a href="/budgets" type="button" class="btn btn-block btn-default btn-lg"><i class="ion ion-android-arrow-back"></i> Back</a>
						</div>
						<form action="/budgets/${ budgetId }/delete" method="post" id="deleteForm">
							<div class="col-sm-3">
								<input id="submitBtn" type="button" name="btn" data-toggle="modal" data-target="#confirm-submit" class="btn btn-block btn-danger btn-lg" value="Delete Budget">
							</div>
						</form>
					</div>
				</div>
				
				<c:forEach items="${ budgetTransactions }" var="transaction">
					<div>
						<div class="info-box" style="width: auto;">
							<div class="info-box-content">
							<div class="row">
								  <div class="col-sm-4">
									  <h4>Description: <c:out value="${ transaction.description }"></c:out></h4>
								  </div>
								  <div class="col-sm-4">
									  <h4>Date: <fmt:parseDate value="${ transaction.date }" pattern="yyyy-MM-dd" /></h4>
								  </div>
							  </div>
							 <div class="row">
								<div class="col-sm-4">
									 <c:choose>
									  <c:when test="${ transaction.type eq 'INCOME' }">
										<h3 style="color: green;">Amount: + <i class="ion-social-usd" style="font-size: 20px;"> </i><fmt:formatNumber value="${ transaction.amount }" minFractionDigits="2"/></h3>
									  </c:when>
									  <c:when test="${ transaction.type eq 'EXPENSE'}">
										<h3 style="color: red;">Amount: - <i class="ion-social-usd" style="font-size: 20px;"> </i><fmt:formatNumber value="${ transaction.amount }" minFractionDigits="2"/></h3>
									  </c:when>
									  <c:otherwise>
										 <h3>Amount: <i class="ion-social-usd" style="font-size: 20px;"> </i><fmt:formatNumber value="${ transaction.amount }" minFractionDigits="2"/></h3>
									  </c:otherwise>
									  </c:choose>
								</div>
								  <div class="col-sm-4">
									 <h3>Category: <c:out value="${ transaction.categoryName }"></c:out></h3>
								 </div>
							  </div>
							</div>
						</div>
					</div>
				</c:forEach>
			</section>
		</div>

		<div class="modal fade in" id="confirm-submit" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
			<div class="modal-dialog">
				<div class="modal-content">
					<div class="modal-header">
						<button type="button" class="close" data-dismiss="modal" aria-label="Close">
							<span aria-hidden="true">×</span>
						</button>
						<h3 class="modal-title">Confirm delete</h3>
					</div>
					<div class="modal-body">
						<h4>Are you sure you want to delete this budget and all the data it contains?</h4>
					</div>

					<div class="modal-footer">
						<button type="button" class="btn btn-default" data-dismiss="modal">Back</button>
						<a href="#" id="submit" class="btn btn-danger danger">Delete</a>
					</div>
				</div>
			</div>
		</div>

		<script>
            $('#submit').click(function(){
                /* when the submit button in the modal is clicked, submit the form */
                $('#deleteForm').submit();
            });
		</script>
	</body>
</html>
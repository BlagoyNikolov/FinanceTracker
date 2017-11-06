<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ page import="java.math.BigDecimal"%>
<%@ page import="com.financeTracker.model.TransactionType" %>
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
							<a href="<c:url value='/budgets/${ budgetId }/editBudget'></c:url>" type="button" class="btn btn-block btn-success btn-lg"><i class="fa fa-edit"></i> Edit budget</a>
						</div>
						<div class="col-sm-3">
							<a href="<c:url value='/budgets'></c:url>" type="button" class="btn btn-block btn-default btn-lg"><i class="ion ion-android-arrow-back"></i> Back</a>
						</div>
						<div class="col-sm-3">
							<a href="<c:url value='/budgets/${ budgetId }/delete'></c:url>" type="button" class="btn btn-block btn-danger btn-lg"><i class="ion ion-android-delete"></i> Delete Budget</a>
						</div>
					</div>
				</div>
				
				<c:forEach items="${ budgetTransactions }" var="transaction">
				<div>
			<%-- 	<a href="transaction/${transaction.transactionId}"> --%>
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
						              <c:when test="${ transaction.type eq 'EXPENCE'}">
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
		            </a>
		        </div> 
			</c:forEach>
				
				
			</section>
				
				
		</div>
	</body>
</html>
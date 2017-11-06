<%@page import="java.math.BigDecimal"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ page import="com.financeTracker.model.User"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
		<title>About | Finance Tracker</title>
		<link href="<c:url value="/img/favicon.ico" />" rel="icon" type="image/x-icon">
	</head>
	<body>
		<div>
			<jsp:include page="left.jsp"></jsp:include>
		</div>
		<div>
			<jsp:include page="header.jsp"></jsp:include>
		</div>
		<div class="content-wrapper">
			<section class="content-header"> 
				<span style="font-size: 35px"><b>Finance</b>Tracker</span> </section>
			<section class="content">
				<div class="box">
					<div class="box-header with-border">
						<h3 class="box-title" style="font-size: 22px">About the project</h3>
					</div>
					<div class="box-body">
						<div>
							<p style="font-size: 20px">
								The <b>Finance</b>Tracker is a web based Java Application which
								helps you manage your bank accounts. It keeps track of all your cash 
								inflow and outflow and collects information about your financial wealth. 
								The app also provides tools that would help you build yourself a financial plan. 
								All of the information is presented via cutting edge technologies in software and design.
							<p>
						</div>
						<div class="row">
							<div class="box-header with-border">
								<h3 class="box-title" style="font-size: 22px">Technologies used</h3>
							</div>
						    <div class="col-sm-4" style="display:table-cell; vertical-align:middle; text-align:center">
						    	<img src="http://www.canoo.com/wp-content/uploads/2017/03/java_ee_logo.png" alt="JEE" width="180" height="auto" align="middle">   
						    </div>
						    <div class="col-sm-4" style="display:table-cell; vertical-align:middle; text-align:center">
						    	<img src="http://www.pngall.com/wp-content/uploads/2016/05/MySQL-Logo.png" alt="MySql" width="300" height="auto">  
						    </div>
						    <div class="col-sm-4" style="display:table-cell; vertical-align:middle; text-align:center">
						    	<img src="https://www.eclipse.org/artwork/images/v2/logo-800x188.png" alt="Eclipse" width="300" height="auto" align="">  
						    </div>
						</div>
						<div class="row">
						    <div class="col-sm-4" style="display:table-cell; vertical-align:middle; text-align:center">
						    	<img src="https://assets-cdn.github.com/images/modules/logos_page/Octocat.png" alt="GitHub" width="180" height="auto">   
						    </div>
						    <div class="col-sm-4" style="display:table-cell; vertical-align:middle; text-align:center">
						    	<img src="https://maven.apache.org/images/maven-logo-black-on-white.png" alt="Maven" width="300" height="auto">  
						    </div>
						    <div class="col-sm-4" style="display:table-cell; vertical-align:middle; text-align:center">
						    	<img src="https://spring.io/img/spring-by-pivotal.png" alt="Spring" width="300" height="auto" align="">  
						    </div>
						</div>
						<br>
						<div class="row">
						    <div class="col-sm-4" style="display:table-cell; vertical-align:middle; text-align:center">
						    	<img src="https://upload.wikimedia.org/wikipedia/commons/thumb/a/a1/AJAX_logo_by_gengns.svg/2000px-AJAX_logo_by_gengns.svg.png" alt="AJAX" width="180" height="auto">   
						    </div>
						    <div class="col-sm-4" style="display:table-cell; vertical-align:middle; text-align:center">
						    	<img src="https://www.shareicon.net/data/512x512/2015/10/24/661304_interface_512x512.png" alt="JSP" width="180" height="auto">   
						    </div>
						    <div class="col-sm-4" style="display:table-cell; vertical-align:middle; text-align:center">
						    	<img src="http://marketplace.intelledox.com/wp-content/uploads/edd/2017/01/Bootstrap.png" alt="Bootstrap" width="250" height="auto">   
						    </div>
						</div>
						<div class="row">
						    <div class="col-sm-4" style="display:table-cell; vertical-align:middle; text-align:center">
						    	<img src="https://upload.wikimedia.org/wikipedia/commons/thumb/b/b6/Badge_js-strict.svg/1000px-Badge_js-strict.svg.png" alt="JS" width="180" height="auto">   
						    </div>
						    <div class="col-sm-4" style="display:table-cell; vertical-align:middle; text-align:center">
						    	<img src="http://bradsknutson.com/wp-content/themes/bradsknutson/images/css3.png" alt="CSS" width="180" height="auto">   
						    </div>
						    <div class="col-sm-4" style="display:table-cell; vertical-align:middle; text-align:center">
						    	<img src="http://bradsknutson.com/wp-content/themes/bradsknutson/images/html5.png" alt="HTML" width="250" height="auto">   
						    </div>
						</div>
					</div>
					<div class="box-footer">
						<h3 class="box-title" style="font-size: 22px">Made possible by</h3>
						<div class="col-sm-4" style="display:table-cell; vertical-align:middle; text-align:center">
							<img src="http://ittalents.bg/images/logo-black.png" alt="ITT">
						</div>
						<div class="col-sm-4" style="display:table-cell; vertical-align:middle; text-align:center">
							<img src="https://media.licdn.com/mpr/mpr/shrinknp_200_200/AAEAAQAAAAAAAAUfAAAAJDhlM2JlNDI1LTljMTEtNGEyNS1iMjM3LTE3MTBmOWQ2YTY0MA.jpg" alt="Krasity" style="border-radius: 50%; box-shadow: 0px 0px 15px black;">
							<h3>IT Talents Trainer Krasimir Stoev</h3>
						</div>
					</div>
					<!-- /.box-body -->
				</div>
			</section>
		</div>
		<div>
			<jsp:include page="footer.jsp"></jsp:include>
		</div>
	
		<!-- jQuery 3 -->
		<script src="jquery.min.js"></script>
		<!-- Bootstrap 3.3.7 -->
		<script src="bootstrap.min.js"></script>
		<!-- SlimScroll -->
		<script src="jquery.slimscroll.min.js"></script>
		<!-- FastClick -->
		<script src="fastclick.js"></script>
		<!-- AdminLTE App -->
		<script src="/adminlte.min.js"></script>
		<!-- AdminLTE for demo purposes -->
		<script src="demo.js"></script>
	</body>
</html>
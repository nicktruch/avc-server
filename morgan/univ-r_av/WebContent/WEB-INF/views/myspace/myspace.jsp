<%@ page language="java" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="pt" uri="/WEB-INF/pagination-taglib.tld" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<fmt:setLocale value="${sessionScope.language}"/>
<fmt:setBundle basename="org.ulpmm.univrav.language.messages"/>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    
    <title><fmt:message key="Univ-R AV Audiovid&eacute;cours"/></title>

	<link rel="stylesheet" type="text/css" href="../files/thickbox/thickbox.css" media="screen">
	<link rel="stylesheet" type="text/css" href="../files/styles/${sessionScope.style}/css/styles.css">
	<link rel="stylesheet" type="text/css" href="../files/styles/${sessionScope.style}/css/recorded.css">
	<link rel="stylesheet" type="text/css" href="../files/styles/${sessionScope.style}/css/searchform.css">
	<link rel="stylesheet" type="text/css" href="../files/styles/${sessionScope.style}/css/courselist.css">
	<link rel="stylesheet" type="text/css" href="../files/styles/${sessionScope.style}/css/myspace.css">
	
	
	<!--[if IE]>
   		<link rel="stylesheet" type="text/css" href="../files/styles/${sessionScope.style}/css/styles_ie.css" media="screen" />
	<![endif]-->
	<!--[if lte IE 6]>
		<link rel="stylesheet" type="text/css" href="../files/styles/${sessionScope.style}/css/styles_ie6.css" media="screen" />
		<link rel="stylesheet" type="text/css" href="../files/styles/${sessionScope.style}/css/home_ie6.css" media="screen" />
		<script defer type="text/javascript" src="../files/js/pngfix.js"></script>
	<![endif]-->
	
	<c:forEach var="rssfile" items="${rssfiles}" begin="0" end="0">
		<link rel="alternate" type="application/rss+xml" title="${rssfile.key}" href="${rssfile.value}"/>
	</c:forEach>

	<script type="text/javascript" src="../files/js/details.js"></script>
	<script type="text/javascript" src="../files/thickbox/jquery.js"></script>
	<script type="text/javascript" src="../files/thickbox/thickbox.js"></script>
	
	<meta name="keywords" content="SMIL, cours audio, cours video, cours live, cours en direct, synchronisation de médias">
	
  </head>
  
  <body>
  
    <div class="main">
      <div class="contents">
	    
	    	 <div class="banner">
	    		<c:import url="../include/banner.jsp" />
	    	</div>
	    
	    	<div class="welcome">
	    		<p><fmt:message key="Bienvenue"/> ${user.login}</p>
	    		<p><fmt:message key="Votre e-mail"/> ${user.email}</p>
	    			    		
	    		<div class="course">
				<table cellspacing="0">
					<tr class="tableheader">
						<th colspan="5" id="courses"><fmt:message key="Mes cours"/></th>
						<th colspan="3"><fmt:message key="Visualisez"/></th>
						<th colspan="1"><fmt:message key="Editer"/></th>
					</tr>
					<c:import url="./mycourselist.jsp" />
				</table>
				
	    		<div class="pagination">
	    			<pt:PaginationTag currentPage="${page}" itemsNumber="${items}" numberPerPage="${number}" resultPageName="${resultPage}" />
				</div>
	 
	    		<a href="<c:url value="./upload" />" title="<fmt:message key="upload"/>" ><fmt:message key="upload"/></a><br>
	    	
	    	<div>
	    	
    	</div>
	    	
	    <div class="footer">
	    	<c:import url="../include/footer.jsp" />
	    </div>
    </div>
  </body>
</html>

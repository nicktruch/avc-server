<%@ page language="java" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="pt" uri="/WEB-INF/pagination-taglib.tld" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<fmt:setLocale value="${sessionScope.language}"/>
<fmt:setBundle basename="org.ulpmm.univrav.language.messages"/>

<!doctype html>
<html>
  <head>

  	<meta charset="utf-8">
  	<meta name="keywords" content="flash, cours audio, cours video, cours live, cours en direct, synchronisation de médias, audiocours, videocours">
    
    <title><fmt:message key="Univ-R AV Audiovid&eacute;cours"/> - <fmt:message key="Cours enregistr&eacute;s"/></title>

	<link rel="stylesheet" type="text/css" href="../files/thickbox/thickbox.css" media="screen">
	<link rel="stylesheet" type="text/css" href="../files/styles/${sessionScope.style}/css/styles.css">
	<link rel="stylesheet" type="text/css" href="../files/styles/${sessionScope.style}/css/rss.css">
	
	
	<!--[if IE]>
   		<link rel="stylesheet" type="text/css" href="../files/styles/${sessionScope.style}/css/styles_ie.css" media="screen" />
	<![endif]-->
	<!--[if lte IE 6]>
		<link rel="stylesheet" type="text/css" href="../files/styles/${sessionScope.style}/css/styles_ie6.css" media="screen" />
		<script defer type="text/javascript" src="../files/js/pngfix.js"></script>
	<![endif]-->
	
	<c:forEach var="rssfile" items="${rssfiles}" begin="0" end="0">
		<link rel="alternate" type="application/rss+xml" title="${rssfile.key}" href="${rssfile.value}"/>
	</c:forEach>
	
	<script type="text/javascript" src="../files/js/details.js"></script>
	<script type="text/javascript" src="../files/thickbox/jquery.js"></script>
	<script type="text/javascript" src="../files/thickbox/thickbox.js"></script>
	
	<!-- google analytics -->
	<c:import url="include/google_analytics.jsp" />
	
  </head>
  
  <body>
    <div class="main">
	    <div class="contents">
	    
	    	<div class="banner">
	    		<c:import url="include/banner.jsp" />
	     	</div>
	   
	   		<br>
	   		
	   		<div  class="aboUnivr">
	     		<c:forEach var="rssfile" varStatus="status2" items="${rssfiles}" begin="0" end="0">	     		
	     		<p> 
	     		<fmt:message key="AbonnementAudiovidecours"/>
	     		<a href="${rssfile.value}" rel="alternate" type="application/rss+xml" title="${rssfile.key}"><img src="../files/img/rss_abo.png" alt="rss_icon"></a>
				
				<c:url value="itpc://${fn:substringAfter(serverUrl,\"://\")}/${fn:substringAfter(rssfile.value,\"../\")}" var="variableURL"></c:url>
				<a href="${variableURL}" rel="alternate" type="application/rss+xml" title="${rssfile.key}"><img src="../files/img/itunes_abo.png" alt="itunes_icon"></a>
				</p>
				</c:forEach>
			</div>
	
			<br>
	     	
	     	<div class="line">
	     		<div class="rss">
	     			<p class="rssName"><fmt:message key="Abonnement"/> <fmt:message key="Auteur"/></p>
	     			<table>
	     				<tr class="tableheader">
				    		<th><fmt:message key="NomRss"/></th>
				    		<th><fmt:message key="LienRss"/></th>
				    		<th>itunes</th>
				    	</tr>
				    	
				    	<c:set var="class" value="row1" />
	     		     
	     				<c:forEach var="rssfile" varStatus="status2" items="${rssfiles}" begin="1" end="${nbrTeachersRss}">
	     				<tr class="${class}">
							<td><p>${rssfile.key}<p></td>
							<td><a href="${rssfile.value}" rel="alternate" type="application/rss+xml" title="${rssfile.key}"><img src="../files/img/rss_abo.png" alt="rss_icon"></a></td>
							
							<c:url value="itpc://${fn:substringAfter(serverUrl,\"://\")}/${fn:substringAfter(rssfile.value,\"../\")}" var="variableURL"></c:url>
							<td><a href="${variableURL}" rel="alternate" type="application/rss+xml" title="${rssfile.key}"><img src="../files/img/itunes_abo.png" alt="itunes_icon"></a></td>
						</tr>
						
						<c:choose>
				    		<c:when test="${status2.count % 2 == 0}">
								<c:set var="class" value="row1" />
							</c:when>
							<c:otherwise>
								<c:set var="class" value="row2" />
							</c:otherwise>
				    	</c:choose>
						
						</c:forEach>
					</table>
	     		</div>
	     		
	     		<div class="rss">
	     			<p class="rssName"><fmt:message key="Abonnement"/> <fmt:message key="Formation"/></p>
	     			<table>
	     				<tr class="tableheader">
				    		<th><fmt:message key="Formation"/></th>
				    		<th><fmt:message key="LienRss"/></th>
				    		<th>itunes</th>
				    	</tr>
	     		     
	     		   <c:set var="class" value="row1" />
	     		     
	     				<c:forEach var="rssfile" varStatus="status2" items="${rssfiles}" begin="${nbrTeachersRss+1}">
	     				<tr class="${class}">
							<td><p>${rssfile.key}<p></td>
							<td><a href="${rssfile.value}" rel="alternate" type="application/rss+xml" title="${rssfile.key}"><img src="../files/img/rss_abo.png" alt="rss_icon"></a></td>
							
							<c:url value="itpc://${fn:substringAfter(serverUrl,\"://\")}/${fn:substringAfter(rssfile.value,\"../\")}" var="variableURL"></c:url>
							<td><a href="${variableURL}" rel="alternate" type="application/rss+xml" title="${rssfile.key}"><img src="../files/img/itunes_abo.png" alt="itunes_icon"></a></td>
						</tr>
						
						<c:choose>
				    		<c:when test="${status2.count % 2 == 0}">
								<c:set var="class" value="row1" />
							</c:when>
							<c:otherwise>
								<c:set var="class" value="row2" />
							</c:otherwise>
				    	</c:choose>
						
						</c:forEach>
					</table>
	     		</div>
	 		</div>
	 	
    	</div>
	    	
	    <div class="footer">
	    	<c:import url="include/footer.jsp" />
	    </div>
    </div>
  </body>
</html>

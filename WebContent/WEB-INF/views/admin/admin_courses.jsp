<%@ page language="java" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<fmt:setLocale value="${sessionScope.language}"/>
<fmt:setBundle basename="org.ulpmm.univrav.language.messages"/>

<!doctype html>
<html>
  <head>

  	<meta charset="utf-8">
    
    <title><fmt:message key="Univ-R AV Audiovid&eacute;cours"/></title>

	<link rel="stylesheet" type="text/css" href="../files/thickbox/thickbox.css" media="screen">
	<link rel="stylesheet" type="text/css" href="../files/styles/${sessionScope.style}/css/styles.css">
	<link rel="stylesheet" type="text/css" href="../files/styles/${sessionScope.style}/css/admin.css">
	
	<!--[if IE]>
   		<link rel="stylesheet" type="text/css" href="../files/styles/${sessionScope.style}/css/styles_ie.css" media="screen" />
	<![endif]-->
	<!--[if lte IE 6]>
		<link rel="stylesheet" type="text/css" href="../files/styles/${sessionScope.style}/css/styles_ie6.css" media="screen" />
		<link rel="stylesheet" type="text/css" href="../files/styles/${sessionScope.style}/css/menus_off_ie6.css" media="screen" />
		<script defer type="text/javascript" src="../files/js/pngfix.js"></script>
	<![endif]-->

	<script type="text/javascript" src="../files/js/confirmation.js"></script>
	<script type="text/javascript" src="../files/thickbox/jquery.js"></script>
	<script type="text/javascript" src="../files/thickbox/thickbox.js"></script>

  </head>
  
  <body>
    <div class="main">
	    <div class="contents">
	    	<div class="banner">
	    		<c:import url="../include/banner.jsp" />
	    	</div>
	    	
	    	<div class="links">
		    	<c:import url="./links.jsp" />
	    	</div>
	    		    	
	    	<display:table id="courses" name="courses" requestURI="${viewurl}" class="displaytag">
	    		<display:column property="courseid" title="N°" sortable="true"/>
	    		<display:column property="date" sortable="true" />
				<display:column property="type" sortable="true" />
				<display:column property="title" sortable="true" />
				<display:column property="formation" sortable="true" />
				<display:column property="name" sortable="true" />
				<display:column property="firstname" />
				<display:column property="ipaddress" sortable="true" />
				<display:column property="recorddate" sortable="true" />
				<display:column property="durationString" title="Duration" sortable="true" />
				<display:column title="visible" sortable="true">
					<input type="checkbox" disabled="disabled" ${courses.visible == true ? 'checked' : '' } />
				</display:column>
				<display:column property="consultations" title="views" sortable="true" />
				<display:column property="userid" title="user id" sortable="true" />
				<display:column>
					<a href="<c:url value="${editurl}?id=${courses.courseid}" />">Edit</a>
				</display:column>
				<display:column>
					<a href="javascript:confirmation('Delete the course ${courses.courseid} named ${fn:replace(courses.title,'\'',' ')}?','${deleteurl}?id=${courses.courseid}')">Delete</a>
				</display:column>
	    	</display:table>
	  	    		    		
	    	<br>
	    	<p class="nbr">${number} courses</p>
	    </div>
	    	
	    <div class="footer">
	    	<c:import url="../include/footer.jsp" />
	    </div>
    </div>
  </body>
</html>

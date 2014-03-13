<%@ page language="java" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="dt" uri="/WEB-INF/taglibs-datetime.tld" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<fmt:setLocale value="${sessionScope.language}"/>
<fmt:setBundle basename="org.ulpmm.univrav.language.messages"/>

<!doctype html>
<html>
  <head>
    
    <meta charset="utf-8">
    
    <title><fmt:message key="Univ-R AV Audiovid&eacute;cours"/> - <fmt:message key="Visualisation du cours"/>&nbsp;${course.title}</title>

	<link rel="stylesheet" type="text/css" href="../files/styles/${sessionScope.style}/css/styles.css">
	<link rel="stylesheet" type="text/css" href="../files/styles/${sessionScope.style}/css/visualization.css">
	<link rel="stylesheet" type="text/css" href="../files/thickbox/thickbox.css" media="screen">
	
	<!--[if IE]>
   		<link rel="stylesheet" type="text/css" href="../files/styles/${sessionScope.style}/css/styles_ie.css" media="screen" />
		<link rel="stylesheet" type="text/css" href="../files/styles/${sessionScope.style}/css/visualization_ie.css" media="screen" />
	<![endif]-->
	<!--[if lte IE 6]>
		<link rel="stylesheet" type="text/css" href="../files/styles/${sessionScope.style}/css/visualization_ie6.css" media="screen" />
		<script defer type="text/javascript" src="../files/js/pngfix.js"></script>
	<![endif]-->
	
	<script type="text/javascript" src="../files/js/jquery.min.js"></script>

	<!-- Redirection to html5 for mobile -->
	<script type="text/javascript" src="../files/js/mobile_detection.js"></script>
	<script type="text/javascript">
		if (isMobile.any()) {
			document.location = "./courseaccess?id=${course.courseid}&type=html5";
		}
	</script>
	
	<script type="text/javascript" src="../files/thickbox/thickbox.js"></script>
	<script type="text/javascript" src="../files/jwflvplayer/swfobject.js"></script>
	<!-- <script type="text/javascript" src="../files/js/ieupdate.js"></script> -->
	<!--<script type="text/javascript" src="../files/js/recordinterface_flash.js"></script>
	<script type="text/javascript">
		var timecodes = ${slides};
		var style = "${sessionScope.style}";
		var slidesurl = "${slidesurl}";
		var timing = ${timing};
	</script>-->
	
	<meta name="keywords" content="${course.name},${course.title},${course.formation}">
	
	<!-- google analytics -->
	<c:import url="include/google_analytics.jsp" />
	
  </head>
  
  <body>
    <div class="main">
	    <div class="contents">
	    	
	    	<div class="firstline">
		    	<div class="amphitheatre">${building} | ${amphi}</div>
		    	<a class="closeButton" href=".${sessionScope.previousPage}"><fmt:message key="Fermer"/> <img src="../files/styles/${sessionScope.style}/img/close.png" alt="close"></a>
	    	</div>
	    			
	        <div class="visumain">
	        	           				        	           			
	        	<p id=textHqPlayer><a href="./courseaccess?id=${course.courseid}&amp;type=flash"><fmt:message key="Highqualityplayer"/></a></p>
	        	
	        	            
	            <script type="text/javascript">
     				 var flashvars =
     				 {
       			     	file:					'${courseurl}', 
        			 	id:						'flashvideo', 
       				 	autostart:				'true',
       				 	type:					'lighttpd',  
       				 	image:					'../files/img/logo_audio.png',
       				 	plugins:				'captions-1',
       	           		'captions.file':		'${courseurlfolder}/additional_docs/${course.courseid}_captions.xml',
       	           		'captions.back':		'true'		  
     				 };

     				 var params =
     				 {
       			     	allowfullscreen:		'true', 
       					allowscriptaccess:		'always',
       					wmode:					'transparent'
     				 };

      				var attributes =
     				 {
     				 	id:						'flashvideo', 
       					name:					'flashvideo'
     				 };

    				  swfobject.embedSWF('../files/jwflvplayer/player.swf', 'flashvideo', '1024', '576', '9.0.124', false, flashvars, params, attributes);
    				  
   				 </script>
   				 
   				 <div class="divCenter" id="playercontainer"><p id="flashvideo"><a href="http://www.adobe.com/go/getflashplayer">Get flash to see this player</a> <br>or<br> <a href="./courseaccess?id=${course.courseid}&amp;type=html5">Try the html5 player</a></p></div>

 
			</div>
									
    	</div>
    	
    	<div class="footer">
	    	<c:import url="include/footer.jsp" />
	    </div>
		    
    </div>
        
  </body>
</html>
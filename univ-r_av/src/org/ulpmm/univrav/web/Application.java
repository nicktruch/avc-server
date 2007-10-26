package org.ulpmm.univrav.web;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.ulpmm.univrav.dao.DaoException;
import org.ulpmm.univrav.dao.DatabaseImpl;
import org.ulpmm.univrav.dao.FileSystemImpl;
import org.ulpmm.univrav.entities.Amphi;
import org.ulpmm.univrav.entities.Building;
import org.ulpmm.univrav.entities.Course;
import org.ulpmm.univrav.entities.Slide;
import org.ulpmm.univrav.entities.Univr;
import org.ulpmm.univrav.service.ServiceImpl;


public class Application extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private ServiceImpl service;
	private HttpSession session;
	
	/**
	 *  The name of the bundle to search the corresponding language properties files
	 */
	private static final String BUNDLE_NAME = "org.ulpmm.univrav.language.messages"; 
	
	/* Configuration parameters */
	
	// The Url to access to the course on internet
	private static String coursesUrl;
	
	// Folders on the file system
	private static String ftpFolder;
	private static String coursesFolder; 
	private static String liveFolder; 
	
	// Default media filenames in the archive sent by the client
	private static String defaultMp3File;
	private static String defaultRmFile;
	private static String defaultFlashFile;
	
	// Copyright comment
	private static String comment;
	
	// IP address of the Helix Server for the video live
	private static String helixServerIp;
	
	// The settings to connect to the database
	private static String host;
	private static String port;
	private static String database;
	private static String user;
	private static String password;
	
	// The settings of the RSS files
	private static String rssTitle;
	private static String rssName;
	private static String rssDescription;
	private static String serverUrl;
	private static String rssImageUrl;
	private static String recordedInterfaceUrl;
	private static String language;
	
	// The numbers of courses to display at the same time
	private static int homeCourseNumber;
	private static int recordedCourseNumber;
	
	// The keyword to identify the tests to delete (genre is equal to this keyword)
	private static String testKeyWord1;

	// The keyword to identify the tests to hide (title begins with this keyword)
	private static String testKeyWord2;
	private static String testKeyWord3;
	
	// The client port for the Univ-R integration
	private static int clientSocketPort;
	
	/**
	 * Initialization of the servlet. <br>
	 *
	 * @throws ServletException if an error occurs
	 */
	public void init() throws ServletException {
		
		System.out.println("Univ-R AV : init method called");
		
		Properties p = new Properties();
		try {
			/* Gets the instance of the service layer */
			service = ServiceImpl.getInstance();
			
			/* configuration parameters loading */
			p.load(new FileInputStream(getServletContext().getRealPath("/conf") + "/univrav.properties"));
			
			// The Url to access to the course on internet
			coursesUrl = p.getProperty("coursesUrl");
			
			// Folders on the file system
			ftpFolder = p.getProperty("ftpFolder");
			coursesFolder = p.getProperty("coursesFolder");
			liveFolder = p.getProperty("liveFolder");
			
			// Default media filenames in the archive sent by the client
			defaultMp3File = p.getProperty("defaultMp3File");
			defaultRmFile = p.getProperty("defaultRmFile");
			defaultFlashFile = p.getProperty("defaultFlashFile");
			
			// Copyright comment
			comment = service.cleanString(p.getProperty("comment"));
			
			// IP address of the Helix Server for the video live
			helixServerIp = p.getProperty("helixServerIp");
			
			// The settings to connect to the database
			host = p.getProperty("host");
			port = p.getProperty("port");
			database = p.getProperty("database");
			user = p.getProperty("user");
			password = p.getProperty("password");
			
			// The settings of the RSS files
			rssTitle = p.getProperty("rssTitle");
			rssName = p.getProperty("rssName");
			rssDescription = p.getProperty("rssDescription");
			serverUrl = p.getProperty("serverUrl");
			rssImageUrl = p.getProperty("rssImageUrl");
			recordedInterfaceUrl = p.getProperty("recordedInterfaceUrl");
			language = p.getProperty("language");
			
			// The numbers of courses to display at the same time
			homeCourseNumber = Integer.parseInt(p.getProperty("homeCourseNumber"));
			recordedCourseNumber = Integer.parseInt(p.getProperty("recordedCourseNumber"));
			
			// The keyword to identify the tests to delete (genre is equal to this keyword)
			testKeyWord1 = p.getProperty("testKeyWord1");

			// The keyword to identify the tests to hide (title begins with this keyword)
			testKeyWord2 = p.getProperty("testKeyWord2");
			testKeyWord3 = p.getProperty("testKeyWord3");
			
			// The client port for the Univ-R integration
			clientSocketPort = Integer.parseInt(p.getProperty("clientSocketPort"));
			
			/* Creates the instance of the data access layer */
			DatabaseImpl db = new DatabaseImpl(host, port, database, user, password);
			FileSystemImpl fs = new FileSystemImpl(
					getServletContext().getRealPath("/") + "scripts",
					ftpFolder, coursesFolder, liveFolder, coursesUrl,
					defaultMp3File, defaultRmFile, defaultFlashFile, comment
			);
			
			/* Links the data access layer to the service layer */
			service.setDb(db);
			service.setFs(fs);
			
			/* Creation of the RSS files */
			service.generateRss(getServletContext().getRealPath("/rss"), rssName, rssTitle, rssDescription, serverUrl, rssImageUrl, recordedInterfaceUrl, language);
		}
		catch( IOException e) {
			System.out.println("Impossible to find the configuration file");
			e.printStackTrace();
			destroy();
		}
		catch( DaoException de) {
			de.printStackTrace();
			destroy();
		}
		catch( Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Destruction of the servlet. <br>
	 */
	public void destroy() {
		super.destroy(); // Just puts "destroy" string in log
		// Put your code here
	}

	/**
	 * The doGet method of the servlet. <br>
	 *
	 * This method is called when a form has its tag value method equals to get.
	 * 
	 * @param request the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException if an error occurred
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		request.setCharacterEncoding("UTF-8");
		
		/* Retrieves the current session or creates a new one if no session exists */
		session = request.getSession(true);
		System.out.println("new session : " + session.isNew());
		
		String style = null;
		String language = null;
		
		/* If the session didn't exist and has just been created */
		if( session.isNew()) {
			/* Checks if the style and language are stored in the cookies */
			Cookie[] cookies = request.getCookies();
			
			/* If the browser has not disabled cookies */
			if( cookies != null ) {
				for(int i=0 ; i<cookies.length ; i++) {
					String cookieName = cookies[i].getName();
					if( cookieName.equals("style") )
						style=cookies[i].getValue();
					else if( cookieName.equals("language") ) 
						language = cookies[i].getValue();
				}
			}
			
			/* If not, store the default values in the cookies */
			if( style == null) {
				style = "style1";
				Cookie styleCookie = new Cookie("style", style);
				styleCookie.setMaxAge(31536000);
				response.addCookie(styleCookie);
			}
			
			if( language == null ){
				language = request.getLocale().getLanguage();
				Cookie languageCookie = new Cookie("language", language);
				languageCookie.setMaxAge(31536000);
				response.addCookie(languageCookie);
			}
			
			/* Store them in the session */
			session.setAttribute("style", style);
			session.setAttribute("language", language);
		}
		
		String page = request.getPathInfo();
		
		if( page == null )
			page = "/home";
		
		if( page.equals("/home")){
			displayHomePage(request, response);
		}
		else if( page.equals("/live"))
			displayLivePage(request, response);
		else if( page.equals("/recorded"))
			displayRecordedPage(request, response);
		else if( page.equals("/search")) {
		//	try {
			displaySearchResults(request, response);
		//	}
		//	catch( Exception e) {
		//		e.printStackTrace();
		//	}
		}
		else if( page.equals("/add"))
			addCourse(request, response);
		else if(page.equals("/livestate"))
			liveState(request, response);
		else if( page.equals("/courseaccess")) {
			courseAccess(request, response);
		}
		else if( page.equals("/liveaccess")) {
			liveAccess(request, response);
		}
		else if( page.equals("/help")) {
			/* Saves the page for the style selection thickbox return */
			session.setAttribute("previousPage", "/help");
			getServletContext().getRequestDispatcher("/WEB-INF/views/help.jsp").forward(request, response);
		}
		else if( page.equals("/changestyle")) {
			changeStyle(request, response);
		}
		else if( page.equals("/changelanguage")) {
			changeLanguage(request, response);
		}
		else if( page.equals("/deletetests")) {
			service.deleteTests(testKeyWord1);
			service.hideTests(testKeyWord2, testKeyWord3);
			/* Regeneration of the RSS files */
			service.generateRss(getServletContext().getRealPath("/rss"), rssName, rssTitle, rssDescription, serverUrl, rssImageUrl, recordedInterfaceUrl, language);
		}
		else if( page.equals("/thick_codeform")) {
			request.setAttribute("id", request.getParameter("id"));
			request.setAttribute("type", request.getParameter("type"));
			getServletContext().getRequestDispatcher("/WEB-INF/views/include/thick_codeform.jsp").forward(request, response);
		}
		else if( page.equals("/thick_styles")) {
			List<String> styles = service.getStyles(getServletContext().getRealPath("/") + "files/styles/");
			request.setAttribute("styles", styles );
			getServletContext().getRequestDispatcher("/WEB-INF/views/include/thick_styles.jsp").forward(request, response);
		}
		else if( page.equals("/thick_languages")) {
			List<String> languages = service.getLanguages(getServletContext().getRealPath("/") + "WEB-INF/classes/org/ulpmm/univrav/language");
			request.setAttribute("languages", languages );
			getServletContext().getRequestDispatcher("/WEB-INF/views/include/thick_languages.jsp").forward(request, response);
		}
		else if( page.equals("/thick_legal")) {
			getServletContext().getRequestDispatcher("/WEB-INF/views/include/thick_legal.jsp").forward(request, response);
		}
		else if( page.equals("/thick_download")) {
			getServletContext().getRequestDispatcher("/WEB-INF/views/include/thick_download.jsp").forward(request, response);
		}
		else if( page.equals("/iframe_liveslide")) {
			liveSlide(request, response);
		}
		else if( page.equals("/admin_courses")) {
			request.setAttribute("courses", service.getAllCourses());
			getServletContext().getRequestDispatcher("/WEB-INF/views/admin/admin_courses.jsp").forward(request, response);
		}
		else if( page.equals("/admin_editcourse")) {
			request.setAttribute("course", service.getCourse(Integer.parseInt(request.getParameter("id"))));
			getServletContext().getRequestDispatcher("/WEB-INF/views/admin/admin_editcourse.jsp").forward(request, response);
		}
		else if( page.equals("/admin_deletecourse")) {
			int courseid = Integer.parseInt(request.getParameter("id"));
			service.deleteCourse(courseid, service.getCourse(courseid).getMediaFolder());
			/* Regeneration of the RSS files */
			service.generateRss(getServletContext().getRealPath("/rss"), rssName, rssTitle, rssDescription, serverUrl, rssImageUrl, recordedInterfaceUrl, language);
			response.sendRedirect(response.encodeRedirectURL("./admin_courses"));
		}
		else if( page.equals("/admin_validatecourse")) {
			validateCourse(request, response);
		}
		else if( page.equals("/admin_buildings")) {
			request.setAttribute("buildings", service.getBuildings());
			getServletContext().getRequestDispatcher("/WEB-INF/views/admin/admin_buildings.jsp").forward(request, response);
		}
		else if( page.equals("/admin_addbuilding")) {
			request.setAttribute("action","add"); 
			getServletContext().getRequestDispatcher("/WEB-INF/views/admin/admin_editbuilding.jsp").forward(request, response);
		}
		else if( page.equals("/admin_editbuilding")) {
			request.setAttribute("action","edit"); 
			request.setAttribute("building", service.getBuilding(Integer.parseInt(request.getParameter("id"))));
			getServletContext().getRequestDispatcher("/WEB-INF/views/admin/admin_editbuilding.jsp").forward(request, response);
		}
		else if( page.equals("/admin_deletebuilding")) {
			int buildingid = Integer.parseInt(request.getParameter("id"));
			service.deleteBuilding(buildingid);
			response.sendRedirect(response.encodeRedirectURL("./admin_buildings"));
		}
		else if( page.equals("/admin_validatebuilding")) {
			validateBuilding(request, response);
		}
		else if( page.equals("/admin_amphis")) {
			request.setAttribute("buildingId", request.getParameter("buildingId"));
			request.setAttribute("amphis", service.getAmphis(Integer.parseInt(request.getParameter("buildingId"))));
			getServletContext().getRequestDispatcher("/WEB-INF/views/admin/admin_amphis.jsp").forward(request, response);
		}
		else if( page.equals("/admin_addamphi")) {
			request.setAttribute("buildingId", request.getParameter("buildingId"));
			request.setAttribute("action","add"); 
			getServletContext().getRequestDispatcher("/WEB-INF/views/admin/admin_editamphi.jsp").forward(request, response);
		}
		else if( page.equals("/admin_editamphi")) {
			request.setAttribute("buildingId", request.getParameter("buildingId"));
			request.setAttribute("action","edit"); 
			request.setAttribute("amphi", service.getAmphi(Integer.parseInt(request.getParameter("id"))));
			getServletContext().getRequestDispatcher("/WEB-INF/views/admin/admin_editamphi.jsp").forward(request, response);
		}
		else if( page.equals("/admin_deleteamphi")) {
			int amphiid = Integer.parseInt(request.getParameter("id"));
			service.deleteAmphi(amphiid);
			response.sendRedirect(response.encodeRedirectURL("./admin_amphis?buildingId=" + request.getParameter("buildingId")));
		}
		else if( page.equals("/admin_validateamphi")) {
			validateAmphi(request, response);
		}
		else if( page.equals("/univr_creation")) {
			univrCreation(request, response);
		}
		else if( page.equals("/univr_courseaccess")) {
			univrCourseAccess(request, response);
		}
		else
			displayHomePage(request, response);
	}

	/**
	 * The doPost method of the servlet. <br>
	 *
	 * This method is called when a form has its tag value method equals to post.
	 * 
	 * @param request the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException if an error occurred
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		doGet(request, response);
	}
	
	private void displayHomePage(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {
		// le modèle de la vue [list]
		request.setAttribute("teachers", service.getTeachers());
		request.setAttribute("formations", service.getFormations());
		request.setAttribute("courses", service.getNLastCourses(homeCourseNumber));
		request.setAttribute("rssFileName", rssName);
		
		request.setAttribute("rssfiles", service.getRssFileList(rssTitle, rssName));
		
		/* Saves the page for the style selection thickbox return */
		session.setAttribute("previousPage", "/home");
		
		// affichage de la vue [list]
		getServletContext().getRequestDispatcher("/WEB-INF/views/home.jsp").forward(request, response);
	}
	
	private void displayLivePage(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {  
		// le modèle de la vue [list]  
		request.setAttribute("buildings", service.getBuildings());
		
		/* Saves the page for the style selection thickbox return */
		session.setAttribute("previousPage", "/live");
		
		// affichage de la vue [list] 
		getServletContext().getRequestDispatcher("/WEB-INF/views/live.jsp").forward(request, response);
	}
	
	private void displayRecordedPage(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {
		
		int start = 0;
		int pageNumber;
		
		// le modèle de la vue [list]
		if( request.getParameter("page") != null) {
			pageNumber = Integer.parseInt( request.getParameter("page"));
			start = recordedCourseNumber * (pageNumber - 1) ;
		}
		else
			pageNumber = 1;
		
		request.setAttribute("page", pageNumber);
		request.setAttribute("teachers", service.getTeachers());
		request.setAttribute("formations", service.getFormations());
		request.setAttribute("courses", service.getCourses(recordedCourseNumber, start));
		request.setAttribute("items", service.getCourseNumber());
		request.setAttribute("number", recordedCourseNumber);
		request.setAttribute("resultPage", "recorded");
		
		request.setAttribute("rssfiles", service.getRssFileList(rssTitle, rssName));
		
		/* Saves the page for the style selection thickbox return */
		session.setAttribute("previousPage", "/recorded?page=" + pageNumber);
		
		// affichage de la vue [list]
		getServletContext().getRequestDispatcher("/WEB-INF/views/recorded.jsp").forward(request, response);
	}
	
	private void displaySearchResults(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {
		
		int start = 0;
		int pageNumber;
		
		HashMap<String, String> params = new HashMap<String, String>();
		
		if( request.getMethod().equals("POST")) { // The form has just been posted
			
			pageNumber = 1;			
			
			/* Puts the search paramaters in a HashMap object */
			if( request.getParameter("audio") != null && request.getParameter("video") == null ) 
				params.put("type", "audio");
			else if( request.getParameter("audio") == null && request.getParameter("video") != null ) 
				params.put("type", "video");
			else if( request.getParameter("audio") == null && request.getParameter("video") == null ) 
				params.put("type", "");
			
			if( request.getParameter("fullname") != null && ! request.getParameter("fullname").equals("*") ) 
				params.put("fullname", request.getParameter("fullname"));
			
			if( request.getParameter("formation") != null && ! request.getParameter("formation").equals("*") ) 
				params.put("formation", request.getParameter("formation"));
			
			if( request.getParameter("keyword") != null && ! request.getParameter("keyword").equals("") ) 
				params.put("keyword", request.getParameter("keyword"));
			
			/* Saves the hashmap in the session */
			session.setAttribute("params", params);
		}
		else { // The user has clicked on a pagination link
			
			pageNumber = Integer.parseInt( request.getParameter("page"));
			start = recordedCourseNumber * (pageNumber - 1) ;
			
			params = (HashMap<String, String>) session.getAttribute("params");
		}
		
		if( params != null) {
			/* Saves the parameters of the form */
			if( params.get("type") == null ) { 
				request.setAttribute("audio", "checked");
				request.setAttribute("video", "checked");
			}
			else if( (params.get("type")).equals("audio") ) {
				request.setAttribute("audio", "checked");
			}
			else if( (params.get("type")).equals("video") ) {
				request.setAttribute("video", "checked");
			}
			
			if( params.get("fullname") != null && ! params.get("fullname").equals("*") ) {
				request.setAttribute("nameSelected", params.get("fullname"));
			}
			
			if( params.get("formation") != null && ! params.get("formation").equals("*") ) {
				request.setAttribute("formationSelected", params.get("formation"));
			}
			
			if( params.get("keyword") != null && ! params.get("keyword").equals("") ) {
				request.setAttribute("keyword", params.get("keyword"));
			}
			
			request.setAttribute("page", pageNumber);
			request.setAttribute("teachers", service.getTeachers());
			request.setAttribute("formations", service.getFormations());
			request.setAttribute("courses", service.getCourses(params, recordedCourseNumber, start));
			request.setAttribute("items", service.getCourseNumber(params));
			request.setAttribute("number", recordedCourseNumber);
			request.setAttribute("resultPage", "search");
			
			request.setAttribute("rssfiles", service.getRssFileList(rssTitle, rssName));
			
			/* Saves the page for the style selection thickbox return */
			session.setAttribute("previousPage", "/search?page=" + pageNumber);
			
			// affichage de la vue [list]
			getServletContext().getRequestDispatcher("/WEB-INF/views/recorded.jsp").forward(request, response);
		}
		else { // if the session is not valid anymore
			response.sendRedirect("./recorded");
		}
	}
	
	private void addCourse(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {
		
		String id, title, description, mediapath, media, timing, name, firstname, formation, genre;
		String message = "";
		String messageType = "information";
		
		/* Le client envoie les informations en ISO8859-15 */
		request.setCharacterEncoding("ISO8859-15");
		
		/* récupération des paramètres du client */
		id = request.getParameter("id");
		mediapath = request.getParameter("mediapath");
		timing = request.getParameter("timing");
		
		description = service.cleanString(request.getParameter("description"));
		title = service.cleanString(request.getParameter("title"));
		name = service.cleanString(request.getParameter("name"));
		firstname = service.cleanString(request.getParameter("firstname"));
		formation = service.cleanString(request.getParameter("ue"));
		genre = request.getParameter("genre");
		
		/* Vérification que les paramètres essentiels ont bien été envoyés, annulation de l'upload dans le cas contraire */
		if(id != null && description != null && mediapath != null && title != null && name != null && formation != null) {
			
			if( timing == null )
				timing = "n-1";
			
			String clientIP = request.getRemoteAddr();
			
			/* Affichage des informations reçues */
			/*message += "ID : " + id + "<br/>";
			message += "Title : " + title + "<br/>";
			message += "Description : " + description + "<br/>";
			message += "Mediapath : " + mediapath + "<br/>";
			message += "Teacher : " + name + " " + firstname + "<br>";
			message += "UE : " + formation + "<br>";
			message += "Genre : " + genre + "<br>";
			message += "Timing : " + timing + "<br>";*/
			
			/* Récupération du nom du fichier à partir de son chemin d'accès */
			media = mediapath.substring(mediapath.lastIndexOf("\\") + 1, mediapath.length());
			if( media.length() == mediapath.length() )
				media = mediapath.substring(mediapath.lastIndexOf("/") + 1, mediapath.length());
			
			message += "Media : " + media + "<br/>";
			
			Course c;
			
			if( id.equals("") || id.equals("(id:no)")) {
				c = new Course(
						service.getNextCoursId(),
						new Timestamp(new Date().getTime()),
						null, // The type can't be set yet
						title.equals("") ? null : title,
						description.equals("") ? null : description,
						formation.equals("") ? null : formation,
						name.equals("") ? null : name,
						(firstname == null || firstname.equals("")) ? null : firstname,
						clientIP,
						0, // The duration can't be set yet
						(genre == null || genre.equals("")) ? null : genre,
						true,
						0,
						timing,
						null // The media folder can't be set yet
				);
				service.addCourse(c, media);
			}
			else {
				c = service.getCourse(Integer.parseInt(id));
				c.setDate(new Timestamp(new Date().getTime()));
				c.setVisible(true);
				c.setTiming(timing);
				service.completeUnivrCourse(c, media);
			}
			
			/* Generation of the RSS files */
			if( c.getGenre() == null)
				service.generateRss(c, getServletContext().getRealPath("/rss"), rssName, rssTitle, rssDescription, serverUrl, rssImageUrl, recordedInterfaceUrl, language);
			
			message = "File successfully sent !";
								
		}
		else {
			messageType = "error";
			message = "Erreur: One ore more parameters have not been given";
		}
		
		request.setAttribute("messagetype", messageType);
		request.setAttribute("message", message);
		getServletContext().getRequestDispatcher("/WEB-INF/views/message.jsp").forward(request, response);
	}
	
	private void liveState(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {
		
		String message = "";
		String messageType = "information";
		
		String recordingPlace = request.getParameter("recordingPlace");
		String status = request.getParameter("status");
		
		/* Vérification que les paramètres sont envoyés et non vides */
		if( recordingPlace != null && status != null && ! recordingPlace.equals("") && ! status.equals("")) {
			
			/* Vérification que status contient une des deux chaînes attendues */
			if( status.equals("begin") || status.equals("end")) {
				service.setAmphiStatus(recordingPlace, status.equals("begin"));
				message = "Amphi : " + recordingPlace + " : " + status;
			}
			else {
				messageType = "error";
				message = "Error: possible status values for status : {begin ; end}";
			}
		} 
		else {
			messageType = "error";
			message = "Error: missing parameters: recordingPlace and status must be sent";
		}
		
		request.setAttribute("messagetype", messageType);
		request.setAttribute("message", message);
		getServletContext().getRequestDispatcher("/WEB-INF/views/message.jsp").forward(request, response);
	}
	
	private void courseAccess(HttpServletRequest request, HttpServletResponse response) 
		throws ServletException, IOException {
		
		int courseid = Integer.parseInt( (String) request.getParameter("id"));
		Course c = null;
		String genre = (String) request.getParameter("code");
		String type = (String) request.getParameter("type");
		
		try {
			if( genre == null)
				c = service.getCourse(courseid);
			else
				c = service.getCourse(courseid, genre);
			
			service.incrementConsultations(c);
			
			if( type.equals("real")) {
				//redirection interface
				request.setAttribute("courseurl", coursesUrl + c.getMediaFolder() + "/" + c.getMediasFileName() + ".smil");
				request.setAttribute("slidesurl", coursesUrl + c.getMediaFolder() + "/screenshots/");
				List<Slide> slides = service.getSlides(c.getCourseid());
				request.setAttribute("slides", slides);
				Amphi a = service.getAmphi(c.getIpaddress());
				String amphi = a != null ? a.getName() : c.getIpaddress();
				String building = service.getBuildingName(c.getIpaddress());
				request.setAttribute("amphi", amphi);
				request.setAttribute("building", building);
				if( c.getTiming().equals("n-1"))
					request.setAttribute("timing", 1);
				else
					request.setAttribute("timing", 0);
				
				// affichage de la vue [list]
				getServletContext().getRequestDispatcher("/WEB-INF/views/recordinterface_smil.jsp").forward(request, response);
			}
			else if( type.equals("flash")) {
				//redirection interface
				request.setAttribute("courseurl", coursesUrl + c.getMediaFolder() + "/" + c.getMediasFileName() + ".swf");
				Amphi a = service.getAmphi(c.getIpaddress());
				String amphi = a != null ? a.getName() : c.getIpaddress();
				String building = service.getBuildingName(c.getIpaddress());
				request.setAttribute("amphi", amphi);
				request.setAttribute("building", building);
				
				// affichage de la vue [list]
				getServletContext().getRequestDispatcher("/WEB-INF/views/recordinterface_flash.jsp").forward(request, response);
			}
			else {
				String filename = coursesFolder + c.getMediaFolder() + "/" + c.getMediasFileName() + "." + type;
				
				// Initialisation des en-têtes.
				response.setContentType("application/x-download");
				response.setHeader("Content-Disposition", "attachment; filename=" + c.getMediasFileName() + "." + type);

				// Envoi du fichier.
				OutputStream out = response.getOutputStream();
				service.returnFile(filename, out);
				
				/*String previousPage = (String) request.getSession().getAttribute("previousPage");
				if( previousPage == null)
					previousPage = "/home";
				response.sendRedirect("." + previousPage);*/
			}
		}
		catch(DaoException de) {
			ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_NAME, new Locale( (String) session.getAttribute("language")));
			request.setAttribute("messagetype", "error");
			request.setAttribute("message", bundle.getString("wrongAccessCode"));
			getServletContext().getRequestDispatcher("/WEB-INF/views/message.jsp").forward(request, response);
		}
	}
	
	private void liveAccess(HttpServletRequest request, HttpServletResponse response) 
		throws ServletException, IOException {
		
		String ip = request.getParameter("amphi");
		Amphi a = service.getAmphi(ip);
		String amphi = a != null ? a.getName() : ip;
		String building = service.getBuildingName(ip);
		String url = "";
		if( a.getType().equals("audio")) {
			url = "http://" + ip + ":8080";
		}
		else if( a.getType().equals("video")) {
			service.createLiveVideo(ip, helixServerIp);
			url =  "../../live/livevideo_" + ip.replace('.','_') + ".ram";
		}
		
		request.setAttribute("amphi", amphi);
		request.setAttribute("building", building);
		request.setAttribute("type", a.getType());
		request.setAttribute("ip", ip);
		request.setAttribute("url", url);
		
		getServletContext().getRequestDispatcher("/WEB-INF/views/liveinterface.jsp").forward(request, response);
	}
	
	private void liveSlide(HttpServletRequest request, HttpServletResponse response) 
		throws ServletException, IOException {
		
		String ip = request.getParameter("ip");
		String url = "../../live/" + ip.replace('.','_') + ".jpg";
		request.setAttribute("ip", ip);
		request.setAttribute("url", url);
		
		getServletContext().getRequestDispatcher("/WEB-INF/views/include/iframe_liveslide.jsp").forward(request, response);
	}
	
	private void validateCourse(HttpServletRequest request, HttpServletResponse response) 
		throws ServletException, IOException {
		
		Course c= new Course(
			Integer.parseInt(request.getParameter("courseid")),
			new Timestamp(Long.parseLong(request.getParameter("date"))),
			request.getParameter("type"),
			request.getParameter("title"),
			request.getParameter("description"),
			request.getParameter("formation"),
			request.getParameter("name"),
			request.getParameter("firstname"),
			request.getParameter("ipaddress"),
			Integer.parseInt(request.getParameter("duration")),
			request.getParameter("genre"),
			request.getParameter("visible") != null ? true : false,
			Integer.parseInt(request.getParameter("consultations")),
			request.getParameter("timing"),
			request.getParameter("mediaFolder")
		);
		service.modifyCourse(c);
		
		/* Regeneration of the RSS files */
		if( c.getGenre() != null )
			service.generateRss(c, getServletContext().getRealPath("/rss"), rssName, rssTitle, rssDescription, serverUrl, rssImageUrl, recordedInterfaceUrl, language);
		response.sendRedirect(response.encodeRedirectURL("./admin_courses"));
	}
	
	private void validateBuilding(HttpServletRequest request, HttpServletResponse response) 
		throws ServletException, IOException {
		
		int buildingId =  ! request.getParameter("buildingid").equals("") ? Integer.parseInt(request.getParameter("buildingid")) : 0;
		
		Building b = new Building(
			buildingId,
			request.getParameter("name"),
			request.getParameter("imagefile")
		);
		
		if(request.getParameter("action").equals("edit"))
			service.modifyBuilding(b);
		else
			service.addBuilding(b);
		response.sendRedirect(response.encodeRedirectURL("./admin_buildings"));
	}
	
	private void validateAmphi(HttpServletRequest request, HttpServletResponse response) 
		throws ServletException, IOException {
		
		int amphiId =  ! request.getParameter("amphiid").equals("") ? Integer.parseInt(request.getParameter("amphiid")) : 0;
		
		Amphi a = new Amphi(
			amphiId,
			Integer.parseInt(request.getParameter("buildingid")),
			request.getParameter("name"),
			request.getParameter("type"),
			request.getParameter("ipaddress"),
			Boolean.parseBoolean(request.getParameter("status"))
		);
		
		if(request.getParameter("action").equals("edit"))
			service.modifyAmphi(a);
		else
			service.addAmphi(a);
		response.sendRedirect(response.encodeRedirectURL("./admin_amphis?buildingId=" + request.getParameter("buildingid")));
	}
	
	private void changeStyle(HttpServletRequest request, HttpServletResponse response) 
		throws ServletException, IOException {
		String style = request.getParameter("style");
		/* Store the style in the session */
		session.setAttribute("style", style);
		/* Store the style in the cookies */
		Cookie styleCookie = new Cookie("style", style);
		styleCookie.setMaxAge(31536000);
		response.addCookie(styleCookie);
		
		/* Returns to the page before the thickbox call (stored in the session) */
		String previousPage = (String) session.getAttribute("previousPage");
		if( previousPage == null)
			previousPage = "/home";
		response.sendRedirect(response.encodeRedirectURL("." + previousPage));
	}
	
	private void changeLanguage(HttpServletRequest request, HttpServletResponse response) 
		throws ServletException, IOException {
		String language = request.getParameter("language");
		/* Store the style in the session */
		session.setAttribute("language", language);
		/* Store the style in the cookies */
		Cookie languageCookie = new Cookie("language", language);
		languageCookie.setMaxAge(31536000);
		response.addCookie(languageCookie);
		
		/* Returns to the page before the thickbox call (stored in the session) */
		String previousPage = (String) session.getAttribute("previousPage");
		if( previousPage == null)
			previousPage = "/home";
		response.sendRedirect(response.encodeRedirectURL("." + previousPage));
	}
	
	private void univrCreation(HttpServletRequest request, HttpServletResponse response) 
		throws ServletException, IOException {
		
		String message = "Veuillez suivre les indications à l'écran";
		String messageType = "information";

		int courseId = service.getNextCoursId();
		
		/* Récupération des paramètres envoyés par Univers */
		/* (envoi des informations en LATIN9) */
		request.setCharacterEncoding("LATIN9");
		
		String uid = request.getParameter("uid");
		String uuid = request.getParameter("uuid");
		String groupCode = request.getParameter("codegroupe");
		String ip = request.getParameter("ip");
		
		String description = request.getParameter("description");
		String title = request.getParameter("titre");
		String genre = request.getParameter("genre");
		
		/* Vérification que les paramètres ont bien été envoyés */
		if( uid != null && uuid != null && groupCode != null && ip != null && (! groupCode.equals("")) && (! ip.equals(""))) {
		
			/*out.println("ID du cours : " + courseId + "<br>");
			out.println("User ID : " + uid + "<br>");
			out.println("Code groupe : " + groupCode + "<br>");
			
			out.println("Titre : " + title + "<br/>");
			out.println("Description : " + description + "<br/>");
			out.println("IP : " + ip + "<br/>");*/
			
			if( service.getAmphi(ip) != null) {
				
				try {
					
					int user = Integer.parseInt(uid);
					int group = Integer.parseInt(groupCode);
					
					/* Si l'utilisateur est autorisé , vérification des droits pour le cours */
					//if( service.isUserAuth(user, uuid)) {
						
						//HashMap<String, String> map = service.getUserInfos(user);
						String name = ""/*map.get("nom")*/;
						String firstname = ""/*map.get("prenom")*/;
						String formation = ""/*service.getGroupName(Integer.parseInt(groupCode))*/;
						
						String msg = "(id:" + courseId + ")";
						//String answer = service.sendMessageToClient(msg, ip, clientSocketPort);
						
						Course c = new Course(
								courseId,
								new Timestamp(new Date().getTime()),
								null, // The type can't be set yet
								title.equals("") ? null : title,
								description.equals("") ? null : description,
								formation.equals("") ? null : formation,
								name.equals("") ? null : name,
								firstname.equals("") ? null : firstname,
								ip,
								0, // The duration can't be set yet
								genre.equals("") ? null : genre,
								false,
								0,
								null, // The timing can't be set yet
								null // The media folder can't be set yet
						);
						
						Univr u = new Univr(courseId, user, group);
						
						service.addUnivrCourse(c,u);
					/*}
					else {
						messageType = "error";
						message = "Erreur: Utilisateur non authentifié";
					}*/
				}
				catch( NumberFormatException nfe) {
					messageType = "error";
					message = "Erreur: Paramètres incorrects";
					System.out.println("Param&egrave;tres incorrects !");
					System.out.println( nfe.toString());
				}
				catch( Exception e ) {
					messageType = "error";
					message = "Erreur: Problème lors de la connexion au client";
					System.out.println("Problème lors de la connexion au client !");
					System.out.println( e.toString() );
					e.printStackTrace();
				}
			}
			else {
				messageType = "error";
				message = "Erreur: ";
				System.out.println("amphi inconnu !");
			}
		}
		else {
			messageType = "error";
			message = "Erreur: paramètres manquants";
			System.out.println("Paramètres manquants !");
		}
		
		request.setAttribute("messagetype", messageType);
		request.setAttribute("message", message);
		getServletContext().getRequestDispatcher("/WEB-INF/views/message.jsp").forward(request, response);
	}
	
	private void univrCourseAccess(HttpServletRequest request, HttpServletResponse response) 
		throws ServletException, IOException {
		
		String message = "";
		String forwardUrl = "/WEB-INF/views/recordinterface_smil.jsp";
		
		String id = request.getParameter("id");
		String uid = request.getParameter("uid");
		String uuid = request.getParameter("uuid");
		
		/* Vérification que les paramètres ont bien été envoyés */
		if( id != null && uid != null && uuid != null) {
		
			try {
				int courseId = Integer.parseInt(id);
				int userId = Integer.parseInt(uid);
				
				/* Si l'utilisateur est loggué , vérification des droits pour le cours */
				//if( service.isUserAuth(userId, uuid)) {
					
					//if ( service.hasAccessToCourse(userId, courseId)) {
						
						Course c = null;
						c = service.getCourse(courseId);	
						service.incrementConsultations(c);
						
						//redirection interface
						request.setAttribute("courseurl", coursesUrl + c.getMediaFolder() + "/" + c.getMediasFileName() + ".smil");
						request.setAttribute("slidesurl", coursesUrl + c.getMediaFolder() + "/screenshots/");
						List<Slide> slides = service.getSlides(c.getCourseid());
						request.setAttribute("slides", slides);
						Amphi a = service.getAmphi(c.getIpaddress());
						String amphi = a != null ? a.getName() : c.getIpaddress();
						String building = service.getBuildingName(c.getIpaddress());
						request.setAttribute("amphi", amphi);
						request.setAttribute("building", building);
						if( c.getTiming().equals("n-1"))
							request.setAttribute("timing", 1);
						else
							request.setAttribute("timing", 0);
						
						// affichage de la vue [list]
						//getServletContext().getRequestDispatcher("/WEB-INF/views/recordinterface_smil.jsp").forward(request, response);
						
					/*} else {
						message = "Erreur: autorisation refusée";
						request.setAttribute("messagetype", "error");
						request.setAttribute("message", message);
						forwardUrl = "/WEB-INF/views/message.jsp";
					}	 	*/
				/*}
				else {
					message = "Erreur: utilisateur non authentifié";
					request.setAttribute("messagetype", "error");
					request.setAttribute("message", message);
					forwardUrl = "/WEB-INF/views/message.jsp";
				}*/
			}
			catch( NumberFormatException nfe) {
				nfe.printStackTrace();
				message = "Erreur: paramètres incorrects (valeurs numériques attendues)";
				request.setAttribute("messagetype", "error");
				request.setAttribute("message", message);
				forwardUrl = "/WEB-INF/views/message.jsp";
			}
		}
		else {
			message = "Erreur: paramètres manquants";
			request.setAttribute("messagetype", "error");
			request.setAttribute("message", message);
			forwardUrl = "/WEB-INF/views/message.jsp";
		}
		
		/* If an error has occured, diplays an error message */
		getServletContext().getRequestDispatcher(forwardUrl).forward(request, response);
	}
	
}
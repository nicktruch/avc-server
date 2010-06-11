package org.ulpmm.univrav.dao;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.fileupload.FileItem;
import org.apache.log4j.Logger;
import org.ulpmm.univrav.entities.Course;
import org.ulpmm.univrav.entities.Tag;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * File system implementation methods
 * 
 * @author morgan
 *
 */
public class FileSystemImpl implements IFileSystem {

	/** The runtime object */
	private static Runtime r;
	
	/** Folder which contains all scripts */
	private static String scriptsFolder;
	
	/** Folder in which the courses are uploaded by FTP */
	private static String ftpFolder;
	
	/** Folder which contains all the courses folders */
	private static String coursesFolder;
	
	/** The URL of courses */
	private static String coursesUrl;
	
	/** Default MP3 filename in the archive sent by the client */
	private static String defaultMp3File;
		
	/** Default Flash filename in the archive sent by the client */
	private static String defaultFlashFile;
		
	/** Copyright comment */
	private static String comment;
	
	/** Logger log4j */
	private static final Logger logger = Logger.getLogger(FileSystemImpl.class);
	
	/** Database interface */
	private static IDatabase db;
	
	
	/**
	 * Constructor for file system
	 * 
	 * @param scriptsFolder Folder which contains the scripts
	 * @param ftpFolder Folder in which the courses are uploaded via FTP
	 * @param coursesFolder Folder which contains all the courses folders
	 * @param liveFolder Folder which contains all the media for the live
	 * @param coursesUrl The URL of courses
	 * @param defaultMp3File Default MP3 filename in the archive sent by the client
	 * @param defaultFlashFile Default Flash filename in the archive sent by the client
	 * @param comment Copyright comment
	 */
	@SuppressWarnings("static-access")
	public FileSystemImpl(String scriptsFolder, String ftpFolder, String coursesFolder, 
		String liveFolder, String coursesUrl, String defaultMp3File, String defaultFlashFile, 
		String comment, IDatabase db) {
		
		r = Runtime.getRuntime();
		this.scriptsFolder = scriptsFolder;
		this.ftpFolder = ftpFolder;
		this.coursesFolder = coursesFolder;
		this.coursesUrl = coursesUrl;
		this.defaultMp3File = defaultMp3File;
		this.defaultFlashFile = defaultFlashFile;
		this.comment = comment;
		this.db = db;
	}
	
	/**
	 * Creates a course flash
	 * @param c the course to create
	 * @param courseArchive the name of the archive file of the course to create
	 */
	public void addCourse(Course c, String courseArchive) {
		
		archiveExtraction(c, courseArchive);
		moveArchive(c, courseArchive);
		setCourseType(c);
		
		if( c.getType().equals("audio")) {
			renameFile(c.getMediaFolder(), defaultMp3File, c.getMediasFileName() + ".mp3");
			setCourseDuration(c, c.getMediaFolder(), c.getMediasFileName(),"mp3");
		}
		else if( c.getType().equals("video")) {
			renameFile(c.getMediaFolder(), defaultFlashFile, c.getMediasFileName() + ".flv");
			injectMetadata(c.getMediaFolder(), c.getMediasFileName(), "flv");
			setCourseDuration(c, c.getMediaFolder(), c.getMediasFileName(),"flv");
		}	
		
		createDescriptionFile(c);
	}
	
	/**
	 * Create the file description.txt
	 * @param c the course
	 */
	public void createDescriptionFile(Course c) {
		
		try {
			/* creates the .txt description file */
			File descriptionFile = new File(coursesFolder + c.getMediaFolder() + "/description.txt");
			descriptionFile.createNewFile();
			PrintWriter pw = new PrintWriter( new OutputStreamWriter( new FileOutputStream( descriptionFile), "ISO8859-15"));
			pw.println("Author:" +  (c.getName() != null ? c.getName() + ( c.getFirstname() != null ? " " + c.getFirstname() : "") : "-"));
			String form = c.getFormation() != null ? db.getFormationFullName(c.getFormation()) : "-";
			pw.println("Formation:" +  (form != null ? form : "-"));
			pw.println("Title:" +  (c.getTitle() != null ? c.getTitle() : "-"));
			pw.println("Subject:" +  (c.getDescription() != null ? c.getDescription() : "-"));
			pw.println("Date:" + c.getDateString());
			pw.println("Type:" + c.getType());
			pw.println("Duration:" + c.getDurationString());
			pw.println("Comment:" + comment);
			pw.close();
		} catch (IOException e) {
			logger.error("error when create description.txt"+e);
		}
		
	}
			
	/**
	 * Creates a course from an uploaded audio or video media file
	 * @param c the course to create
	 * @param mediaFile the media file of the course to create
	 * @param fileName the filename
	 * @param extension the extension
	 */
	public void mediaUpload(Course c, FileItem mediaFile, String fileName, String extension) {
		//Creation of the folder		
		mediaFolderCreation(c, mediaFile, fileName);
		// Rename all upload file into courseid.extension and calculate duration
		
		if(c.getType().equals("video") && !extension.equals("flv")) {
			renameFile(c.getMediaFolder(), fileName, "ori_"+c.getMediasFileName() + "."+extension);
			setCourseDuration(c, c.getMediaFolder(), "ori_"+c.getMediasFileName(),extension);	
		}
		else {
			renameFile(c.getMediaFolder(), fileName, c.getMediasFileName() + "."+extension);
			setCourseDuration(c, c.getMediaFolder(), c.getMediasFileName(),extension);		
		}
		
		// create the description file
		createDescriptionFile(c);
	}
	
	
	/**
	 * Reads the timecodes csv file and creates the timecodes list
	 * @param mediaFolder the folder where the timecode list is stored
	 * @return the timecodes list
	 */
	public ArrayList<String> getTimecodes(String mediaFolder) {
		String s;
		ArrayList<String> timecodes = new ArrayList<String>();
		try {
			BufferedReader in = new BufferedReader(new FileReader(coursesFolder + mediaFolder + "/timecode.csv"));
			while( (s = in.readLine()) != null) {
				timecodes.add(s);
			}
			in.close();
		}
		catch(IOException ioe) {
			logger.warn("Impossible to load the timecodes file",ioe);
		}
		return timecodes;
	}
	
	/**
	 * Removes the media folder of a course on the file system
	 * @param mediaFolder the media folder of the course
	 */
	public void deleteCourse(String mediaFolder) {
		if( mediaFolder != null && ! mediaFolder.equals("")) {
			try {
				Process p = r.exec("rm -Rf " + coursesFolder + mediaFolder);
				if( p.waitFor() != 0)
					logger.error("the course folder " + mediaFolder + " mediaFolder has not been deleted");
			}
			catch(IOException ioe) {
				logger.error("Impossible to delete the course folder");
			}
			catch(InterruptedException ie) {
				logger.error("Impossible to delete the course folder");
			}
		}
	}
	
		
	/**
	 * Retrieves a list of the website's available themes
	 * @param stylesFolder the folder in which the themes are stored
	 * @return the list of themes
	 */
	public List<String> getStyles(String stylesFolder) {
		File f = new File(stylesFolder);
		return Arrays.asList(f.list());
	}
	
	/**
	 * Retrieves a list of the website's available languages
	 * @param languagesFolder the folder in which the language property files are stored
	 * @return the list of languages
	 */
	public List<String> getLanguages(String languagesFolder) {
		File f = new File(languagesFolder);
		String[] files = f.list();
		ArrayList<String> languages = new ArrayList<String>();
		for( int i=0 ; i<files.length ; i++ ) {
			if(files[i].endsWith(".properties")) {
				String language = files[i].substring(files[i].indexOf('_') + 1, files[i].indexOf('.'));
				languages.add(language);
			}
		}
		return languages;
	}
	
	/**
	 * Sends a file to the client's browser
	 * @param filename the name of the file to send
	 * @param out the stream in which send the file
	 */
	public void returnFile(String filename, OutputStream out) {				
		InputStream in = null;
		try {
			URI uri = new URI(null,null,filename,null);
			URLConnection urlConnection = uri.toURL().openConnection();
			in=new BufferedInputStream(urlConnection.getInputStream());
			//in = new BufferedInputStream(new FileInputStream(filename));
			byte[  ] buf = new byte[4 * 1024];  // 4K buffer
			int bytesRead;
			while ((bytesRead = in.read(buf)) != -1) {
				out.write(buf, 0, bytesRead);
			}
		}
		catch( Exception e) {
			logger.error("Error while sending the file " + filename + " to the client",e);
		}
		finally {
			if (in != null) {
				try {
					in.close(  );
				}
				catch( IOException ioe) {
					logger.error("close error",ioe);
				}
			}
		}
	}
	
	/**
	 * Sends a message over a socket to the Univ-R AV client
	 * @param message the message to send
	 * @param ip the ip to contact the client
	 * @param port the port to contact the client
	 * @param timeout the timeout to contact the client
	 * @return the answer of the client
	 */
	public String sendMessageToClient(String message, String ip, int port, int timeout) {
		
		String answer = "";
		
		try {
			/* Sends the message to the client */
		//	Socket client = new Socket(ip, port);
			SocketAddress sockaddr = new InetSocketAddress(ip, port);
			Socket client = new Socket();
			client.connect(sockaddr, timeout);
				
			PrintWriter output = new PrintWriter(client.getOutputStream());
			output.print(message);
			output.flush();
			/* Reception of the answer */
			BufferedReader entree = new BufferedReader(new InputStreamReader(client.getInputStream()));
			answer = entree.readLine();
			client.close();
		}
		catch( IOException ioe) {
			logger.error("Error while sending the message to the client",ioe);
		}
		
		return answer;
	}
	
	/**
	 * Creates a RSS files for a list of courses
	 * @param courses the list of courses
	 * @param filePath the full path of the RSS file to create
	 * @param rssName the name of the RSS files
	 * @param rssTitle the title of the RSS file
	 * @param rssDescription the description of the RSS file
	 * @param serverUrl the URL of the application on the server
	 * @param rssImageUrl the URL of the RSS image file
	 * @param recordedInterfaceUrl the URL of the recorded interface
	 * @param language the language of the RSS file
	 * @param rssCategory the category of the RSS file
	 * @param itunesAuthor The itunes author
	 * @param itunesSubtitle The itunes subtitle
	 * @param itunesSummary The itunes summary
	 * @param itunesImage The itunes image
	 * @param itunesCategory The itunes category
	 * @param itunesKeywords The itunes keywords
	 */
	public void rssCreation( List<Course> courses, String filePath, String rssName, 
			String rssTitle, String rssDescription, String serverUrl, String rssImageUrl, 
			String recordedInterfaceUrl, String language, String rssCategory, String itunesAuthor,
			String itunesSubtitle, String itunesSummary, String itunesImage, String itunesCategory, String itunesKeywords) {
		
		try {		
			// Création d'un nouveau DOM
	        DocumentBuilderFactory fabrique = DocumentBuilderFactory.newInstance();
	        DocumentBuilder constructeur = fabrique.newDocumentBuilder();
	        Document document = constructeur.newDocument();
	        
	        // Propriétés du DOM
	        document.setXmlVersion("1.0");
	        document.setXmlStandalone(true);
	        
	        // Création de l'arborescence du DOM
	        Element racine = document.createElement("rss");
	        racine.setAttribute("xmlns:itunes", "http://www.itunes.com/dtds/podcast-1.0.dtd");
	        racine.setAttribute("version", "2.0");
	        
	        Element channel = document.createElement("channel");
	        racine.appendChild(channel);
	        
	        // Ajout des informations sur le flux
	        
	        Element title = document.createElement("title");
	        title.setTextContent(rssTitle);
	        channel.appendChild(title);
	        
	        Element itAuthor = document.createElement("itunes:author");
	        itAuthor.setTextContent(itunesAuthor);
	        channel.appendChild(itAuthor);
	        
	        Element link = document.createElement("link");
	        link.setTextContent(serverUrl);
	        channel.appendChild(link);
	        
	        Element description = document.createElement("description");
	        description.setTextContent(rssDescription);
	        channel.appendChild(description);
	        
	        Element itSubtitle = document.createElement("itunes:subtitle");
	        itSubtitle.setTextContent(itunesSubtitle);
	        channel.appendChild(itSubtitle);
	        
	        Element iSummary = document.createElement("itunes:summary");
	        iSummary.setTextContent(itunesSummary);
	        channel.appendChild(iSummary);
	        
	        Element lang = document.createElement("language");
	        lang.setTextContent(language);
	        channel.appendChild(lang);
	        
	        Element cr = document.createElement("copyright");
	        cr.setTextContent(comment);
	        channel.appendChild(cr);
	        
	        // Ajout d'une image au flux RSS
	        Element image = document.createElement("image");
	        channel.appendChild(image);
	        
	        Element imageTitle = document.createElement("title");
	        imageTitle.setTextContent(rssTitle);
	        image.appendChild(imageTitle);
	        
	        Element urlLink = document.createElement("url");
	        urlLink.setTextContent(rssImageUrl);
	        image.appendChild(urlLink);
	        
	        Element imageLink = document.createElement("link");
	        imageLink.setTextContent(serverUrl);
	        image.appendChild(imageLink);
	        
	        Element itImage = document.createElement("itunes:image");
	        itImage.setAttribute("href",itunesImage);
	        channel.appendChild(itImage);
	        
	        Element itCategory = document.createElement("itunes:category");
	        itCategory.setAttribute("text", itunesCategory);
	        channel.appendChild(itCategory);
	        
	        Element category = document.createElement("category");
	        category.setTextContent(rssCategory);
	        channel.appendChild(category);
	        
	        Element itKeywords = document.createElement("itunes:keywords");
	        itKeywords.setTextContent(itunesKeywords);
	        channel.appendChild(itKeywords);
	        
	        Element itExplicit = document.createElement("itunes:explicit");
	        itExplicit.setTextContent("no");
	        channel.appendChild(itExplicit);
	        
	        // Recherche de cours et création de 2 items pour chaque cours : un audio et un videoslide
			for( Course course : courses) {
				
				if( course.getTitle() != null) {
					// Conversion de la date dans le bon format	
			    	Date d = course.getDate();
			    	SimpleDateFormat sdf = new SimpleDateFormat("EEE', 'dd' 'MMM' 'yyyy' 'HH:mm:ss' 'Z", Locale.US);
		        
			    	// Audio item
			        Element item = document.createElement("item");
			        channel.appendChild(item);
			        
			        Element coursGuid = document.createElement("guid");
			        coursGuid.setTextContent(rssName + "_" + course.getCourseid());
			        coursGuid.setAttribute("isPermaLink","false");
			        item.appendChild(coursGuid);
			        
			        Element coursTitle = document.createElement("title");
			        coursTitle.setTextContent(course.getTitle());
			        item.appendChild(coursTitle);
			        
			        Element coursDescription = document.createElement("description");
			        coursDescription.setTextContent(
			        		(course.getName() != null ? course.getName() : "") 
			        		+ (! (course.getName() == null || course.getFirstname() == null) ? " " : "")
			        		+ (course.getFirstname() != null ? course.getFirstname() : "")
			        		+ (! ((course.getName() == null && course.getFirstname() == null) || course.getFormation() == null) ? " - " : "")
			        		+ (course.getFormation() != null ? course.getFormation() : "")
			        		+ (! ((course.getName() == null && course.getFirstname() == null && course.getFormation() == null) || course.getDescription() == null) ? " : " : "")
			        		+ (course.getDescription() != null ? course.getDescription() : ""));
			        item.appendChild(coursDescription);
			        
			        Element coursCategory = document.createElement("category");
			        coursCategory.setTextContent(course.getType());
			        item.appendChild(coursCategory);
			        
			        Element coursLink = document.createElement("link");
			        coursLink.setTextContent(recordedInterfaceUrl + "?id=" + course.getCourseid() + "&type=flash");
			        item.appendChild(coursLink);
			        
			        Element coursPubDate = document.createElement("pubDate");
			        coursPubDate.setTextContent(sdf.format(d));
			        item.appendChild(coursPubDate);
			        
			        String courseMediaUrl = coursesUrl + course.getMediaFolder() + "/" + course.getMediasFileName();
			        		        
			        if(course.getGenre()==null && !course.isRestrictionuds()) {
			        	if(course.isAvailable("mp3")) {
			        		Element coursEnclosure = document.createElement("enclosure");
			        		coursEnclosure.setAttribute("url",courseMediaUrl + ".mp3");
			        		coursEnclosure.setAttribute("type","audio/mpeg");
			        		coursEnclosure.setAttribute("length", Long.toString(getContentLength(coursesFolder + course.getMediaFolder() + "/" + course.getMediasFileName() + ".mp3")));
			        		item.appendChild(coursEnclosure);
			        	}
			        	if(course.isAvailable("ogg")) {			        		
			        		Element coursEnclosure2 = document.createElement("enclosure");
			        		coursEnclosure2.setAttribute("url",courseMediaUrl + ".ogg");
			        		coursEnclosure2.setAttribute("type","application/ogg");
			        		coursEnclosure2.setAttribute("length", Long.toString(getContentLength(coursesFolder + course.getMediaFolder() + "/" + course.getMediasFileName() + ".ogg")));
			        		item.appendChild(coursEnclosure2);
			        	}
			        	if(course.isAvailable("pdf")) {
			        		Element coursEnclosure3 = document.createElement("enclosure");
			        		coursEnclosure3.setAttribute("url",courseMediaUrl + ".pdf");
			        		coursEnclosure3.setAttribute("type","application/pdf");
			        		coursEnclosure3.setAttribute("length", Long.toString(getContentLength(coursesFolder + course.getMediaFolder() + "/" + course.getMediasFileName() + ".pdf")));
			        		item.appendChild(coursEnclosure3);
			        	}
			        	if(course.isAvailable("zip")) {
			        		Element coursEnclosure4 = document.createElement("enclosure");
			        		coursEnclosure4.setAttribute("url",courseMediaUrl + ".zip");
			        		coursEnclosure4.setAttribute("type","application/zip");
			        		coursEnclosure4.setAttribute("length", Long.toString(getContentLength(coursesFolder + course.getMediaFolder() + "/" + course.getMediasFileName() + ".zip")));
			        		item.appendChild(coursEnclosure4);
			        	}
			        }
			        
			        Element itDuration = document.createElement("itunes:duration");
			        itDuration.setTextContent(course.getDurationStringItunes());
			        item.appendChild(itDuration);
			        
			     // Getting tags of the course
					String tags="";
					List<Tag> listTags = db.getTagsByCourse(course);
					for(int i=0;i<listTags.size();i++) {
							tags=tags + "," + listTags.get(i).getTag();
					}
			        
			        Element itItemKeywords = document.createElement("itunes:keywords");
			        itItemKeywords.setTextContent(itunesKeywords + tags);
			        item.appendChild(itItemKeywords);
			        
			        
			        // VIDEOSLIDE ITEM
			        if(course.isAvailable("videoslide")) {
			        	Element item2 = document.createElement("item");
			        	channel.appendChild(item2);

			        	Element coursGuid2 = document.createElement("guid");
			        	coursGuid2.setTextContent(rssName + "_" + course.getCourseid() + "_vs");
			        	coursGuid2.setAttribute("isPermaLink","false");
			        	item2.appendChild(coursGuid2);

			        	Element coursTitle2 = document.createElement("title");
			        	coursTitle2.setTextContent("Videoslide - " + course.getTitle());
			        	item2.appendChild(coursTitle2);

			        	Element coursDescription2 = document.createElement("description");
			        	coursDescription2.setTextContent(coursDescription.getTextContent());
			        	item2.appendChild(coursDescription2);
			        	
			        	Element coursCategory2 = document.createElement("category");
			        	coursCategory2.setTextContent(coursCategory.getTextContent());
			        	item2.appendChild(coursCategory2);
			        	
			        	Element coursLink2 = document.createElement("link");
			        	coursLink2.setTextContent(recordedInterfaceUrl + "?id=" + course.getCourseid() + "&type=videoslide");
			        	item2.appendChild(coursLink2);
			        	
			        	Element coursPubDate2 = document.createElement("pubDate");
			        	coursPubDate2.setTextContent(coursPubDate.getTextContent());
			        	item2.appendChild(coursPubDate2);
			        	
			        	if(course.getGenre()==null && !course.isRestrictionuds()) {
			        		Element coursEnclosure5 = document.createElement("enclosure");
			        		coursEnclosure5.setAttribute("url",courseMediaUrl + "_videoslide.mp4");
			        		coursEnclosure5.setAttribute("type","video/mp4");
			        		coursEnclosure5.setAttribute("length", Long.toString(getContentLength(coursesFolder + course.getMediaFolder() + "/" + course.getMediasFileName() + "_videoslide.mp4")));
			        		item2.appendChild(coursEnclosure5);
			        	}

			        	Element itDuration2 = document.createElement("itunes:duration");
			        	itDuration2.setTextContent(itDuration.getTextContent());
			        	item2.appendChild(itDuration2);
			        	
			        	Element itItemKeywords2 = document.createElement("itunes:keywords");
			        	itItemKeywords2.setTextContent(itItemKeywords.getTextContent());
			        	item2.appendChild(itItemKeywords2);
			        	
			        }
			        
			        // MP4-HQ-HD ITEM
			        if(course.isAvailable("hq")) {
			        	Element item2 = document.createElement("item");
			        	channel.appendChild(item2);

			        	Element coursGuid2 = document.createElement("guid");
			        	coursGuid2.setTextContent(rssName + "_" + course.getCourseid() + "_hq");
			        	coursGuid2.setAttribute("isPermaLink","false");
			        	item2.appendChild(coursGuid2);

			        	Element coursTitle2 = document.createElement("title");
			        	coursTitle2.setTextContent("Video - " + course.getTitle());
			        	item2.appendChild(coursTitle2);

			        	Element coursDescription2 = document.createElement("description");
			        	coursDescription2.setTextContent(coursDescription.getTextContent());
			        	item2.appendChild(coursDescription2);
			        	
			        	Element coursCategory2 = document.createElement("category");
			        	coursCategory2.setTextContent(coursCategory.getTextContent());
			        	item2.appendChild(coursCategory2);
			        	
			        	Element coursLink2 = document.createElement("link");
			        	coursLink2.setTextContent(recordedInterfaceUrl + "?id=" + course.getCourseid() + "&type=hq");
			        	item2.appendChild(coursLink2);
			        	
			        	Element coursPubDate2 = document.createElement("pubDate");
			        	coursPubDate2.setTextContent(coursPubDate.getTextContent());
			        	item2.appendChild(coursPubDate2);
			        	
			        	if(course.getGenre()==null && !course.isRestrictionuds()) {
			        		Element coursEnclosure5 = document.createElement("enclosure");
			        		coursEnclosure5.setAttribute("url",courseMediaUrl + "_ipod.mp4");
			        		coursEnclosure5.setAttribute("type","video/mp4");
			        		coursEnclosure5.setAttribute("length", Long.toString(getContentLength(coursesFolder + course.getMediaFolder() + "/" + course.getMediasFileName() + ".mp4")));
			        		item2.appendChild(coursEnclosure5);
			        	}

			        	Element itDuration2 = document.createElement("itunes:duration");
			        	itDuration2.setTextContent(itDuration.getTextContent());
			        	item2.appendChild(itDuration2);
			        	
			        	Element itItemKeywords2 = document.createElement("itunes:keywords");
			        	itItemKeywords2.setTextContent(itItemKeywords.getTextContent());
			        	item2.appendChild(itItemKeywords2);
			        	
			        }
				}
			}
		        
			document.appendChild(racine);
	        
			svgXml(document, filePath);
		}
		catch( ParserConfigurationException pce) {
			logger.error("Error while creating the RSS file " + filePath,pce);
		}
	}
	
	/**
	 * Retrieves information about used and free disk space on the server
	 * @return the string containing the info
	 */
	public String getDiskSpaceInfo() {
		
		String result = "";
		
		try {
			/* Execution of the df program */
			Process p = r.exec("df -h");
			if( p.waitFor() != 0) {
				logger.error("Error while getting disk space info");
				throw new DaoException("Error while getting disk space info");
			}
			
			InputStream is = p.getInputStream();
			BufferedReader entree = new BufferedReader(new InputStreamReader(is));
			String text="";
			while( (text = entree.readLine()) != null )
				result+= "\n" + text ;
		}
		catch( Exception e ) {
			logger.error("Error while getting disk space info",e);
		}
		
		return result;
	}
	
	/**
	 * Extracts the course archive file to the courses folder and renames it
	 * @param c the course corresponding to the archive
	 * @param courseArchive the course archive file
	 */
	private static void archiveExtraction(Course c, String courseArchive) {
		String extension;
		
		if( courseArchive.length() > 3) {
			extension = courseArchive.substring(courseArchive.length()-4, courseArchive.length());
			String tmpMediaFolder = courseArchive.substring(0,courseArchive.lastIndexOf(".")); //uncompressed media folder
			String mf=c.getMediaFolder();
			
			// Create all folders			
			File mediaFolder = new File(coursesFolder, mf.substring(0,mf.lastIndexOf('/')));
			mediaFolder.mkdirs();
			
			// Don't extract if the course already exists
			if( ! new File(coursesFolder + mf).exists()) {
				try {					
					/* Archive extraction according to its type to the courses folder */
					if(extension.equals(".zip")){			
						/* Zip extracting */
						Process p = r.exec("unzip -u " + ftpFolder + courseArchive, null, mediaFolder);
						if( p.waitFor() != 0) {
							logger.error("The course archive " + courseArchive + " has not been extracted");
		        			throw new DaoException("The course archive " + courseArchive + " has not been extracted");
						}
					}
					/*else {								
						// Tar extracting
						Process p = r.exec("tar xf " + ftpFolder + courseArchive, null, mediaFolder);
						if( p.waitFor() != 0) {
							logger.error("The course archive " + courseArchive + " has not been extracted");
		        			throw new DaoException("The course archive " + courseArchive + " has not been extracted");
						}
					}*/
						
	        		/* Renaming of the extracted folder to have a unique one */
					Process p = r.exec("mv " + tmpMediaFolder + " " + mf.substring(mf.lastIndexOf('/')+1), null, mediaFolder);
	        		if( p.waitFor() != 0) {
	        			logger.error("The course archive " + courseArchive + " has not been renamed");
	        			throw new DaoException("The course archive " + courseArchive + " has not been renamed");
	        		}
				}
				catch (IOException ioe) {
					logger.error("Error while extracting the course archive",ioe);
				}
				catch (InterruptedException ie) {
					logger.error("Error while extracting the course archive",ie);
				}
			}
			else {
				logger.error("The course folder " + mediaFolder + " already exists");
				throw new DaoException("The course folder " + mediaFolder + " already exists" );
			}
		}
		else {
			logger.error("Incorrect archive file to extract: " + courseArchive );
			throw new DaoException("Incorrect archive file to extract: " + courseArchive );
		}
	}
	
	
	/**
	 * Move the archive into course folder
	 * @param c the course corresponding to the archive
	 * @param courseArchive the course archive file
	 */
	private static void moveArchive(Course c, String courseArchive) {	
		Process p;
		try {
			p = r.exec("cp " + ftpFolder + courseArchive + " "+c.getCourseid()+".zip" , null, new File(coursesFolder + c.getMediaFolder()));
			if( p.waitFor() != 0) {
				logger.error("The course archive " + courseArchive + " has not been renamed");
				throw new DaoException("The course archive " + courseArchive + " has not been renamed");
			}
		} catch (IOException e) {
			logger.error("error during the copy of the archive of course "+c.getCourseid());
		} catch (InterruptedException e) {
			logger.error("error during the copy of the archive of course "+c.getCourseid());
		}
		
	}
	
	
	/**
	 * Sets the "type" attribute of the Course object by identifying the media file
	 * @param c the course
	 */
	private static void setCourseType(Course c) {
		
		if( new File(coursesFolder + c.getMediaFolder() + "/" + defaultMp3File).exists()) 
			c.setType("audio");
		else if(new File(coursesFolder + c.getMediaFolder() + "/" + defaultFlashFile).exists())
			c.setType("video");
		else {
			logger.error("No course media file found in the " + coursesFolder + c.getMediaFolder() + " folder");
			throw new DaoException("No course media file found in the " + coursesFolder + c.getMediaFolder() + " folder");
		}
	}
	
	
	/** 
	 * Renames a file
	 * @param mediaFolder the folder which contains the media files of a course
	 * @param oldFileName the old name of the file
	 * @param newFileName the new name of the file
	 */ 
	private static void renameFile( String mediaFolder, String oldFileName, String newFileName) {
		try {
			Process p = r.exec(new String[]{"mv",oldFileName,newFileName}, null, new File(coursesFolder + mediaFolder));
			if( p.waitFor() != 0 ) {
				logger.error("Error while renaming the file " + oldFileName);
				throw new DaoException("Error while renaming the file " + oldFileName);
			}
		} catch (IOException ioe) {
			logger.error("Error while renaming the file " + oldFileName,ioe);
		}
		catch( InterruptedException ie) {
			logger.error("Error while renaming the file " + oldFileName,ie);
		}
	}
	
	
	/** 
	 * Launches a bash script which inject metadata to flv
	 * @param mediaFolder the folder which contains the media files of a course
	 * @param mediaFileName the name used by all the media files of a course
	 * @param mediaFileExtension the extension used by the media file
	 */
	private static void injectMetadata( String mediaFolder, String mediaFileName, String mediaFileExtension) {
		try {
			Process p = r.exec("bash injectMetadata.sh "  + coursesFolder + mediaFolder + " " + mediaFileName + " " + mediaFileExtension, null, new File(scriptsFolder));
			if( p.waitFor() != 0 ) {
				logger.error("Error while inject metadata to " + mediaFileName + "." + mediaFileExtension);
				throw new DaoException("Error while inject metadata to " + mediaFileName + "." + mediaFileExtension);
			}
		}
		catch(IOException ioe) {
			logger.error("Error while inject metadata to " + mediaFileName + "." + mediaFileExtension,ioe);
		}
		catch(InterruptedException ie) {
			logger.error("Error while inject metadata to " + mediaFileName + "." + mediaFileExtension,ie);
		}
	}
	
	
	/** 
	 * Sets the "duration" attribute of the Course object by identifying the media file
	 * @param c the course
	 * @param mediaFolder the folder which contains the media files of a course
	 * @param mediaFileName the name used by all the media files of a course
	 * @param mediaFileExtension the extension used by the media file
	 */
	private static void setCourseDuration(Course c, String mediaFolder, String mediaFileName, String mediaFileExtension) {
		int duration;
		
		try {
			//Process p = r.exec("mp3info -p %S " +  mediaFileName + ".mp3", null, new File(coursesFolder + mediaFolder));
			Process p = r.exec("bash mediaduration.sh " + coursesFolder + mediaFolder + " " + mediaFileName + " " + mediaFileExtension, null, new File(scriptsFolder));
			if( p.waitFor() != 0 ) {
				logger.error("Error while getting the length of the file " + mediaFileName + "."+mediaFileExtension);
				throw new DaoException("Error while getting the length of the file " + mediaFileName + "."+mediaFileExtension);
			}
			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			duration = Integer.parseInt(br.readLine());
			c.setDuration(duration);
			
		} catch (IOException e) {
			logger.error("Error while getting the length of the file " + mediaFileName + ".mp3",e);
		}
		catch (NumberFormatException nfe) {
			logger.error("Error while getting the length of the file " + mediaFileName + ".mp3",nfe);
		}
		catch( InterruptedException ie) {
			logger.error("Error while getting the length of the file " + mediaFileName + ".mp3",ie);
		}
	}
	
	
	
	/**
	 * To save the xml file of the Rss flux
	 * @param document the document
	 * @param fichier the file
	 */
	private static void svgXml(Document document, String fichier) {
        try {
	        // Create DOM source
	        Source source = new DOMSource(document);
	        
	        // Create output file
	        File file = new File(fichier);
	        Result resultat = new StreamResult(file);
	        
	        // Transformer configuration
	        TransformerFactory fabrique = TransformerFactory.newInstance();
	        Transformer transformer = fabrique.newTransformer();
	        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
	        
	        // Transformation into xml file
	        transformer.transform(source, resultat);
	        
        }
        catch(Exception e){
        	logger.error("save xml error",e);
        }
	}
	
	/**
	 * Returns the content length of a file
	 * @param filePath the path of the file
	 * @return the content length
	 */
	private static long getContentLength(String filePath) {
		File f = new File(filePath);
		return f.length();
	}
	
	/**
	 * Creates the media folder of a course created from a media file
	 * @param c the course
	 * @param mediaFile the media file
	 * @param fileName the file name
	 */
	private static void mediaFolderCreation( Course c, FileItem mediaFile, String fileName) {
						
		File mediaFolder = new File(coursesFolder, c.getMediaFolder());
		mediaFolder.mkdirs();
				
		try {
			mediaFile.write(new File(mediaFolder, fileName));
		}
		catch( Exception e) {
			logger.error("Error while writing the media file " + fileName,e);
		}
	}
	
	
		
	/**
	 * Send an email to confirm the add of the new course 
	 * @param subject the subject of the mail
	 * @param message the message of the mail
	 * @param email the email address
	 */
	public void sendMail(String subject, String message, String email) {
		  
	  String[] command_array = {"bash","mail.sh",message,subject, email};
		
		try {
			Process p = r.exec(command_array, null, new File(scriptsFolder));
						
			if( p.waitFor() != 0 ) {
				logger.error("Error while send the mail to " + email);
				throw new DaoException("Error while send the mail to " + email);
			}
		}
		catch(IOException ioe) {
			logger.error("Error while send the mail to " + email,ioe);
		}
		catch(InterruptedException ie) {
			logger.error("Error while send the mail to " + email,ie);
		}
		
	}
	
	/**
	 * Add an additional document to a course
	 * @param mediafolder the mediafolder
	 * @param docFile the fileitem of the document
	 */
	public void addAdditionalDoc(String mediafolder, FileItem docFile) {
		
		String fileName = docFile.getName();
		
		/* Used to fix full path problem with IE */
		if( fileName.indexOf("\\") != -1 ) {
			fileName = fileName.substring(fileName.lastIndexOf("\\") + 1,fileName.length());
			docFile.setFieldName(fileName);
		}
				
		File docFolder = new File(coursesFolder + mediafolder +"/additional_docs");
		// Create the directory if not exist
		if(!docFolder.exists()) {
			docFolder.mkdir();
		}
		
		try {
			docFile.write(new File(docFolder, fileName));
		}
		catch( Exception e) {
			logger.error("Error while writing the doc file " + fileName,e);
		}		
	}
	
	/**
	 * Delete an additional document of a course
	 * @param mediafolder the mediafolder
	 * @param addDocName name of the additional document
	 */
	public void deleteAdditionalDoc(String mediafolder, String addDocName) {
		
		File addDocFile = new File(coursesFolder + mediafolder +"/additional_docs/" + addDocName);
		File newAddDocFile = new File(coursesFolder + mediafolder +"/additional_docs/" + new Date().getTime() + "." + addDocName);
		addDocFile.renameTo(newAddDocFile);
	}
	
	
	/**
	 * Script to launch medias encodage
	 * @param serverUrl the server url
	 * @param job_line the job line
	 */
	public void launchJob(String serverUrl, String job_line) {
		
		try {	
			String[] cmd = new String[] {"nice", "-n", "19", "bash" , "JobEnc.sh" , scriptsFolder+"/jobs_encodage" , serverUrl, coursesFolder, job_line};		
			Process p = r.exec(cmd, null, new File(scriptsFolder+"/jobs_encodage"));
			
			if( p.waitFor() != 0 ) {
				logger.error("Error while lauching the job");
				throw new DaoException("Error while lauching the job");
			}
		}
		catch(IOException ioe) {
			logger.error("Error while lauching the job ",ioe);
		}
		catch(InterruptedException ie) {
			logger.error("Error while lauching the job ",ie);
		}		
	}
	
}

package org.ulpmm.univrav.service;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.ulpmm.univrav.dao.IDatabase;
import org.ulpmm.univrav.dao.IFileSystem;
import org.ulpmm.univrav.entities.Course;
import org.ulpmm.univrav.entities.Slide;
import org.ulpmm.univrav.entities.Tag;

/**
 * This thread is used to add a course
 * 
 * @author morgan
 *
 */
public class CourseAddition extends Thread {
	
	/** Database interface */
	private IDatabase db;
	
	/** FileSystem interface */
	private IFileSystem fs;
	
	/** The course */
	private Course c;
	
	/** the media's name */
	private String courseArchive;
	
	/** List of tags **/
	private String tags;
	
	/** Service interface */
	private IService service;
		
	/** the url of the server */
	private String serverUrl;
		
	/** true if medias encodage is separated */
	private boolean sepEnc;
	
	/** the course folder */
	private String coursesFolder;

	/**
	 * CourseAddition's constructor
	 * 
	 * @param db Database interface
	 * @param fs FileSystem interface
	 * @param c The course
	 * @param courseArchive the media's name
	 * @param tags tags list
	 * @param service Service interface
	 * @param serverUrl the url of the server
	 * @param sepEnc true if medias encodage is separated
	 * @param coursesFolder the courses folder
	 */
	public CourseAddition(IDatabase db, IFileSystem fs, Course c, String courseArchive, String tags,
			IService service, String serverUrl, boolean sepEnc, String coursesFolder) {
		
		super();
		this.db = db;
		this.fs = fs;
		this.c = c;
		this.courseArchive = courseArchive;
		this.tags=tags;
		this.service = service;
		this.serverUrl = serverUrl;
		this.sepEnc = sepEnc;
		this.coursesFolder = coursesFolder;
	}

	/**
	 * The process to create a course inside a thread
	 */
	public void run() {
		// Create flash course in filesystem
		fs.addCourse(c, courseArchive);
		// add course into database for direct flash access
		db.addCourse(c);
		
		// Adding slides
		ArrayList<String> list = fs.getTimecodes(c.getMediafolder());
		int time;
		/* Determines if the times of the slides must be changed or not */
		if( c.getTiming().equals("n") ) {
			time = 1;
		}
		else if( c.getTiming().equals("n-1") ){
			time = 2;
		}
		else
			time = 2;
		
		for( int i = 0 ; i< list.size() - (time-1) ; i++)
			db.addSlide(new Slide(c.getCourseid(), Math.round(Float.parseFloat(list.get(i)))));
		
		// Adding tags
		if(tags!=null && !tags.equals("")) {
			// ADD TAGS		
			List<String> listTmp=new ArrayList<String>();
			StringTokenizer st = new StringTokenizer(tags," ,;");
			String token = null;
			while (st.hasMoreTokens()) {
				token = st.nextToken();
				if(!listTmp.contains(token)) {
					service.addTag(new Tag(0, //is not used
						token, // the tag
						c.getCourseid()) // the course
					);
					listTmp.add(token);
				}
			}
			listTmp = null;
			st = null;
			token = null;
		}
				
		
		// If medias encodage isnt separated
		if(!sepEnc) {

			String job_line ="";
						
			if(c.getType().equals("video")) {
				// if the flv is not available, it's mp4
				if(!c.isAvailable("flash")) {
					service.createJob(c,Course.typeFlash+Course.typeMp3+Course.typeOgg+Course.typePdf+Course.typeZip+Course.typeVideoslide+Course.typeVideoslideIpod + Course.typeWebm,"CVMP4","mp4",coursesFolder);
					job_line =c.getCourseid()+":"+"waiting"+":"+String.valueOf(Course.typeFlash+Course.typeMp3+Course.typeOgg+Course.typePdf+Course.typeZip+Course.typeVideoslide+Course.typeVideoslideIpod + Course.typeWebm)+":CVMP4:"+c.getMediafolder()+":mp4";
					service.modifyJobStatus(c.getCourseid(), "processing", "CVMP4");
				}
				//else flv
				else {
					service.createJob(c,Course.typeMp3+Course.typeOgg+Course.typePdf+Course.typeZip+Course.typeVideoslide+Course.typeVideoslideIpod + Course.typeWebm,"CV","flv",coursesFolder);
					job_line =c.getCourseid()+":"+"waiting"+":"+String.valueOf(Course.typeMp3+Course.typeOgg+Course.typePdf+Course.typeZip+Course.typeVideoslide+Course.typeVideoslideIpod + Course.typeWebm)+":CV:"+c.getMediafolder()+":flv";
					service.modifyJobStatus(c.getCourseid(), "processing", "CV");
				}
			}
			else {
				service.createJob(c,Course.typeMp3+Course.typeOgg+Course.typePdf+Course.typeZip+Course.typeVideoslide+Course.typeVideoslideIpod + Course.typeWebm,"CA","mp3",coursesFolder);
				job_line =c.getCourseid()+":"+"waiting"+":"+String.valueOf(Course.typeMp3+Course.typeOgg+Course.typePdf+Course.typeZip+Course.typeVideoslide+Course.typeVideoslideIpod + Course.typeWebm)+":CA:"+c.getMediafolder()+":mp3";
				service.modifyJobStatus(c.getCourseid(), "processing", "CA");	
			}
										
			service.launchJob(serverUrl, job_line);		
		}
		else {
			if(c.getType().equals("video")) {
				// if the flv is not available, it's mp4
				if(!c.isAvailable("flash")) {
					service.createJob(c,Course.typeFlash+Course.typeMp3+Course.typeOgg+Course.typePdf+Course.typeZip+Course.typeVideoslide+Course.typeVideoslideIpod + Course.typeWebm,"CVMP4","mp4",coursesFolder);
				}
				//else flv
				else {
					service.createJob(c,Course.typeMp3+Course.typeOgg+Course.typePdf+Course.typeZip+Course.typeVideoslide+Course.typeVideoslideIpod + Course.typeWebm,"CV","flv",coursesFolder);
				}
			}
			else {
				service.createJob(c,Course.typeMp3+Course.typeOgg+Course.typePdf+Course.typeZip+Course.typeVideoslide+Course.typeVideoslideIpod + Course.typeWebm,"CA","mp3",coursesFolder);
			}
		}
	}
	
	
}

package org.ulpmm.univrav.entities;

import java.util.Collections;
import java.util.List;

public class Building {
	
	private int buildingid;
	private String name;
	private String imageFile;
	private List<Amphi> amphis =  null;
	
	/**
	 * @param buildingid
	 * @param name
	 * @param imageFile
	 */
	public Building(int buildingid, String name, String imageFile) {
		super();
		this.buildingid = buildingid;
		this.name = name;
		this.imageFile = imageFile;
	}

	/**
	 * @return the buildingid
	 */
	public int getBuildingid() {
		return buildingid;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the imageFile
	 */
	public String getImageFile() {
		return imageFile;
	}

	/**
	 * @return the amphis sort by name
	 */
	public List<Amphi> getAmphis() {
		Collections.sort(amphis);
		return amphis;
	}

	/**
	 * @param amphis the amphis to set
	 */
	public void setAmphis(List<Amphi> amphis) {
		this.amphis = amphis;
	}
	
	/**
	 * returns a String representation of this object
	 */
	public String toString() {
		return this.name;
	}
	
	
	
	
	
	
}

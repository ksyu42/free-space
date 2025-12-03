package com.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

/*
 * 座席の時間
 */
@Entity
@Table(name = "space_times")
public class SpaceTime {

	@Id
	@Column(name = "space_times_id")
	private int spaceTimesId;
	
	@Column
	private String time;
	
	@Transient
	private boolean hasVacancy;
	
	@Transient
	private int seatCount;


	
	public int getSpaceTimesId() {
		return spaceTimesId;
	}

	public void setSpaceTimesId(int spaceTimesId) {
		this.spaceTimesId = spaceTimesId;
	}


	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}
	
	public boolean isHasVacancy() {
		return hasVacancy;
	}
	
	public void setHasVacancy(boolean hasVacancy) {
		this.hasVacancy = hasVacancy;
	}

	public int getSeatCount() {
		return seatCount;
	}

	public void setSeatCount(int seatCount) {
		this.seatCount = seatCount;
	}
	
	
}

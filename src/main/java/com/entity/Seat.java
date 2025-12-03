package com.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/*
 * 座席数
 */
@Entity
@Table(name = "space_seats")
public class Seat {
	
	@Id
	@Column(name = "space_seat_id")
	private int spaceSeatId;

	@Column(name = "seat_count")
	private int seatCount;

	@Column(name = "space_id")
	private int spaceId;
	
	@Column(name = "space_times_id")
	private int spaceTimesId;


	public int getSpaceSeatId() {
		return spaceSeatId;
	}

	public void setSpaceSeatId(int spaceSeatId) {
		this.spaceSeatId = spaceSeatId;
	}

	public int getSeatCount() {
		return seatCount;
	}

	public void setSeatCount(int seatCount) {
		this.seatCount = seatCount;
	}

	public int getSpaceId() {
		return spaceId;
	}

	public void setSpaceId(int spaceId) {
		this.spaceId = spaceId;
	}

	public int getSpaceTimesId() {
		return spaceTimesId;
	}

	public void setSpaceTimesId(int spaceTimesId) {
		this.spaceTimesId = spaceTimesId;
	}

	


}

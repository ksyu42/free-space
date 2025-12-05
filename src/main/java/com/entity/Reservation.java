package com.entity;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "user_id")
    private int userId;

    @Column(name = "space_id")
    private int spaceId;

    @Column(name = "space_times_id")
    private int spaceTimesId;

    @Column(name = "reservation_day")
    private LocalDate reservationDay;

    @PrePersist
    public void onPrePersist() {
        this.reservationDay = LocalDate.now();
    }

    @Transient
    private String spaceName;

    @Transient
    private String location;

    @Transient
    private String time;

    // ------ Getter / Setter ------
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getSpaceId() { return spaceId; }
    public void setSpaceId(int spaceId) { this.spaceId = spaceId; }

    public int getSpaceTimesId() { return spaceTimesId; }
    public void setSpaceTimesId(int spaceTimesId) { this.spaceTimesId = spaceTimesId; }

    public LocalDate getReservationDay() { return reservationDay; }
    public void setReservationDay(LocalDate reservationDay) { this.reservationDay = reservationDay; }

    public String getSpaceName() { return spaceName; }
    public void setSpaceName(String spaceName) { this.spaceName = spaceName; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
}

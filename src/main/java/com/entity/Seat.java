package com.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/*
 * 座席数
 */
@Entity
@Table(name = "space_seats")
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "space_seat_id")
    private Integer spaceSeatId;

    @Column(name = "seat_count", nullable = false)
    private Integer seatCount = 0;  // ← デフォルト必須!!

    @Column(name = "space_id", nullable = false)
    private Integer spaceId;

    @Column(name = "space_times_id", nullable = false)
    private Integer spaceTimesId;

    public Seat() {
        this.seatCount = 0;  // ← ここも重要
    }

    // --- 以下 getter/setter 全部再生成（重要） ---
    public Integer getSpaceSeatId() { return spaceSeatId; }
    public void setSpaceSeatId(Integer id) { this.spaceSeatId = id; }

    public Integer getSeatCount() { return seatCount; }
    public void setSeatCount(Integer seatCount) { this.seatCount = seatCount; }

    public Integer getSpaceId() { return spaceId; }
    public void setSpaceId(Integer id) { this.spaceId = id; }

    public Integer getSpaceTimesId() { return spaceTimesId; }
    public void setSpaceTimesId(Integer id) { this.spaceTimesId = id; }
}


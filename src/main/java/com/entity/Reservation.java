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

/*
 * 予約情報
 * 
 * status：
 *  PENDING   … 申請中（仮押さえ）
 *  APPROVED  … 承認済み
 *  REJECTED  … 却下
 *  CANCELLED … キャンセル
 *  USED      … 利用済み（レビュー可能）
 */
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

    @Column(name = "status", nullable = false)
    private String status = "PENDING"; // PENDING / APPROVED / REJECTED / CANCELLED / USED

    /*
     * 予約日が未設定のまま保存されるのを防ぐ（保険）
     */
    @PrePersist
    public void prePersist() {
        if (this.reservationDay == null) {
            this.reservationDay = LocalDate.now();
        }
        if (this.status == null || this.status.isBlank()) {
            this.status = "PENDING";
        }
    }

    /* ===========================
     * 画面表示用（DBには保存しない）
     * =========================== */
    @Transient
    private String spaceName;

    @Transient
    private String location;

    @Transient
    private String time;

    @Transient
    private String userName;

    @Transient
    private boolean reviewed; // レビュー済みフラグ

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

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isReviewed() { return reviewed; }
    public void setReviewed(boolean reviewed) { this.reviewed = reviewed; }
}

package com.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

/*import jakarta.persistence.*;*/
import lombok.Data;

@Entity
@Table(name = "spaces")
@Data
public class Space {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "location", nullable = false)
    private String location;

    @Column(name = "available_from", nullable = false)
    private String availableFrom;

    @Column(name = "available_to", nullable = false)
    private String availableTo;

    @Column(name = "seat_count", nullable = false)
    private Integer seatCount;

    @Column(name = "admin_id", nullable = false)
    private Long adminId;

    @Transient
    private String availableTime;

    public String getAvailableTime() {
        if (this.availableTime != null && !this.availableTime.isBlank()) {
            return this.availableTime;
        }
        String from = this.availableFrom == null ? "" : this.availableFrom;
        String to = this.availableTo == null ? "" : this.availableTo;
        return from + "〜" + to;
    }

    public void setAvailableTime(String availableTime) {
        this.availableTime = availableTime;
        if (availableTime == null) return;
        if (availableTime.contains("〜")) {
            String[] parts = availableTime.split("〜", 2);
            this.availableFrom = parts[0].trim();
            this.availableTo = parts[1].trim();
        }
    }
}

package com.form;

public class TimeSeatDto {
    private String time;
    private int seatCount;

    public TimeSeatDto(String time, int seatCount) {
        this.time = time;
        this.seatCount = seatCount;
    }

    public String getTime() {
        return time;
    }

    public int getSeatCount() {
        return seatCount;
    }
}

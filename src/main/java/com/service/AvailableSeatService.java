package com.service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.entity.Seat;
import com.repository.ReservationRepository;
import com.repository.SeatRepository;

/**
 * 残席数を計算するサービスクラス
 * 
 * ・seat テーブルの seat_count ＝ 最大席数
 * ・reservations の件数 ＝ 予約された席数
 *
 * 残席 = seat_count - reservations_count
 */
@Service
public class AvailableSeatService {

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    /**
     * 指定スペース × 指定時間帯 の残席数を取得
     *
     * @param spaceId      スペースID
     * @param spaceTimesId 時間帯ID
     * @return 残席数（最低0）
     */
    public int getAvailableSeats(int spaceId, int spaceTimesId, LocalDate reservationDay) {

        Seat seat = seatRepository.findBySpaceIdAndSpaceTimesId(spaceId, spaceTimesId);
        if (seat == null) {
            return 0;
        }

        int maxSeats = seat.getSeatCount(); // 最大席数

        // 空席計算に含める status（申請中も枠を確保したいので PENDING もカウント）
        List<String> targetStatus = Arrays.asList("PENDING", "APPROVED");

        long reserved = reservationRepository.countBySpaceIdAndSpaceTimesIdAndReservationDayAndStatusIn(
                spaceId,
                spaceTimesId,
                reservationDay,
                targetStatus
        );

        int available = maxSeats - (int) reserved;
        return Math.max(available, 0);
    }

    /**
     * 既存呼び出し互換用（reservationDay 未指定なら今日で計算）
     */
    public int getAvailableSeats(int spaceId, int spaceTimesId) {
        return getAvailableSeats(spaceId, spaceTimesId, LocalDate.now());
    }
}

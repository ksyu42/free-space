package com.service;

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
    public int getAvailableSeats(int spaceId, int spaceTimesId) {

        // 座席マスタ seat_count を取得
        Seat seat = seatRepository.findBySpaceIdAndSpaceTimesId(spaceId, spaceTimesId);

        if (seat == null) {
            // その時間帯に席自体が存在しない → 残席0扱い
            return 0;
        }

        int maxSeats = seat.getSeatCount(); // 最大席数

        // 予約済み数を取得
        long reserved = reservationRepository.countBySpaceIdAndSpaceTimesId(spaceId, spaceTimesId);

        // 残席数 = 最大席数 - 予約数
        int available = maxSeats - (int) reserved;

        return Math.max(available, 0); // 最低0
    }
}

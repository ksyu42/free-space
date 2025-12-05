package com.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.entity.Seat;
import com.repository.ReservationRepository;
import com.repository.SeatRepository;

/**
 * 残席数を計算するサービスクラス
 * 
 * seat_count（座席マスタ） － 予約済み件数（reservations）
 * の計算を行い、残席数を返却します。
 */
@Service
public class AvailableSeatService {

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    /**
     * 指定スペース × 指定時間帯 の残席数を返す
     *
     * @param spaceId       スペースID
     * @param spaceTimesId  時間帯ID
     * @return 残席数（最低 0）
     */
    public int getAvailableSeats(int spaceId, int spaceTimesId) {

        // (1) seat テーブルから seat_count を取得
        Seat seat = seatRepository.findBySpaceIdAndSpaceTimesId(spaceId, spaceTimesId);

        if (seat == null) {
            // seat レコードが存在しない → 対象の時間帯に席がない = 空席 0
            return 0;
        }

        int seatCount = seat.getSeatCount();  // 最大席数

        // (2) Reservation から既に予約されている件数を取得
        long reservedCount = reservationRepository.countBySpaceIdAndSpaceTimesId(spaceId, spaceTimesId);

        // (3) 残席数 = seat_count - reservedCount
        int available = seatCount - (int) reservedCount;

        // (4) 残席数が 0 未満にならないように補正
        return Math.max(available, 0);
    }
}

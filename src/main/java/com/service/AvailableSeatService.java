package com.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.entity.Seat;
import com.repository.SeatRepository;

/**
 * 残席数を取得するサービスクラス
 * 
 * 仕様（このプロジェクトの運用）：
 * ・space_seats.seat_count は「最大席数」ではなく「残席」として扱う
 * ・予約作成（PENDING）時点で seat_count を 1 減らす
 * ・予約却下 / キャンセル時は seat_count を 1 増やして戻す
 * ・予約承認（APPROVED）はステータス更新のみ（座席数は動かさない）
 */
@Service
public class AvailableSeatService {

    @Autowired
    private SeatRepository seatRepository;

    /**
     * 指定スペース・時間帯の残席数を返す
     */
    public int getAvailableSeats(int spaceId, int spaceTimesId) {

        Seat seat = seatRepository.findBySpaceIdAndSpaceTimesId(spaceId, spaceTimesId);

        // seat 情報が存在しない場合は 0 とする
        if (seat == null || seat.getSeatCount() == null) {
            return 0;
        }

        return Math.max(seat.getSeatCount(), 0);
    }
}

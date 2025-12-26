package com.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.entity.Reservation;

/**
 * 予約テーブル用リポジトリ
 */
public interface ReservationRepository extends JpaRepository<Reservation, Integer>{

	List<Reservation> findByUserId(int userId);

    /*
     * 管理者側で「申請中だけ抽出」などに利用
     */
    List<Reservation> findByStatus(String status);

    /*
     * 管理者のスペース一覧（自分のスペースID群）から予約を取る
     */
	List<Reservation> findBySpaceIdIn(List<Integer> mySpaceIds);

    List<Reservation> findBySpaceId(int spaceId);

    void deleteBySpaceId(int spaceId);

    /*
     * 空席確認用：指定日の予約件数（PENDING/APPROVEDなど）を取る
     * 
     * ※seat_count を「残席」として運用する場合は通常不要だが、
     * 　念のため二重チェック用に残しておく（画面の表示整合性向け）
     */
    long countBySpaceIdAndSpaceTimesIdAndReservationDayAndStatusIn(
            int spaceId,
            int spaceTimesId,
            LocalDate reservationDay,
            List<String> statuses
    );
}

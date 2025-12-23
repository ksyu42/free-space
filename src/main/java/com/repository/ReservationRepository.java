package com.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.entity.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Integer>{
	
    // 日付＋status を加味して予約数を数える（空席計算用）
    long countBySpaceIdAndSpaceTimesIdAndReservationDayAndStatusIn(
            int spaceId,
            int spaceTimesId,
            LocalDate reservationDay,
            List<String> statusList
    );

	List<Reservation> findByUserId(int userId);
	
	//空席数計算のために追加
    long countBySpaceIdAndSpaceTimesId(int spaceId, int spaceTimesId);
    
    //管理者側で「申請中だけ抽出」
    List<Reservation> findByStatus(String status);

	List<Reservation> findBySpaceIdIn(List<Integer> mySpaceIds);

    
    

}

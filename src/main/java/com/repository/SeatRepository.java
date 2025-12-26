package com.repository;

import javax.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.entity.Seat;

/*
 * Seat(座席数)テーブル用リポジトリ
 * 
 * ※空席確認機能（同時予約の二重取り防止）のため、PESSIMISTIC_WRITE でロックするメソッドを追加
 */
public interface SeatRepository extends JpaRepository<Seat, Integer>{

    Seat findBySpaceIdAndSpaceTimesId(int spaceId, int spaceTimesId);

    /*
     * 同時予約対策：対象の seat 行をロックして取得する
     * 
     * 【修正点】
     * メソッド名の末尾 "ForUpdate" を Spring Data がプロパティとして解釈してしまい、
     * 「spaceTimesIdForUpdate」という存在しないプロパティを探して起動エラーになっていた。
     * 
     * 対策として @Query を明示し、派生クエリ解析をさせないようにする。
     * その上で @Lock(PESSIMISTIC_WRITE) により、対象行を更新ロックする。
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Seat s WHERE s.spaceId = :spaceId AND s.spaceTimesId = :spaceTimesId")
    Seat findBySpaceIdAndSpaceTimesIdForUpdate(
            @Param("spaceId") int spaceId,
            @Param("spaceTimesId") int spaceTimesId
    );

    @Transactional
    void deleteBySpaceId(int id);

}

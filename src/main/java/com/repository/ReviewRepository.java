package com.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.entity.Review;

/**
 * レビューテーブル用リポジトリ
 */
public interface ReviewRepository extends JpaRepository<Review, Integer> {

    Optional<Review> findByReservationId(int reservationId);

    List<Review> findBySpaceId(int spaceId);

    void deleteBySpaceId(int spaceId);

    @Query("select avg(r.rating) from Review r where r.spaceId = ?1")
    Double avgRatingBySpaceId(int spaceId);

    @Query("select count(r) from Review r where r.spaceId = ?1")
    Long countBySpaceId(int spaceId);
}

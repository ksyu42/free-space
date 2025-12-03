package com.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import com.entity.Seat;

public interface SeatRepository extends JpaRepository<Seat, Integer>{

    Seat findBySpaceIdAndSpaceTimesId(int spaceId, int spaceTimesId);

    @Transactional
	void deleteBySpaceId(int id);

}

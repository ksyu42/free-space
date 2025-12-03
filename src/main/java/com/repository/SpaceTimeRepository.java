package com.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import com.entity.SpaceTime;


public interface SpaceTimeRepository extends JpaRepository<SpaceTime, Integer>{

	

}

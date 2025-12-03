package com.repository;



import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.entity.Master;


@Repository
public interface MasterRepository extends JpaRepository<Master, Integer>{

	//findAllは書かなくても取得可能
	//List<Master> findAll(String string);

	Master findByName(String string);
}


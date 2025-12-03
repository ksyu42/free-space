package com.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.entity.User;
import com.entity.UserMiddle;

@Repository
public interface UserMiddleRepository extends JpaRepository<UserMiddle, Integer>{

	void deleteByPersonId(UserMiddle usermdl);
	@Transactional
	void deleteByPersonId(Integer id);

	List<UserMiddle> findByPersonId(Integer id);

	UserMiddle findBylanguageId(Integer id);

}

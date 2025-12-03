package com.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.entity.UserLanguage;
import com.entity.UserMiddle;

@Repository
public interface UserLanguageRepository extends JpaRepository<UserLanguage, Integer>{

	List<UserLanguage> findByLanguage(UserMiddle userMiddle);



	


	
}
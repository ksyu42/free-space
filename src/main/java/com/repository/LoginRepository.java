package com.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.entity.Login;

/**
 * ログインテーブル用リポジトリ
 */

@Repository
public interface LoginRepository extends JpaRepository<Login, Integer>{
	
/* SELECT */
	
	public boolean existsByName(String name);
	
	public boolean existsByMail(String mail);
	
	public boolean existsByPass(String pass);

	public boolean existsByMailAndPass(String val1, String val2);
	
	Login findByMailAndPass(String mail, String pass);

	
	
}
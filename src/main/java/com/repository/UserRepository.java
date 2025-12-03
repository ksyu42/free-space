package com.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Repository;

import com.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Integer>{

	
	//新規登録DB突合チェック
	boolean existsByNameAndMailAndPassword(String nameVal, String mailVal, String passwordVal);
	//新規登録DBメール多重チェック
	boolean existsByMail(String mailVal);
	//更新時DBパスワードレコード取得
	User findByMail(User user);
	Optional<User> findByMail(String mail);
	//言語を大文字に表示する(DBストアドプロシージャ)呼び出し
//	@Procedure(name = "user_change_language")
//	void  user_change_language();

	//言語を小文字に表示する(DBストアドプロシージャ)呼び出し
	@Procedure(name = "user_return_language")
	void user_return_language();
}
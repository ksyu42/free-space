package com.repository;




import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.stereotype.Repository;

import com.entity.User;


@Repository
public interface UserCheckLanguageRepository extends JpaRepository<User, Integer> {
	
//	/* 言語をチェックしていない人数(DBストアドプロシージャ)呼び出し　プロシージャの引数なしで実装 */
//	@Query(value = "call user_check_language()", nativeQuery = true)
//	Integer sampleMethod();
	/* 上記同内容　@Procedureを用いてプロシージャに引数(OUT~)で設定 */
	@Procedure(procedureName = "user_check_language")
	Integer callPlus1InOut();
	
}

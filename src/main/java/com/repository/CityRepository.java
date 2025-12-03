package com.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.entity.City;

/**
 * Cityテーブル用リポジトリ
 */

@Repository
public interface CityRepository extends JpaRepository<City, Integer>{
	
/* SELECT */
	
	/**
	 * カラム[Name]で、単一レコード取得
	 * @param name 街名
	 * @return 街情報クラス
	 */
	public City findByName(String name);
	/**
	 * カラム[Countrycode]で、複数レコード取得
	 * @param countrycode コード
	 * @return 街情報クラス
	 */
	public List<City> findByCountrycode(String countrycode);
	/**
	 * カラム[Name]で、レコード存在可否を取得
	 * @param name　街名
	 * @return 街情報クラス
	 */
	public boolean existsByName(String name);
}
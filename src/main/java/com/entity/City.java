package com.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 街情報クラス
 */
@Entity
@Table(name = "city")
public class City{
	
	// ID
	@Id
	@Column
	private int id;
	// 街名
	@Column
	private String name;
	// コード
	@Column
	private String countrycode;
	// 区域
	@Column
	private String district;
	// 人口
	@Column
	private int population;
    // メモ
	@Column
	private String memo;
	
	// Getter・Setter
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCountrycode() {
		return countrycode;
	}
	public void setCountrycode(String countrycode) {
		this.countrycode = countrycode;
	}
	public String getDistrict() {
		return district;
	}
	public void setDistrict(String district) {
		this.district = district;
	}
	public int getPopulation() {
		return population;
	}
	public void setPopulation(int population) {
		this.population = population;
	}
	public String getMemo() {
		return memo;
	}
	public void setMemo(String memo) {
		this.memo = memo;
	}
}
package com.entity;



import javax.persistence.Column;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedStoredProcedureQuery;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@NamedStoredProcedureQuery(name = "user_change_language", procedureName = "user_change_language")
@Entity
@Table(name = "user_person")
public class User {
	
	@Id
	@Column
	private Integer id;
	private String name;
	private String mail;
	private String password;
	private Integer team_id;
	@Transient
	private String language;
	
	
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	public Integer getTeam_id() {
		return team_id;
	}
	public void setTeam_id(Integer team_id) {
		this.team_id = team_id;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getMail() {
		return mail;
	}
	public void setMail(String mail) {
		this.mail = mail;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}




}

package com.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Id;
import javax.persistence.Column;


/*
 * ログイン情報クラス
 */

@Entity
@Table(name = "user")
public class Login{
	
	@Id
	@Column
	private int ID;
	
	@Column
	private String name;
	
	@Column
	private String mail;
	
	@Column 
	private String pass;

	public int getID() {
		return ID;
	}

	public void setID(int iD) {
		ID = iD;
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

	public String getPass() {
		return pass;
	}

	public void setPass(String pass) {
		this.pass = pass;
	}
	
	
	
	
}



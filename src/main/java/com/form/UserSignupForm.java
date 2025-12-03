package com.form;

import java.util.List;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

public class UserSignupForm {

	//新規登録画面　値
	private Integer id;
	
	@NotEmpty(message= "※氏名を入力してください")
	@Size(max= 10, message="※氏名：10文字以内の入力をしてください")
	private String name;
	
	@NotEmpty(message="※メールアドレスを入力してください")
	@Email(message="※メールアドレスのフォーマットが正しくありません")
	@Size(max= 20, message="※メールアドレス：20文字以内の入力をしてください")	
	private String mail;
	
	@Size(max= 10, message="※パスワード：10文字以内の入力をしてください")
	private String password;
	
	@Size(max= 10, message="※パスワード：10文字以内の入力をしてください")
	private String password2;
	
	private Integer team_id;
	//チェックボックス複数受け取り用
	private List<Integer>programList;
	

	public List<Integer> getProgramList() {
		return programList;
	}
	public void setProgramList(List<Integer> programList) {
		this.programList = programList;
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
	public String getPassword2() {
		return password2;
	}
	public void setPassword2(String password2) {
		this.password2 = password2;
	}
	
	
}

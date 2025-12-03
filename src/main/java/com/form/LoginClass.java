package com.form;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

public class LoginClass {
	
	@NotEmpty(message="※メールアドレスを入力してください")
	@Email(message="※メールアドレスのフォーマットが正しくありません")
	@Size(max= 20, message="※メールアドレス：20文字以内の入力をしてください")
	private String userMail;
	
	@NotEmpty(message= "※パスワードを入力してください")
	@Size(max= 10, message="※パスワード：10文字以内の入力をしてください")
	private String userPass;

	
	public String getUserMail() {
		return userMail;
	}
	public void setUserMail(String userMail) {
		this.userMail = userMail;
	}
	
	public String getUserPass() {
		return userPass;
	}
	public void setUserPass(String userPass) {
		this.userPass = userPass;
	}

}
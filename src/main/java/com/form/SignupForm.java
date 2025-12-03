package com.form;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;




public class SignupForm {
	
	private Integer id;

	@NotEmpty(message="※技術名を入力してください")
	@Size(max= 5, message="※技術名：5文字以内の入力をしてください")
	private String skills;
	
	@NotEmpty(message="※オプションを入力してください")
	@Size(max= 5, message="※オプション：5文字以内の入力をしてください")
	private String option;
	
	private String name;
	
	private String option_1;
	

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getOption_1() {
		return option_1;
	}

	public void setOption_1(String option_1) {
		this.option_1 = option_1;
	}

	public String getSkills() {
		return skills;
	}

	public void setSkills(String skills) {
		this.skills = skills;
	}

	public String getOption() {
		return option;
	}

	public void setOption(String option) {
		this.option = option;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}


}

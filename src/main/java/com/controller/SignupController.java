	package com.controller;

import java.util.ArrayList;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.entity.Master;
import com.form.LoginClass;
import com.form.SignupForm;
import com.repository.LoginRepository;
import com.repository.MasterRepository;

@Controller
public class SignupController {
	
	@RequestMapping("/signup")
	public String disp(Model model, 
			@ModelAttribute("sign")SignupForm signupform) {

		model.addAttribute("id", signupform.getId());
		return "signup";
	}

	@RequestMapping("/sign")
	public String sign(Model model, 
			@Validated 
			@ModelAttribute("sign")SignupForm signupform,
			BindingResult result) {

		String val1 = signupform.getSkills();
		String val2 = signupform.getOption();
		
		List<String>errorlist = new ArrayList<String>();
		
		if(result.hasErrors()) {
            List<String> errorList = new ArrayList<String>();
            for (ObjectError error : result.getAllErrors()) {
                errorList.add(error.getDefaultMessage());
            }
            model.addAttribute("validationError", errorList);
            model.addAttribute("id", signupform.getId());
            return "signup";
		}


		return "redirect:/master";
	}	
}
	

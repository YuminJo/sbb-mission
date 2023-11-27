package com.mysite.sbb.user;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

	private final UserService userService;

	@GetMapping("/signup")
	public String signup(UserCreateForm userCreateForm) {
		return "signup_form";
	}

	@PostMapping("/signup")
	public String signup(Model model,@Valid UserCreateForm userCreateForm, BindingResult bindingResult) {
		if(bindingResult.hasErrors()) {
			return "signup_form";
		}

		if(!userCreateForm.getPassword1().equals(userCreateForm.getPassword2())) {
			bindingResult.rejectValue("password2", "passwordInCorrect",
				"2개의 패스워드가 일치하지 않습니다.");
			return "signup_form";
		}

		try {
			userService.create(userCreateForm.getUsername(),
				userCreateForm.getEmail(), userCreateForm.getPassword1());

			//생략해도됨 오류 거슬려서 추가한것
			model.addAttribute("userCreateForm", userCreateForm);

		}catch(
	DataIntegrityViolationException e) {
		e.printStackTrace();
		bindingResult.reject("signupFailed", "이미 등록된 사용자입니다.");
		return "signup_form";
	}catch(Exception e) {
		e.printStackTrace();
		bindingResult.reject("signupFailed", e.getMessage());
		return "signup_form";
	}

		return "redirect:/";
	}

	@GetMapping("/login")
	public String login() {
		return "login_form";
	}
}

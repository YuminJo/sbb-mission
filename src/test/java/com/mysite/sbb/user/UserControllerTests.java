package com.mysite.sbb.user;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.mysite.sbb.SecurityConfig;

import jakarta.transaction.Transactional;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
public class UserControllerTests {
	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private UserService userService;

	@Test
	@DisplayName("[/user/signup] 접속")
	public void connect_signup() throws Exception {
		mockMvc.perform(get("/user/signup"))
			.andExpect(status().isOk())
			.andExpect(view().name("signup_form"))
			.andDo(print());
	}

	@Test
	@DisplayName("[/user/signup] 회원가입")
	public void connect_trysignup() throws Exception {
		MockHttpServletRequestBuilder requestBuilder = post("/user/signup")
			.contentType(MediaType.APPLICATION_FORM_URLENCODED)
			.param("username", "fakeidab")
			.param("password1", "fakepw12")
			.param("password2", "fakepw12")
			.param("email", "Fakeemail@gmail.com")
			.with(csrf());

		mockMvc.perform(requestBuilder)
			.andExpect(status().isFound())
			.andExpect(redirectedUrl("/"))
			.andDo(print());
	}

	@Test
	@DisplayName("[/user/signup] 회원가입 비밀번호 확인 Blank")
	public void connect_tryblanksignup() throws Exception {
		MockHttpServletRequestBuilder requestBuilder = post("/user/signup")
			.contentType(MediaType.APPLICATION_FORM_URLENCODED)
			.param("username", "fakeidab")
			.param("password1", "fakepw12")
			.param("email", "Fakeemail@gmail.com")
			.with(csrf());

		mockMvc.perform(requestBuilder)
			.andExpect(status().isOk())
			.andExpect(view().name("signup_form"))
			.andDo(print());
	}

	@Test
	@DisplayName("[/user/signup] 중복 회원가입 방지")
	public void connect_duplicatesignup() throws Exception {
		// 첫 번째 회원가입 요청
		MockHttpServletRequestBuilder requestBuilder = post("/user/signup")
			.contentType(MediaType.APPLICATION_FORM_URLENCODED)
			.param("username", "fakeidaba")
			.param("password1", "fakepw12")
			.param("password2", "fakepw12")
			.param("email", "fakeemail@gmail.com")
			.with(csrf());

		mockMvc.perform(requestBuilder)
			.andExpect(status().isFound())
			.andExpect(redirectedUrl("/"));

		// userService.create가 DataIntegrityViolationException을 던지도록 설정
		doThrow(DataIntegrityViolationException.class).when(userService).create(any(), any(), any());

		// 두 번째 중복 회원가입 요청
		mockMvc.perform(get("/user/signup")
				.param("username", "fakeidaba")
				.param("password1", "fakepw12")
				.param("password2", "fakepw12")
				.param("email", "fakeemail@gmail.com")
			.with(csrf()))
			.andExpect(status().isOk()) // 중복 회원가입 실패로 예상되는 상태 확인
			.andExpect(view().name("signup_form"));
	}
}

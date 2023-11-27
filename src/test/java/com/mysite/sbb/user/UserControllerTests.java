package com.mysite.sbb.user;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.mysite.sbb.SecurityConfig;

import jakarta.transaction.Transactional;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UserControllerTests {
	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private UserService userService;

	@Test
	@DisplayName("[/user/signup] 접속")
	public void connect_signup() throws Exception {
		ResultActions resultActions = mockMvc
			.perform(get("/user/signup"))
			.andDo(print());

		resultActions
			.andExpect(status().is2xxSuccessful())
			.andExpect(handler().handlerType(UserController.class))
			.andExpect(handler().methodName("signup"))
			.andExpect(view().name("signup_form"));
	}

	@Test
	@DisplayName("[/user/signup] 회원가입")
	public void connect_trysignup() throws Exception {
		ResultActions resultActions = mockMvc
			.perform(
				post("/user/signup")
					.with(csrf())
					.param("username", "fakeidab")
					.param("password1", "fakepw12")
					.param("password2", "fakepw12")
					.param("email", "Fakeemail@gmail.com")
			)
				.andDo(print());

		resultActions
			.andExpect(status().is3xxRedirection())
			.andExpect(handler().handlerType(UserController.class))
			.andExpect(handler().methodName("signup"))
			.andExpect(redirectedUrl("/"))
			.andDo(print());
	}

	@Test
	@DisplayName("[/user/signup] 회원가입 비밀번호 확인이 비어있다.")
	public void connect_tryblanksignup() throws Exception {
		ResultActions resultActions = mockMvc
			.perform(
				post("/user/signup")
					.with(csrf())
					.param("username", "fakeidab")
					.param("password1", "fakepw12")
					.param("email", "Fakeemail@gmail.com")
			)
			.andDo(print());

		resultActions
			.andExpect(status().is2xxSuccessful())
			.andExpect(handler().handlerType(UserController.class))
			.andExpect(handler().methodName("signup"))
			.andExpect(view().name("signup_form"))
			.andDo(print());
	}

	@Test
	@DisplayName("[/user/signup] 중복 회원가입 방지")
	public void connect_duplicatesignup() throws Exception {
		// 첫 번째 회원가입 요청
		ResultActions resultActions = mockMvc
			.perform(
				post("/user/signup")
					.with(csrf())
					.param("username", "fakeidab")
					.param("password1", "fakepw12")
					.param("password2", "fakepw12")
					.param("email", "Fakeemail@gmail.com")
			)
			.andDo(print());

		resultActions
			.andExpect(status().is3xxRedirection())
			.andExpect(handler().handlerType(UserController.class))
			.andExpect(handler().methodName("signup"))
			.andExpect(redirectedUrl("/"))
			.andDo(print());

		// userService.create가 DataIntegrityViolationException을 던지도록 설정
		doThrow(DataIntegrityViolationException.class).when(userService).create(any(), any(), any());

		// 두 번째 중복 회원가입 요청
		ResultActions resultActions2 = mockMvc
			.perform(
				post("/user/signup")
					.with(csrf())
					.param("username", "fakeidab")
					.param("password1", "fakepw12")
					.param("password2", "fakepw12")
					.param("email", "Fakeemail@gmail.com")
			)
			.andDo(print());

		resultActions2
			.andExpect(status().is2xxSuccessful())
			.andExpect(handler().handlerType(UserController.class))
			.andExpect(handler().methodName("signup"))
			.andExpect(view().name("signup_form"))
			.andDo(print());
	}
}

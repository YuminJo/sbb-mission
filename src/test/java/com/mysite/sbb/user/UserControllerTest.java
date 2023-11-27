package com.mysite.sbb.user;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.mysite.sbb.SecurityConfig;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UserControllerTest {
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
		// 가상의 사용자를 생성
		SiteUser fakeUser = createFakeUser("fakeidab", "fakepw12", "Fakeemail@gmail.com");

		// userRepository.findById 메서드가 호출될 때 fakeUser를 반환하도록 설정
		when(userService.findUser("fakeidab")).thenReturn(Optional.of(fakeUser));

		// 가상의 HTTP POST 요청을 수행하여 회원가입을 시뮬레이트
		ResultActions resultActions = performSignup("fakeidab", "fakepw12", "fakepw12", "Fakeemail@gmail.com");

		// 응답을 검증
		resultActions
			.andExpect(status().is3xxRedirection())
			.andExpect(handler().handlerType(UserController.class))
			.andExpect(handler().methodName("signup"))
			.andExpect(redirectedUrl("/"))
			.andDo(print());

		// userService.findUser("fakeidab")에서 fakeUser를 얻을 수 있어야 함
		SiteUser user = userService.findUser("fakeidab").orElseThrow();
		assertThat(user.getUsername()).isEqualTo("fakeidab");
	}

	private SiteUser createFakeUser(String username, String password, String email) {
		SiteUser fakeUser = new SiteUser();
		fakeUser.setUsername(username);
		fakeUser.setPassword(password);
		fakeUser.setEmail(email);
		return fakeUser;
	}

	@Test
	@DisplayName("[/user/signup] 회원가입 비밀번호 확인이 비어있다.")
	public void connect_tryblanksignup() throws Exception {
		performSignup("fakeidab", "fakepw12", null, "Fakeemail@gmail.com")
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
		connect_signup();

		// userService.create가 DataIntegrityViolationException을 던지도록 설정
		doThrow(DataIntegrityViolationException.class).when(userService).create(any(), any(), any());

		// 두 번째 중복 회원가입 요청
		performSignup("fakeidab", "fakepw12", "fakepw12", "Fakeemail@gmail.com")
			.andExpect(status().is2xxSuccessful())
			.andExpect(handler().handlerType(UserController.class))
			.andExpect(handler().methodName("signup"))
			.andExpect(view().name("signup_form"))
			.andDo(print());
	}

	private ResultActions performSignup(String username, String password1, String password2, String email) throws Exception {
		return mockMvc.perform(
			post("/user/signup")
				.with(csrf())
				.param("username", username)
				.param("password1", password1)
				.param("password2", password2)
				.param("email", email)
		);
	}
}

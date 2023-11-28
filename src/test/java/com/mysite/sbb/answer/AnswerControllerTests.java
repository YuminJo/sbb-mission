package com.mysite.sbb.answer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.mysite.sbb.question.Question;
import com.mysite.sbb.question.QuestionService;
import com.mysite.sbb.user.SiteUser;
import com.mysite.sbb.user.UserService;

@WebMvcTest(AnswerController.class)
@ActiveProfiles("test")
public class AnswerControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private AnswerService answerService;

	@MockBean
	private QuestionService questionService;

	@MockBean
	private UserService userService;

	@Test
	@DisplayName("[/answer/create/{id}] Create Post 발송 성공")
	@WithMockUser
	public void connect_CreateId() throws Exception {
		SiteUser siteUser = new SiteUser();
		siteUser.setUsername("TEST");

		AnswerForm answerForm = new AnswerForm();
		answerForm.setContent("TEST CONTENT");

		// 가짜 Question 객체 생성
		Question fakeQuestion = createFakeQuestion();

		// 가짜 Answer 객체 생성
		Answer answer = createFakeAnswer(siteUser,fakeQuestion);

		fakeQuestion.setAnswerList(new ArrayList<>() {{
			add(answer);
		}});

		when(questionService.getQuestion(1)).thenReturn(fakeQuestion);
		when(userService.getUser(any())).thenReturn(siteUser);

		mockMvc.perform(post("/answer/create/1")
				.param("content", answerForm.getContent())
				.with(csrf())) // AnswerForm의 content를 전달
			.andExpect(status().is3xxRedirection()) // 3xx 리다이렉션 상태 코드 기대
			.andExpect(redirectedUrl("/question/detail/1"));
	}

	@Test
	@DisplayName("[/answer/modify/{id}] 접속 성공")
	@WithMockUser("testUser")
	public void connect_Modify() throws Exception {
		SiteUser siteUser = new SiteUser();
		siteUser.setUsername("testUser");

		// 가짜 Question 객체 생성
		Question fakeQuestion = createFakeQuestion();

		// 가짜 Answer 객체 생성
		Answer answer = createFakeAnswer(siteUser,fakeQuestion);

		fakeQuestion.setAnswerList(new ArrayList<>() {{add(answer);}});

		when(answerService.getAnswer(anyInt())).thenReturn(answer);

		mockMvc.perform(get("/answer/modify/{id}",0))
			.andExpect(status().is2xxSuccessful())
			.andExpect(view().name("answer_form"));
	}

	@Test
	@DisplayName("[/answer/modify/{id}] 수정 성공")
	@WithMockUser("testUser")
	public void connect_ModifyAnswer() throws Exception {
		SiteUser siteUser = new SiteUser();
		siteUser.setUsername("testUser");

		AnswerForm answerForm = new AnswerForm();
		answerForm.setContent("TEST CONTENT");

		// 가짜 Question 객체 생성
		Question fakeQuestion = createFakeQuestion();

		// 가짜 Answer 객체 생성
		Answer answer = createFakeAnswer(siteUser,fakeQuestion);

		fakeQuestion.setAnswerList(new ArrayList<>() {{add(answer);}});

		when(answerService.getAnswer(anyInt())).thenReturn(answer);

		mockMvc.perform(post("/answer/modify/0")
				.param("content", answerForm.getContent())
				.with(csrf()))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/question/detail/1"));
	}

	@Test
	@DisplayName("[/answer/modify/{id}] 수정 실패")
	@WithMockUser("fakeUser")
	public void connect_ModifyAnswerError() throws Exception {
		SiteUser siteUser = new SiteUser();
		siteUser.setUsername("testUser");

		AnswerForm answerForm = new AnswerForm();
		answerForm.setContent("TEST CONTENT");

		// 가짜 Question 객체 생성
		Question fakeQuestion = createFakeQuestion();

		// 가짜 Answer 객체 생성
		Answer answer = createFakeAnswer(siteUser,fakeQuestion);

		fakeQuestion.setAnswerList(new ArrayList<>() {{add(answer);}});

		when(answerService.getAnswer(anyInt())).thenReturn(answer);

		mockMvc.perform(post("/answer/modify/0")
				.param("content", answerForm.getContent())
				.with(csrf())) // AnswerForm의 content를 전달
			.andExpect(status().is4xxClientError());
	}

	@Test
	@DisplayName("[/answer/delete/{id}] 삭제 성공")
	@WithMockUser("testUser")
	public void connect_Delete() throws Exception {
		SiteUser siteUser = new SiteUser();
		siteUser.setUsername("testUser");

		// 가짜 Question 객체 생성
		Question fakeQuestion = createFakeQuestion();

		// 가짜 Answer 객체 생성
		Answer answer = createFakeAnswer(siteUser,fakeQuestion);

		fakeQuestion.setAnswerList(new ArrayList<>() {{add(answer);}});

		when(answerService.getAnswer(anyInt())).thenReturn(answer);

		mockMvc.perform(get("/answer/delete/{id}",0))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/question/detail/1"));
	}

	@Test
	@DisplayName("[/answer/delete/{id}] 삭제 실패")
	@WithMockUser("fakeUser")
	public void connect_DeleteError() throws Exception {
		SiteUser siteUser = new SiteUser();
		siteUser.setUsername("testUser");

		// 가짜 Question 객체 생성
		Question fakeQuestion = createFakeQuestion();

		// 가짜 Answer 객체 생성
		Answer answer = createFakeAnswer(siteUser,fakeQuestion);

		fakeQuestion.setAnswerList(new ArrayList<>() {{add(answer);}});

		when(answerService.getAnswer(anyInt())).thenReturn(answer);

		mockMvc.perform(get("/answer/delete/{id}",0))
			.andExpect(status().is4xxClientError());
	}

	@Test
	@DisplayName("[/answer/delete/{id}] 추천하기")
	@WithMockUser
	public void connect_vote() throws Exception {
		SiteUser siteUser = new SiteUser();
		siteUser.setUsername("testUser");

		// 가짜 Question 객체 생성
		Question fakeQuestion = createFakeQuestion();

		// 가짜 Answer 객체 생성
		Answer answer = createFakeAnswer(siteUser,fakeQuestion);

		fakeQuestion.setAnswerList(new ArrayList<>() {{add(answer);}});

		when(answerService.getAnswer(anyInt())).thenReturn(answer);
		when(userService.getUser(any())).thenReturn(siteUser);

		mockMvc.perform(get("/answer/vote/{id}",0))
			.andExpect(status().is3xxRedirection());
	}

	private Question createFakeQuestion() {
		Question fakeQuestion = new Question();
		fakeQuestion.setId(1);
		fakeQuestion.setSubject("Fake Subject");
		fakeQuestion.setContent("Fake Content");
		fakeQuestion.setCreateDate(LocalDateTime.now());

		return fakeQuestion;
	}

	private Answer createFakeAnswer(SiteUser user,Question question) {
		Answer answer = new Answer();
		answer.setAuthor(user);
		answer.setId(0);
		answer.setContent(question.getContent());
		answer.setCreateDate(question.getCreateDate());
		answer.setQuestion(question);
		return answer;
	}

}

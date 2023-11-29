package com.mysite.sbb.question;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.mysite.sbb.SecurityConfig;
import com.mysite.sbb.answer.Answer;
import com.mysite.sbb.answer.AnswerForm;
import com.mysite.sbb.user.SiteUser;
import com.mysite.sbb.user.UserController;
import com.mysite.sbb.user.UserService;

@WebMvcTest(QuestionController.class)
@Import(SecurityConfig.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class QuestionControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private QuestionService questionService;

	@MockBean
	private UserService userService;

	@Test
	@DisplayName("[/question/list] 접속")
	public void connect_List() throws Exception {
		Question fakeQuestion = createFakeQuestion();
		Answer fakeAnswer = createFakeAnswer(null,fakeQuestion);
		fakeQuestion.setAnswerList(new ArrayList<>() {{add(fakeAnswer);}});

		// Mock 데이터 생성
		List<Question> questionList = List.of(
			fakeQuestion
		);

		Page<Question> page = new PageImpl<>(questionList);

		// QuestionService의 동작 설정
		when(questionService.getList(anyInt(),any())).thenReturn(page);

		ResultActions resultActions = mockMvc
			.perform(get("/question/list")
				.param("page", "0")
				.with(csrf()))
			.andDo(print());

		resultActions
			.andExpect(status().is2xxSuccessful())
			.andExpect(handler().handlerType(QuestionController.class))
			.andExpect(handler().methodName("list"))
			.andExpect(view().name("question_list"))
			.andExpect(model().attributeExists("paging"))
			.andExpect(model().attribute("paging", page))
			.andDo(print()); // 디버깅을 위해 요청 및 응답 세부 정보를 출력합니다.
	}

	@Test
	@DisplayName("[/question/create] Create 접속")
	@WithMockUser
	public void connect_Create() throws Exception {
		mockMvc.perform(get("/question/create"))
			.andExpect(handler().handlerType(QuestionController.class))
			.andExpect(handler().methodName("questionCreate"))
			.andExpect(status().is2xxSuccessful())
			.andExpect(view().name("question_form"))
			.andExpect(model().attributeExists("questionForm"));
	}

	@Test
	@DisplayName("[/question/create] Create Post 발송 실패")
	@WithMockUser
	public void connect_CreateError() throws Exception {
		MockHttpServletRequestBuilder requestBuilder = post("/question/create")
			.with(csrf())
			.param("subject", "")  // empty subject to trigger validation error
			.param("content", "Valid Content");

		mockMvc.perform(requestBuilder)
			.andExpect(status().isOk())
			.andExpect(view().name("question_form"))
			.andExpect(model().attributeExists("questionForm"))
			.andExpect(model().hasErrors())
			.andExpect(model().attributeHasFieldErrors("questionForm", "subject"));
	}

	@Test
	@DisplayName("[/question/create] Create Post 빈 제목 발송")
	@WithMockUser
	public void connect_CreateError2() throws Exception {
		MockHttpServletRequestBuilder requestBuilder = post("/question/create")
			.with(csrf())
			.param("subject", "")
			.param("content", "Fake Content");

		mockMvc.perform(requestBuilder)
			.andExpect(status().isOk())
			.andExpect(redirectedUrl(null))
			.andDo(print());
	}

	@Test
	@DisplayName("[/question/create] Create Post 빈 내용 발송")
	@WithMockUser
	public void connect_CreateError3() throws Exception {
		MockHttpServletRequestBuilder requestBuilder = post("/question/create")
			.with(csrf())
			.param("subject", "Fake Subject")
			.param("content", "");

		mockMvc.perform(requestBuilder)
			.andExpect(status().isOk())
			.andExpect(redirectedUrl(null))
			.andDo(print());
	}

	@Test
	@DisplayName("[/question/create] Create Post 발송 성공")
	@WithMockUser
	public void connect_CreatePost() throws Exception {
		MockHttpServletRequestBuilder requestBuilder = post("/question/create")
			.with(csrf())
			.param("subject", "Fake Subject")
			.param("content", "Fake Content");

		mockMvc.perform(requestBuilder)
			.andExpect(status().isFound())
			.andExpect(redirectedUrl("/question/list"))
			.andDo(print());
	}

	@Test
	@DisplayName("[/question/modify/{id}] 접속 성공")
	@WithMockUser("testUser")
	public void connect_Modify() throws Exception {
		SiteUser siteUser = new SiteUser();
		siteUser.setUsername("testUser");

		// 가짜 Question 객체 생성
		Question fakeQuestion = createFakeQuestion();
		fakeQuestion.setAuthor(siteUser);

		when(questionService.getQuestion(anyInt())).thenReturn(fakeQuestion);

		mockMvc.perform(get("/question/modify/{id}",1))
			.andExpect(status().is2xxSuccessful())
			.andExpect(view().name("question_form"));
	}

	@Test
	@DisplayName("[/question/modify/{id}] 수정 성공")
	@WithMockUser("testUser")
	public void connect_ModifyAnswer() throws Exception {
		SiteUser siteUser = new SiteUser();
		siteUser.setUsername("testUser");

		QuestionForm questionForm = new QuestionForm();
		questionForm.setContent("TEST CONTENT");
		questionForm.setSubject("TEST SUB");

		// 가짜 Question 객체 생성
		Question fakeQuestion = createFakeQuestion();
		fakeQuestion.setAuthor(siteUser);

		when(questionService.getQuestion(anyInt())).thenReturn(fakeQuestion);

		mockMvc.perform(post("/question/modify/{id}",1)
				.param("content", questionForm.getContent())
				.param("subject", questionForm.getSubject())
				.with(csrf()))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/question/detail/1"));
	}

	@Test
	@DisplayName("[/question/modify/{id}] 수정 실패")
	@WithMockUser("fakeUser")
	public void connect_ModifyAnswerError() throws Exception {
		SiteUser siteUser = new SiteUser();
		siteUser.setUsername("testUser");

		QuestionForm questionForm = new QuestionForm();
		questionForm.setContent("TEST CONTENT");
		questionForm.setSubject("TEST SUB");

		// 가짜 Question 객체 생성
		Question fakeQuestion = createFakeQuestion();
		fakeQuestion.setAuthor(siteUser);

		when(questionService.getQuestion(anyInt())).thenReturn(fakeQuestion);

		mockMvc.perform(post("/question/modify/{id}",1)
				.param("content", questionForm.getContent())
				.param("subject", questionForm.getSubject())
				.with(csrf()))
			.andExpect(status().is4xxClientError());
	}

	@Test
	@DisplayName("[/question/delete/{id}] 삭제 성공")
	@WithMockUser("testUser")
	public void connect_Delete() throws Exception {
		SiteUser siteUser = new SiteUser();
		siteUser.setUsername("testUser");

		// 가짜 Question 객체 생성
		Question fakeQuestion = createFakeQuestion();
		fakeQuestion.setAuthor(siteUser);

		when(questionService.getQuestion(anyInt())).thenReturn(fakeQuestion);

		mockMvc.perform(get("/question/delete/{id}",1))
			.andExpect(status().is3xxRedirection());
	}

	@Test
	@DisplayName("[/question/delete/{id}] 삭제 실패")
	@WithMockUser("fakeUser")
	public void connect_DeleteError() throws Exception {
		SiteUser siteUser = new SiteUser();
		siteUser.setUsername("testUser");

		// 가짜 Question 객체 생성
		Question fakeQuestion = createFakeQuestion();
		fakeQuestion.setAuthor(siteUser);

		when(questionService.getQuestion(anyInt())).thenReturn(fakeQuestion);

		mockMvc.perform(get("/question/delete/{id}",1))
			.andExpect(status().is4xxClientError());
	}

	@Test
	@DisplayName("[/question/delete/{id}] 삭제 실패")
	@WithMockUser("fakeUser")
	public void connect_vote() throws Exception {
		SiteUser siteUser = new SiteUser();
		siteUser.setUsername("testUser");

		// 가짜 Question 객체 생성
		Question fakeQuestion = createFakeQuestion();
		fakeQuestion.setAuthor(siteUser);

		when(questionService.getQuestion(anyInt())).thenReturn(fakeQuestion);

		mockMvc.perform(get("/question/vote/{id}",1))
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
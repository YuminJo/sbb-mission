package com.mysite.sbb.question;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
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
	public void connect_QuestionList() throws Exception {
		// 가짜 Question 객체 생성
		Question fakeQuestion = new Question();
		fakeQuestion.setSubject("Fake Subject");
		fakeQuestion.setContent("Fake Content");
		fakeQuestion.setCreateDate(LocalDateTime.now());

		Answer fakeAnswer = new Answer();
		fakeAnswer.setContent("Fake Con");
		fakeAnswer.setCreateDate(LocalDateTime.now());
		fakeAnswer.setQuestion(fakeQuestion);

		fakeQuestion.setAnswerList(new ArrayList<>() {{add(fakeAnswer);}});

		// Mock 데이터 생성
		List<Question> questionList = List.of(
			fakeQuestion
		);

		Page<Question> page = new PageImpl<>(questionList);

		// QuestionService의 동작 설정
		when(questionService.getList(anyInt())).thenReturn(page);

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
	@DisplayName("[/question/detail/{id}] Fake Question 생성 후 detail/1 접속")
	@WithMockUser
	public void fakeqes_connect_QuestionDetail() throws Exception {
		// 가짜 Answer 객체 생성
		Answer answer = new Answer();

		// 가짜 Question 객체 생성
		Question fakeQuestion = new Question();
		fakeQuestion.setId(1);
		fakeQuestion.setSubject("Fake Subject");
		fakeQuestion.setContent("Fake Content");
		fakeQuestion.setCreateDate(LocalDateTime.now());

		answer.setContent(fakeQuestion.getContent());
		answer.setCreateDate(fakeQuestion.getCreateDate());
		answer.setQuestion(fakeQuestion);
		ArrayList<Answer> newAns = new ArrayList<>();
		newAns.add(answer);
		fakeQuestion.setAnswerList(newAns);

		// QuestionService의 동작 설정
		when(questionService.getQuestion(1)).thenReturn(fakeQuestion);

		mockMvc.perform(get("/question/detail/1"))
			.andExpect(status().is2xxSuccessful())
			.andExpect(handler().handlerType(QuestionController.class))
			.andExpect(handler().methodName("detail"))
			.andExpect(view().name("question_detail"))
			.andExpect(model().attributeExists("question"))
			.andExpect(model().attribute("question", fakeQuestion));
	}

	@Test
	@DisplayName("[/question/create] Create 접속")
	@WithMockUser
	public void connect_QuestionCreate4xxGet() throws Exception {
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
	public void connect_QuestionCreatePostFailed() throws Exception {
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
	public void connect_QuestionCreatePostEmptySbj() throws Exception {
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
	public void connect_QuestionCreatePostEmptyCon() throws Exception {
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
	public void connect_QuestionCreatePost() throws Exception {
		MockHttpServletRequestBuilder requestBuilder = post("/question/create")
			.with(csrf())
			.param("subject", "Fake Subject")
			.param("content", "Fake Content");

		mockMvc.perform(requestBuilder)
			.andExpect(status().isFound())
			.andExpect(redirectedUrl("/question/list"))
			.andDo(print());
	}
}
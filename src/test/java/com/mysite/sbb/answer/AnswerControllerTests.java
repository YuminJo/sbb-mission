package com.mysite.sbb.answer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.mysite.sbb.answer.AnswerController;
import com.mysite.sbb.answer.AnswerForm;
import com.mysite.sbb.answer.AnswerService;
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
	public void connect_AnswerCreateId() throws Exception {
		SiteUser siteUser = new SiteUser();
		siteUser.setUsername("TEST");

		AnswerForm answerForm = new AnswerForm();
		answerForm.setContent("TEST CONTENT");

		// 가짜 Answer 객체 생성
		Answer answer = new Answer();

		// 가짜 Question 객체 생성
		Question fakeQuestion = createFakeQuestion();
		answer.setContent(fakeQuestion.getContent());
		answer.setCreateDate(fakeQuestion.getCreateDate());
		answer.setQuestion(fakeQuestion);

		Principal principal = new TestPrincipal("testUser");

		when(questionService.getQuestion(1)).thenReturn(fakeQuestion);
		when(userService.getUser(any())).thenReturn(siteUser);

		mockMvc.perform(post("/answer/create/1")
				.param("content", answerForm.getContent())
				.principal(principal)
				.with(csrf())) // AnswerForm의 content를 전달
			.andExpect(status().is3xxRedirection()) // 3xx 리다이렉션 상태 코드 기대
			.andExpect(redirectedUrl("/question/detail/1"));
	}

	private Question createFakeQuestion() {
		Question fakeQuestion = new Question();
		fakeQuestion.setId(1);
		fakeQuestion.setSubject("Fake Subject");
		fakeQuestion.setContent("Fake Content");
		fakeQuestion.setCreateDate(LocalDateTime.now());

		Answer answer = new Answer();
		answer.setContent(fakeQuestion.getContent());
		answer.setCreateDate(fakeQuestion.getCreateDate());
		answer.setQuestion(fakeQuestion);

		fakeQuestion.setAnswerList(new ArrayList<>() {{
			add(answer);
		}});

		return fakeQuestion;
	}

	private static class TestPrincipal implements Principal {
		private final String name;

		public TestPrincipal(String name) {
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}
	}
}

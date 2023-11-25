package com.mysite.sbb.answer;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.ArrayList;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.mysite.sbb.question.Question;
import com.mysite.sbb.question.QuestionService;

@WebMvcTest(AnswerController.class)
@ActiveProfiles("test")
public class AnswerControllerTests {
	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private AnswerService answerService;

	@MockBean
	private QuestionService questionService;

	@Test
	@DisplayName("[/answer/create/{id}] Create Post 발송 성공")
	public void connect_AnswerCreateId() throws Exception {
		// 가짜 Question 객체 생성
		Question fakeQuestion = new Question();
		fakeQuestion.setId(1);
		fakeQuestion.setSubject("Fake Subject");
		fakeQuestion.setContent("Fake Content");
		fakeQuestion.setCreateDate(LocalDateTime.now());

		// 가짜 AnswerForm 객체 생성
		AnswerForm fakeAnswerForm = new AnswerForm();
		fakeAnswerForm.setContent("Fake answer content"); // 적절한 값으로 설정

		// QuestionService가 메서드를 호출할 때 가짜 Question을 반환하도록 설정
		when(questionService.getQuestion(1)).thenReturn(fakeQuestion);

		// 테스트 수행
		mockMvc.perform(post("/answer/create/1")
				.param("content", fakeAnswerForm.getContent())) // AnswerForm의 content를 전달
			.andExpect(status().is3xxRedirection()) // 3xx 리다이렉션 상태 코드 기대
			.andExpect(redirectedUrl("/question/detail/1")); // 리다이렉션된 URL 기대
	}
}
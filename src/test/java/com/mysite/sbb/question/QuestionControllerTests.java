package com.mysite.sbb.question;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import jakarta.transaction.Transactional;

@WebMvcTest(QuestionController.class)
public class QuestionControllerTests {
	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private QuestionService questionService;

	@Test
	@DisplayName("/question/list 접속")
	public void connect_QuestionList() throws Exception{
		mockMvc.perform(MockMvcRequestBuilders
				.get("/question/list")
				.accept(MediaType.APPLICATION_JSON))
			.andDo(print())
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andReturn();
	}
	@Test
	@DisplayName("/question/detail/{id} 접속")
	public void connect_QuestionDetail() throws Exception {
		// 가짜 Question 객체 생성
		Question fakeQuestion = new Question();
		fakeQuestion.setId(2);
		fakeQuestion.setSubject("Fake Subject");
		fakeQuestion.setContent("Fake Content");

		// QuestionService의 동작 설정
		when(questionService.getQuestion(2)).thenReturn(fakeQuestion);

		mockMvc.perform(MockMvcRequestBuilders
				.get("/question/detail/2")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.view().name("question_detail"))
			.andExpect(MockMvcResultMatchers.model().attributeExists("question"))
			.andExpect(MockMvcResultMatchers.model().attribute("question", fakeQuestion));
	}
}
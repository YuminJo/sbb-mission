package com.mysite.sbb.question;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@WebMvcTest(QuestionController.class)
public class QuestionControllerTests {
	@Autowired
	private MockMvc mockMvc;

	@Test
	@DisplayName("/question/list 접속")
	public void connect_QuestionList() throws Exception{
		mockMvc.perform(MockMvcRequestBuilders
			.get("/question/list")
			.accept(MediaType.APPLICATION_JSON))
			.andDo(print())
			.andExpect(MockMvcResultMatchers.status().isOk());
	}
}

package com.example.demo.controller.v1.book;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.transaction.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private String token;

    @BeforeEach
    void setUp() throws Exception{
        long epochTime = LocalDateTime.now().atZone(ZoneId.systemDefault()).toEpochSecond();
        {
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("id", "test_" + epochTime + "@test.com");
            params.add("password", "test");
            params.add("name", "test_" + epochTime);
            mockMvc.perform(post("/v1/signup").params(params))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.msg").exists());
        }
        {
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("id", "test_" + epochTime + "@test.com");
            params.add("password", "test");
            MvcResult result = mockMvc.perform(post("/v1/signin").params(params))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.msg").exists())
                    .andExpect(jsonPath("$.data").exists())
                    .andReturn();

            String resultString = result.getResponse().getContentAsString();
            JacksonJsonParser jsonParser = new JacksonJsonParser();
            token = jsonParser.parseMap(resultString).get("data").toString();
        }
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    public void invalidToken() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                .get("/v1/search/history")
                .header("X-AUTH-TOKEN", "XXXXXXXXXX"))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/exception/entrypoint"));
    }


    @Test
    @WithMockUser(username = "mockUser", roles = {"ADMIN"}) // 가상의 Mock 유저 대입
    public void accessdenied() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                .get("/v1/search/book/history"))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/exception/accessdenied"));
    }

    @Test
    void search() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                .get("/v1/search/book/스프링부트")
                .header("X-AUTH-TOKEN", token))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    void history() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                .get("/v1/search/book/history")
                .header("X-AUTH-TOKEN", token))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.list").exists());
    }

    @Test
    void rank() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                .get("/v1/search/book/rank/")
                .header("X-AUTH-TOKEN", token))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.list").exists());
    }
}
package de.scampiRest.test;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.is;

import de.scampiRest.Application;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;



@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
public class RestScampiMessageTest {
	
	private MockMvc mockMvc;
	@Autowired private WebApplicationContext webApplicationContext;
	// ScampiCommunicator scampiCommunicator = new ScampiCommunicator();
	
	@Before
    public void setup() throws Exception {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }
	
	@Test
	public void emptyMessage() throws Exception {
		ResultActions as = mockMvc.perform(get("/"))
			.andExpect(status().isOk());
	}
	
	@Test 
	public void runNewMessage() throws Exception {
		ResultActions as = mockMvc.perform(get("/test/newMessage"));
		// .andExpect(status().is4xxClientError());
		System.out.println("Response: " + as.andReturn().getResponse().getContentAsString());
		// .andExpect(jsonPath("$.status").value("400"));
	}
	@Test 
	public void runExeption() throws Exception {
		ResultActions as = mockMvc.perform(get("/test/exeption"));
		// .andExpect(status().is4xxClientError());
		System.out.println("Response: " + as.andReturn().getResponse().getContentAsString());
		// .andExpect(jsonPath("$.status").value("400"));
	}
	@Test 
	public void runPath() throws Exception {
		ResultActions as = mockMvc.perform(get("/test/path"));
		// .andExpect(status().is4xxClientError());
		System.out.println("Response: " + as.andReturn().getResponse().getContentAsString());
		// .andExpect(jsonPath("$.status").value("400"));
	}
}

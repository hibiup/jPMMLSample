package samples.jpmml.unit.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;
import samples.jpmml.configuration.ApplicationConfiguration;
import samples.jpmml.controller.impl.RealtimeScoringControllerImpl;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes={
        RealtimeScoringControllerImpl.class,
        ApplicationConfiguration.class})
@AutoConfigureMockMvc
public class RealtimeScoringTests {
    @Autowired private MockMvc mvc;
    @Autowired
    private MappingJackson2JsonView jsonView;

    @Test
    public void getHelloSpringBoot() throws Exception {
        MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.get("/v1/scoring")
                .accept(MediaType.APPLICATION_JSON)).andReturn();

        mvc.perform(asyncDispatch(mvcResult)).andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"));

        MockHttpServletResponse response = mvcResult.getResponse();
        Map message = jsonView.getObjectMapper().readValue(response.getContentAsByteArray(), Map.class);
        assert(message.get("score").toString().equals("2"));
    }
}

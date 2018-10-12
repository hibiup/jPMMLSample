package samples.jpmml.unit.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;
import samples.jpmml.configuration.ApplicationConfiguration;
import samples.jpmml.controller.impl.RealtimeScoringControllerImpl;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.fail;
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
    public void getScoring() throws Exception {
        MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.get("/v1/scoring")
                .accept(MediaType.APPLICATION_JSON)).andReturn();

        mvc.perform(asyncDispatch(mvcResult)).andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"));

        MockHttpServletResponse response = mvcResult.getResponse();
        Map message = jsonView.getObjectMapper().readValue(response.getContentAsByteArray(), Map.class);
        assert(message.get("score").toString().equals("2"));
    }


    @Test
    public void getReleaseModel() throws Exception {
        List<MockMultipartFile> files = new ArrayList();

        List<String> fileNames = new ArrayList();
        fileNames.add("svc.pmml");
        fileNames.add("svc1.pmml");

        fileNames.forEach(name -> {
            File file = new File(getClass().getClassLoader().getResource(name).getFile());
            try(InputStream fis = new FileInputStream(file)) {
                MockMultipartFile multipartFile = new MockMultipartFile("model", name, null,
                        FileCopyUtils.copyToByteArray(fis));
                files.add(multipartFile);
            }
            catch(Exception e){
                e.printStackTrace();
                fail();
            }
        });

        MockMultipartHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.multipart("/v1/release");
        files.forEach(file -> requestBuilder.file(file));

        MvcResult mvcResult = mvc.perform(requestBuilder
                .accept(MediaType.APPLICATION_JSON)).andReturn();

        mvc.perform(asyncDispatch(mvcResult)).andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"));

        MockHttpServletResponse response = mvcResult.getResponse();
        List message = jsonView.getObjectMapper().readValue(response.getContentAsByteArray(), List.class);
        assert(message.size() >= 1);
    }
}

package samples.jpmml.unit.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import java.lang.reflect.Array;
import java.util.*;

import static org.junit.Assert.assertTrue;
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

    @Value("${model.repository.localtion}") String mail_stmp_enabled;

    MockMultipartHttpServletRequestBuilder createRequestBuilder(List<String> fileNames, String url) {
        List<MockMultipartFile> files = new ArrayList();

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

        MockMultipartHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.multipart(url);
        files.forEach(file -> requestBuilder.file(file));

        return requestBuilder;
    }

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
    public void releaseModel() throws Exception {
        File repoPath = new File(mail_stmp_enabled);
        if(!repoPath.exists()) repoPath.mkdir();
        else Arrays.asList(repoPath.listFiles()).forEach(file -> file.delete());


        List<String> fileNames = new ArrayList();
        fileNames.add("svc.pmml");
        fileNames.add("svc1.pmml");

        MockMultipartHttpServletRequestBuilder requestBuilder = createRequestBuilder(fileNames, "/v1/release");

        MvcResult mvcResult = mvc.perform(requestBuilder
                .accept(MediaType.APPLICATION_JSON)).andReturn();

        mvc.perform(asyncDispatch(mvcResult)).andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"));

        MockHttpServletResponse response = mvcResult.getResponse();
        List<Map<String, String>> message = jsonView.getObjectMapper().readValue(response.getContentAsByteArray(), List.class);
        assert(message.size() >= 1);
        message.forEach(msg -> assertTrue(msg.get("SUCCESS") instanceof String));
    }


    @Test
    public void releaseExistingModel() throws Exception {
        File repoPath = new File(mail_stmp_enabled);
        assertTrue(repoPath.exists());
        assertTrue(Arrays.asList(repoPath.listFiles()).size() > 0);

        MockMultipartHttpServletRequestBuilder requestBuilder = createRequestBuilder(Arrays.asList("svc.pmml"), "/v1/release");

        MvcResult mvcResult = mvc.perform(requestBuilder
                .accept(MediaType.APPLICATION_JSON)).andReturn();

        mvc.perform(asyncDispatch(mvcResult)).andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"));

        MockHttpServletResponse response = mvcResult.getResponse();
        List<Map<String, Exception>> message = jsonView.getObjectMapper().readValue(response.getContentAsByteArray(),
                new TypeReference<List<Map<String, Exception>>>(){});

        assert(message.size() >= 1);
        message.forEach(msg -> assertTrue(msg.get("FAILED") instanceof Exception));
    }


   /* @Test
    public void UpdateModel() throws Exception {
        File repoPath = new File(mail_stmp_enabled);
        if(!repoPath.exists()) fail();
        else assertTrue(Arrays.asList(repoPath.listFiles()).size() > 0);

        MockMultipartHttpServletRequestBuilder requestBuilder = createRequestBuilder(Arrays.asList("svc.pmml"), "/v1/update");

        MvcResult mvcResult = mvc.perform(requestBuilder
                .accept(MediaType.APPLICATION_JSON)).andReturn();

        mvc.perform(asyncDispatch(mvcResult)).andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"));
    }*/

}

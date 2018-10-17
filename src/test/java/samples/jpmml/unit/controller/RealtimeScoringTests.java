package samples.jpmml.unit.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
import samples.jpmml.service.ModelManager;
import samples.jpmml.service.impl.ModelRepositoryManager;
import samples.jpmml.configuration.ApplicationConfiguration;
import samples.jpmml.controller.impl.RealtimeScoringControllerImpl;
import samples.jpmml.service.impl.RealtimeScoringService;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes={
        RealtimeScoringControllerImpl.class,
        ApplicationConfiguration.class,
        ModelRepositoryManager.class,
        RealtimeScoringService.class})
@AutoConfigureMockMvc
public class RealtimeScoringTests {
    @Autowired private MockMvc mvc;
    @Autowired
    private MappingJackson2JsonView jsonView;

    ObjectMapper mapper = new ObjectMapper();

    @Value("${model.repository.location}") String model_repo_location;

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
    public void score() throws Exception {
        Map<String, Number> input = new HashMap();
        input.put("Sepal.Length", 1);
        input.put("Sepal.Width", 1);
        input.put("Petal.Length", 1);
        input.put("Petal.Width", 1);

        MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.get("/v1/scoring/svc")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(input))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(request().asyncStarted())
                .andReturn();

        mvc.perform(asyncDispatch(mvcResult)).andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"));

        // The second should load model from cache.
        mvcResult = mvc.perform(MockMvcRequestBuilders.get("/v1/scoring/svc")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(input))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(request().asyncStarted())
                .andReturn();

        mvc.perform(asyncDispatch(mvcResult)).andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"));

        MockHttpServletResponse response = mvcResult.getResponse();

        Map message = jsonView.getObjectMapper().readValue(response.getContentAsByteArray(), Map.class);
        assert(message.keySet().contains("Species"));
    }

    @Test
    public void releaseModel() throws Exception {
        File repoPath = new File(model_repo_location);
        if(!repoPath.exists()) repoPath.mkdir();
        else Arrays.asList(repoPath.listFiles()).forEach(file -> file.delete());


        List<String> fileNames = new ArrayList();
        fileNames.add("svc.pmml");
        //fileNames.add("svc1.pmml");

        MockMultipartHttpServletRequestBuilder requestBuilder = createRequestBuilder(fileNames, "/v1/release");

        MvcResult mvcResult = mvc.perform(requestBuilder
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(request().asyncStarted())
                .andReturn();

        mvc.perform(asyncDispatch(mvcResult)).andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"));
    }


    @Test
    public void releaseExistingModel() throws Exception {
        File repoPath = new File(model_repo_location);
        assertTrue(repoPath.exists());
        assertTrue(Arrays.asList(repoPath.listFiles()).size() > 0);

        MockMultipartHttpServletRequestBuilder requestBuilder = createRequestBuilder(Arrays.asList("svc.pmml"), "/v1/release");

        MvcResult mvcResult = mvc.perform(requestBuilder
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(request().asyncStarted())
                .andReturn();

        mvc.perform(asyncDispatch(mvcResult)).andExpect(status().isConflict())
                .andExpect(content().contentType("application/json;charset=UTF-8"));
    }


    @Test
    public void UpdateModel() throws Exception {
        File repoPath = new File(model_repo_location);
        if(!repoPath.exists()) fail();
        else assertTrue(Arrays.asList(repoPath.listFiles()).size() > 0);

        MockMultipartHttpServletRequestBuilder requestBuilder = createRequestBuilder(Arrays.asList("svc.pmml"), "/v1/refresh");
        requestBuilder.with(request -> {
            request.setMethod("PUT");
            return request;
        });

        MvcResult mvcResult = mvc.perform(requestBuilder
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(request().asyncStarted())
                .andReturn();

        mvc.perform(asyncDispatch(mvcResult)).andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"));
    }


    @Test
    public void UpdateNoneExistsModel() throws Exception {
        File repoPath = new File(model_repo_location);
        if(!repoPath.exists()) fail();
        else Arrays.asList(repoPath.listFiles()).forEach(file -> file.delete());

        MockMultipartHttpServletRequestBuilder requestBuilder =
                createRequestBuilder(Arrays.asList("svc.pmml"), "/v1/refresh");
        requestBuilder.with(request -> {
            request.setMethod("PUT");
            return request;
        });

        MvcResult mvcResult = mvc.perform(requestBuilder
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(request().asyncStarted())
                .andReturn();

        mvc.perform(asyncDispatch(mvcResult)).andExpect(status().isGone())
                .andExpect(content().contentType("application/json;charset=UTF-8"));
    }

    @Test
    public void deleteModel() throws Exception {
        File repoPath = new File(model_repo_location + "/svc" + ".pmml");
        if(!repoPath.exists()) fail();

        MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.delete("/v1/retire/svc")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(request().asyncStarted())
                .andReturn();

        mvc.perform(asyncDispatch(mvcResult)).andExpect(status().isOk());

        // Delete twice, the second time should got "Gone" status.
        mvcResult = mvc.perform(MockMvcRequestBuilders.delete("/v1/retire/svc"))
                .andReturn();
        mvc.perform(asyncDispatch(mvcResult)).andExpect(status().isGone());
    }
}

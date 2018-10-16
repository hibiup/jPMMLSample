package samples.jpmml.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RequestMapping(value = "/v1")
public interface RealtimeScoringController {
    @RequestMapping(value = "scoring",
            method = RequestMethod.GET,
            consumes = {MediaType.ALL_VALUE},
            produces = { MediaType.APPLICATION_JSON_VALUE })
    @ResponseBody
    CompletableFuture<ResponseEntity<Map<String, Object>>> scoring();


    @RequestMapping(value = "release",
            method = RequestMethod.POST,
            consumes = { MediaType.MULTIPART_FORM_DATA_VALUE },
            produces = { MediaType.APPLICATION_JSON_VALUE })
    @ResponseBody
    CompletableFuture<ResponseEntity<List<Map<String, Object>>>> release(@RequestPart(value = "model") List<MultipartFile> files);


    @RequestMapping(value = "refresh",
            method = RequestMethod.PUT,
            consumes = { MediaType.MULTIPART_FORM_DATA_VALUE },
            produces = { MediaType.APPLICATION_JSON_VALUE })
    @ResponseBody
    CompletableFuture<ResponseEntity<Map<String, Object>>> refresh(@RequestPart(value = "model") List<MultipartFile> files);

    @RequestMapping(value = "retire/{name}",
            method = RequestMethod.DELETE,
            consumes = { MediaType.ALL_VALUE },
            produces = { MediaType.APPLICATION_JSON_VALUE })
    @ResponseBody
    CompletableFuture<ResponseEntity<Map<String, Object>>> retire(@PathVariable(value = "name") String name);
}
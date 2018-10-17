package samples.jpmml.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RequestMapping(value = "/v1")
public interface RealtimeScoringController {
    @RequestMapping(value = "scoring/{name}",
            method = RequestMethod.GET,
            consumes = {MediaType.ALL_VALUE},
            produces = { MediaType.APPLICATION_JSON_VALUE })
    @ResponseBody
    CompletableFuture<ResponseEntity> scoring(@PathVariable(value = "name") String name,
                                                                   @RequestBody Map<String, Number> input);


    @RequestMapping(value = "release",
            method = RequestMethod.POST,
            consumes = { MediaType.MULTIPART_FORM_DATA_VALUE },
            produces = { MediaType.APPLICATION_JSON_VALUE })
    @ResponseBody
    CompletableFuture<ResponseEntity> release(@RequestPart(value = "model") MultipartFile files);


    @RequestMapping(value = "refresh",
            method = RequestMethod.PUT,
            consumes = { MediaType.MULTIPART_FORM_DATA_VALUE },
            produces = { MediaType.APPLICATION_JSON_VALUE })
    @ResponseBody
    CompletableFuture<ResponseEntity> refresh(@RequestPart(value = "model") MultipartFile file);

    @RequestMapping(value = "retire/{name}",
            method = RequestMethod.DELETE,
            consumes = { MediaType.ALL_VALUE },
            produces = { MediaType.APPLICATION_JSON_VALUE })
    @ResponseBody
    CompletableFuture<ResponseEntity> retire(@PathVariable(value = "name") String name);
}
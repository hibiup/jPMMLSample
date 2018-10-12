package samples.jpmml.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
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
    @Async
    CompletableFuture<ResponseEntity<Map<String, Object>>> scoring();


    @RequestMapping(value = "release",
            method = RequestMethod.POST,
            consumes = { MediaType.MULTIPART_FORM_DATA_VALUE },
            produces = { MediaType.APPLICATION_JSON_VALUE })
    @ResponseBody
    @Async
    CompletableFuture<ResponseEntity<Map<String, Object>>> release(@RequestPart(value = "model") List<MultipartFile> files);
}
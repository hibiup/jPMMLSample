package samples.jpmml.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RequestMapping(value = "/v1")
public interface RealtimeScoringController {
    @RequestMapping(value = "scoring",
            method = RequestMethod.GET,
            consumes = {MediaType.ALL_VALUE},
            produces = {
                    MediaType.APPLICATION_JSON_VALUE
            })
    @ResponseBody
    @Async
    CompletableFuture<ResponseEntity<Map<String, Object>>> scoring();
}
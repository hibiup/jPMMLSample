package samples.jpmml.controller;

import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

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
    CompletableFuture<Map<String, Object>> scoring();
}
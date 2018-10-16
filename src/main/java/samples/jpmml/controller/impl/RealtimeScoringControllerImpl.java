package samples.jpmml.controller.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import samples.jpmml.controller.RealtimeScoringController;
import samples.jpmml.controller.trait.RealtimeScoring;
import samples.jpmml.controller.trait.UploadFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;

import static samples.jpmml.controller.trait.UploadFile.MODE.Create;
import static samples.jpmml.controller.trait.UploadFile.MODE.Overwrite;

@RestController
public class RealtimeScoringControllerImpl implements RealtimeScoringController, RealtimeScoring, UploadFile {
    @Value("${model.repository.localtion}") String mail_stmp_enabled;
    @Autowired
    Executor executorService;

    @Override
    public String getRepositoryLocation() {
        return mail_stmp_enabled;
    }

    @Override
    public Executor getExecutorService() {
        return executorService;
    }

    @Override
    public CompletableFuture<ResponseEntity<List<Map<String, Object>>>> release(
            @RequestPart(value = "model") List<MultipartFile> files
    ) {
        ResponseEntity<List<Map<String, Object>>> entity =  new ResponseEntity(UploadFile.super.upload(files, Create), HttpStatus.OK);
        return CompletableFuture.completedFuture(entity);
    }

    @Override
    public CompletableFuture<ResponseEntity<Map<String, Object>>> refresh (
            @RequestPart(value = "model") List<MultipartFile> files
    ) throws Throwable {
        List<Map<String, Object>> uploaded =  UploadFile.super.upload(files, Overwrite);

        return CompletableFuture.completedFuture(uploaded.get(0))
                .thenApplyAsync(res -> {
                    Object path = res.get("SUCCESS");
                    if(path != null) return new ResponseEntity(path, HttpStatus.OK);

                    CompletionException e = (CompletionException)res.get("FAILED");
                    return new ResponseEntity(
                            new HashMap<String, Object>() { { put("Error", e.getMessage()); }},
                            HttpStatus.GONE);
                });
    }
}

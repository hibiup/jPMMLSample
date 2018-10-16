package samples.jpmml.controller.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import samples.jpmml.controller.RealtimeScoringController;
import samples.jpmml.controller.trait.RealtimeScoring;
import samples.jpmml.Service.UploadFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static samples.jpmml.Service.UploadFile.MODE.Create;
import static samples.jpmml.Service.UploadFile.MODE.Overwrite;

@RestController
public class RealtimeScoringControllerImpl implements RealtimeScoringController, RealtimeScoring {

    @Autowired UploadFile uploadFile;

    @Override
    public CompletableFuture<ResponseEntity<List<Map<String, Object>>>> release(
            @RequestPart(value = "model") List<MultipartFile> files
    ) {
        ResponseEntity<List<Map<String, Object>>> entity =  new ResponseEntity(uploadFile.upload(files, Create), HttpStatus.OK);
        return completedFuture(entity);
    }

    @Override
    public CompletableFuture<ResponseEntity<Map<String, Object>>> refresh (
            @RequestPart(value = "model") List<MultipartFile> files
    ) {
        List<Map<String, Object>> uploaded =  uploadFile.upload(files, Overwrite);

        return completedFuture(uploaded.get(0))
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

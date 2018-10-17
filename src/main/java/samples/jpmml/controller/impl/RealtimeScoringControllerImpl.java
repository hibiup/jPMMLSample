package samples.jpmml.controller.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import samples.jpmml.service.RepositoryManager;
import samples.jpmml.controller.RealtimeScoringController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static samples.jpmml.service.impl.UploadFile.MODE.Create;
import static samples.jpmml.service.impl.UploadFile.MODE.Update;

@RestController
public class RealtimeScoringControllerImpl implements RealtimeScoringController, RealtimeScoring {
    @Autowired
    Executor executor;
    @Autowired
    RepositoryManager repoManager;

    @Override
    public CompletableFuture<ResponseEntity<List<Map<String, Object>>>> release(
            @RequestPart(value = "model") List<MultipartFile> files
    ) {
        CompletableFuture f = supplyAsync(() -> {
            List<Map<String, Object>> result = new ArrayList();
            List<CompletableFuture> futures =  repoManager.upload(files, Create);
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()])).join();
            futures.forEach(s -> result.add((Map<String, Object>) s.join()));
            return result;
        }, executor);

        return completedFuture(new ResponseEntity(f.join(), HttpStatus.OK));
    }

    @Override
    public CompletableFuture<ResponseEntity<Map<String, Object>>> refresh (
            @RequestPart(value = "model") List<MultipartFile> files
    ) {
        List<CompletableFuture> futures =  repoManager.upload(files, Update);
        Map<String, Object> result = (Map<String, Object>) futures.get(0).join();
        if(result.containsKey("FAILED"))
            return completedFuture(new ResponseEntity(result, HttpStatus.GONE));
        else
            return completedFuture(new ResponseEntity(result, HttpStatus.OK));
    }

    @Override
    public CompletableFuture<ResponseEntity<Map<String, Object>>> retire(@PathVariable(value = "name") String name) {
        Object s = repoManager.retire(name).join();

        if (s == null)
            return completedFuture(new ResponseEntity(HttpStatus.OK));
        else
            return completedFuture(new ResponseEntity(HttpStatus.GONE));
    }
}

package samples.jpmml.controller.impl;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import samples.jpmml.service.ModelManager;
import samples.jpmml.service.RepositoryManager;
import samples.jpmml.controller.RealtimeScoringController;
import samples.jpmml.service.impl.RealtimeScoringService;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import static samples.jpmml.service.impl.UploadFile.MODE.Create;
import static samples.jpmml.service.impl.UploadFile.MODE.Update;

@RestController
public class RealtimeScoringControllerImpl implements RealtimeScoringController/*, RealtimeScoring*/ {
    @Autowired
    RepositoryManager repoManager;

    @Autowired
    ModelManager<Map<String, Number>, Map<String, Object>> modelManager;

    @Override
    public CompletableFuture<ResponseEntity> scoring(
            @PathVariable(value = "name") String name,
            @RequestBody Map<String, Number> input) {
        return modelManager.scoring(name, input)
                .thenApply(result ->(result instanceof Exception)?
                        new ResponseEntity(result, HttpStatus.BAD_REQUEST): new ResponseEntity(result, HttpStatus.OK)
                );
    }

    @Override
    public CompletableFuture<ResponseEntity> release(
            @RequestPart(value = "model") MultipartFile file
    ) {
        return repoManager.uploadSingle(file, Create).thenApply(result -> {
            if(result instanceof Exception)
                return new ResponseEntity(result, HttpStatus.CONFLICT);
            else
                return new ResponseEntity(result, HttpStatus.OK);
        });
    }

    @Override
    public CompletableFuture<ResponseEntity> refresh (
            @RequestPart(value = "model") MultipartFile file
    ) {
        return repoManager.uploadSingle(file, Update).thenApply(result -> {
            if(result instanceof Exception)
                return new ResponseEntity(result, HttpStatus.GONE);
            else
                return new ResponseEntity(result, HttpStatus.OK);
        });
    }

    @Override
    public CompletableFuture<ResponseEntity> retire(@PathVariable(value = "name") String name) {
        return repoManager.retire(name).thenApply(result -> {
            if(result instanceof Exception)
                return new ResponseEntity(result, HttpStatus.GONE);
            else
                return new ResponseEntity(result, HttpStatus.OK);
        });
    }
}

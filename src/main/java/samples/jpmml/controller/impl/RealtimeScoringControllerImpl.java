package samples.jpmml.controller.impl;

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

import java.util.HashMap;
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
                /** handle 的插入位子会影响对结果的抓取，如果将 handle　移入 scoring. 例如插入 predict 之后会引起其他问题．
                 * 参考 ReadtimeScoring.java 中的说明． */
                .handle((s, t) ->  (t == null)?s:t)
                .thenApply(result ->(result instanceof Exception)?
                        new ResponseEntity(result, HttpStatus.BAD_REQUEST): new ResponseEntity(result, HttpStatus.OK)
                );
    }

    @Override
    public CompletableFuture<ResponseEntity> release(
            @RequestPart(value = "model") MultipartFile file
    ) {
        return repoManager.uploadSingle(file, Create).thenApply(result ->
            (result instanceof Exception)? new ResponseEntity(result, HttpStatus.CONFLICT):new ResponseEntity(result, HttpStatus.OK)
        );
    }

    @Override
    public CompletableFuture<ResponseEntity> refresh (
            @RequestPart(value = "model") MultipartFile file
    ) {
        return repoManager.uploadSingle(file, Update).thenApply(result ->
                (result instanceof Exception)? new ResponseEntity(result, HttpStatus.GONE):new ResponseEntity(result, HttpStatus.OK)
        );
    }

    @Override
    public CompletableFuture<ResponseEntity> retire(@PathVariable(value = "name") String name) {
        return repoManager.retire(name).thenApply(result ->
                (result instanceof Exception)? new ResponseEntity(result, HttpStatus.GONE):new ResponseEntity(result, HttpStatus.OK)
        );
    }
}

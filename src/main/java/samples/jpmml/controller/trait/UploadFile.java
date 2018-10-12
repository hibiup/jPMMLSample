package samples.jpmml.controller.trait;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import samples.jpmml.controller.RealtimeScoringController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public interface UploadFile extends RealtimeScoringController {
    final Logger logger = LogManager.getLogger(UploadFile.class);

    default public CompletableFuture<ResponseEntity<Map<String, Object>>> release(
            List<MultipartFile> files
    ) {
        List<String> result = new ArrayList<String>();

        List<CompletableFuture> futures = files.stream()
                .map(file -> CompletableFuture.completedFuture(ModelRepositoryManager.save(file, getRepositoryLocation()))
                .thenApply(s -> result.add(s)))
                .collect(Collectors.toList());

        ResponseEntity<Map<String, Object>> entity =  new ResponseEntity(result, HttpStatus.OK);
        return CompletableFuture.completedFuture(entity);
    }

    String getRepositoryLocation();
}

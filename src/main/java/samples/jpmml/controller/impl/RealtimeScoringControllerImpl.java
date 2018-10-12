package samples.jpmml.controller.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import samples.jpmml.controller.RealtimeScoringController;
import samples.jpmml.controller.trait.RealtimeScoring;
import samples.jpmml.controller.trait.ModelRepositoryManager;
import samples.jpmml.controller.trait.UploadFile;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

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
    public CompletableFuture<ResponseEntity<Map<String, Object>>> release(
            @RequestPart(value = "model") List<MultipartFile> files
    ) {
        return UploadFile.super.release(files);
    }
}

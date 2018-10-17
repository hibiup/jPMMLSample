package samples.jpmml.service;

import org.springframework.web.multipart.MultipartFile;
import samples.jpmml.service.impl.UploadFile;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface RepositoryManager {
    String getRepositoryLocation();

    List<CompletableFuture> upload(
            List<MultipartFile> files, UploadFile.MODE mode
    );

    CompletableFuture retire(String filename);
}

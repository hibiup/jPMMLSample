package samples.jpmml.Service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ModelRepositoryManager {
    List<CompletableFuture> upload(
            List<MultipartFile> files, UploadFile.MODE mode
    );

    CompletableFuture retire(String filename);
}

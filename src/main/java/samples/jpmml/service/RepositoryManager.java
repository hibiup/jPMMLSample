package samples.jpmml.service;

import org.dmg.pmml.PMML;
import org.springframework.web.multipart.MultipartFile;
import samples.jpmml.service.impl.UploadFile;

import java.util.concurrent.CompletableFuture;

public interface RepositoryManager {

    CompletableFuture<Object> uploadSingle( MultipartFile file, UploadFile.MODE mode );

    CompletableFuture<Object> retire(String filename);

    PMML getModel(String modelName);
}

package samples.jpmml.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executor;

@Service
public class FileUploadService implements UploadFile, DeleteFile {
    @Value("${model.repository.location}") String model_repo_location;

    @Autowired Executor executor;

    @Override
    public String getRepositoryLocation() {
        return model_repo_location;
    }

    @Override
    public Executor getExecutorService() {
        return executor;
    }
}

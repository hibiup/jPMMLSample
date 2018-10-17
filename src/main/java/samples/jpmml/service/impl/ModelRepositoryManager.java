package samples.jpmml.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executor;

@Service
public class ModelRepositoryManager implements UploadFile, DeleteFile {
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

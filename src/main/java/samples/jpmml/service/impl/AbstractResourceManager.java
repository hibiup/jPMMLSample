package samples.jpmml.service.impl;

import org.springframework.beans.factory.annotation.Value;
import samples.jpmml.service.RepositoryLocationService;


public class AbstractResourceManager implements RepositoryLocationService {
    @Value("${model.repository.location}") String model_repo_location;

    @Override
    public String getRepositoryLocation() {
        return model_repo_location;
    }
}

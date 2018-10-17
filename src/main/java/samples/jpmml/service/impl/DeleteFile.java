package samples.jpmml.service.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Async;
import samples.jpmml.service.RepositoryLocationService;
import samples.jpmml.service.RepositoryManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.concurrent.CompletableFuture;

public interface DeleteFile extends RepositoryManager, RepositoryLocationService {
    Logger logger = LogManager.getLogger(DeleteFile.class);

    @Async
    default CompletableFuture retire(String modelName) {
        CompletableFuture f = CompletableFuture.runAsync(() -> {
            try {
                delete(modelName );
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        });

        CompletableFuture resultHandler = f.handle((s,t) -> (t == null)?s: t);

        return resultHandler;
    }

    default void delete(String file) throws FileNotFoundException {
        String path = getRepositoryLocation() + "/" + file + ".pmml";
        logger.debug(path);

        File modelFile = new File(path);

        if (modelFile.exists()) {
            modelFile.delete();
        }

        else throw new FileNotFoundException(path);
    }
}

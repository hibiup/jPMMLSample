package samples.jpmml.service.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.multipart.MultipartFile;
import samples.jpmml.service.RepositoryLocationService;
import samples.jpmml.service.RepositoryManager;

import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.util.concurrent.CompletableFuture;
public interface UploadFile extends RepositoryManager, RepositoryLocationService {
    Logger logger = LogManager.getLogger(UploadFile.class);

    enum MODE {
        Update, Create
    }

    @Async
    default CompletableFuture<Object> uploadSingle(
            MultipartFile file, MODE mode
    ) {
        CompletableFuture<String> stage1 = CompletableFuture.supplyAsync(() ->
                save(file, getRepositoryLocation(), mode)
        );

        CompletableFuture<Object> resultHandler = stage1.handle((s, t) ->
                (t == null)?s:t
        );

        return resultHandler;
    }

    default String save(MultipartFile file, String location, MODE mode) throws RuntimeException {
        String path = location + "/" + file.getOriginalFilename();
        logger.debug(path);
        File outputFile = new File(path);
        try {
            if (outputFile.exists()) {
                if (MODE.Update != mode)
                    throw new FileAlreadyExistsException(path);
            }
            else if (MODE.Create != mode)
                throw new FileNotFoundException(path);

            outputFile.createNewFile();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }

        try(InputStream inputStream  = file.getInputStream()) {
            FileOutputStream fileOutputStream = new FileOutputStream(outputFile, false);

            byte[] buf = new byte[1024];
            int numRead = 0;
            while ((numRead = inputStream.read(buf)) >= 0) {
                fileOutputStream.write(buf, 0, numRead);
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
        return path;
    }
}

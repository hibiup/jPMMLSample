package samples.jpmml.Service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public interface UploadFile extends ModelRepositoryManager{
    Logger logger = LogManager.getLogger(UploadFile.class);

    enum MODE {
        Update, Create
    }

    default List<CompletableFuture> upload(
            List<MultipartFile> files, MODE mode
    ) {
        List<CompletableFuture> futures = files.stream().map(file -> {
            CompletableFuture stage1 = CompletableFuture.supplyAsync(() ->
                    save(file, getRepositoryLocation(), mode), getExecutorService()
            );

            CompletableFuture resultHandler = stage1.handleAsync((s, t) -> {
                Map<String, Object> result = new HashMap();
                if (t != null)
                    result.put("FAILED", t);
                else
                    result.put("SUCCESS", s);
                return result;
            }, getExecutorService() );

            return resultHandler;
        }).collect(Collectors.toList());

        return futures;
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

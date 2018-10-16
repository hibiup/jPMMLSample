package samples.jpmml.Service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import samples.jpmml.Service.UploadFile.*;

public interface ModelRepositoryManager {
    Logger logger = LogManager.getLogger(ModelRepositoryManager.class);

    default String save(MultipartFile file, String location, MODE mode) throws RuntimeException {
        String path = location + "/" + file.getOriginalFilename();
        logger.debug(path);
        File outputFile = new File(path);
        try {
            if (outputFile.exists()) {
                if (MODE.Overwrite != mode)
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
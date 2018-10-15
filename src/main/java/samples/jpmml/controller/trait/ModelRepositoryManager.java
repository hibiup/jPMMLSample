package samples.jpmml.controller.trait;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.FileAlreadyExistsException;

public interface ModelRepositoryManager {
    enum MODE {
        Overwrite, Update, Create
    }

    Logger logger = LogManager.getLogger(ModelRepositoryManager.class);

    static String save(MultipartFile file, String location, MODE mode) throws RuntimeException {
        String path = location + "/" + file.getOriginalFilename();
        logger.debug(path);
        File outputFile = new File(path);
        try {
            if (outputFile.exists())
                if (MODE.Overwrite != mode)
                    throw new FileAlreadyExistsException(path);
            else if (MODE.Update == mode)
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

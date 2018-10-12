package samples.jpmml.controller.trait;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.multipart.MultipartFile;

public interface ModelRepositoryManager {
    Logger logger = LogManager.getLogger(ModelRepositoryManager.class);

    static String save(MultipartFile file, String location) {
        String path = location + "/" + file.getOriginalFilename();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        logger.debug(path);
        return path;
    }
}

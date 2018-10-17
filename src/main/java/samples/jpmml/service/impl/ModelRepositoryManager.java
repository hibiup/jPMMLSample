package samples.jpmml.service.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dmg.pmml.PMML;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static org.jpmml.model.PMMLUtil.unmarshal;

@Service
public class ModelRepositoryManager extends AbstractResourceManager implements UploadFile, DeleteFile {
    Logger logger = LogManager.getLogger(ModelRepositoryManager.class);

    static Map<String, PMML> modelCache = new HashMap();

    @Override
    public PMML getModel(String modelName) {
        PMML model = modelCache.get(modelName);
        if(model == null) {
            String pmmlFilePath = this.getRepositoryLocation() + "/" + modelName + ".pmml";

            try (InputStream is = new FileInputStream((new File(pmmlFilePath)))) {
                model = unmarshal(is);
                cacheModel(modelName, model);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                throw new PMMLLoadingException(e);
            }
        }
        return model;
    }

    private void cacheModel(String modelName, PMML model) {
        modelCache.put(modelName, model);
    }

    private void removeFromCache(String modelName) {
        modelCache.remove(modelName);
    }

    @Override
    public String save(MultipartFile file, String location, MODE mode) throws RuntimeException {
        String path = UploadFile.super.save(file, location, mode);
        removeFromCache(file.getName());

        return path;
    }

    @Override public void delete(String file) throws FileNotFoundException {
        DeleteFile.super.delete(file);
        removeFromCache(file);
    }
}

package samples.jpmml.Service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

public interface UploadFile extends ModelRepositoryManager{
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

    String getRepositoryLocation();
    Executor getExecutorService();
}

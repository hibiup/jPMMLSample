package samples.jpmml.controller.trait;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import samples.jpmml.controller.RealtimeScoringController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import static samples.jpmml.controller.trait.ModelRepositoryManager.MODE.Create;

public interface UploadFile extends RealtimeScoringController {
    Logger logger = LogManager.getLogger(UploadFile.class);

    default CompletableFuture<ResponseEntity<Map<String, Object>>> release(
            List<MultipartFile> files
    ) {
        List<Map<String, Object>> result = new ArrayList();

        /** 利用 CompletableFuture 并行存储上载的文件 */
        List<CompletableFuture> futures = files.stream()
            /** 1) 将 List 中的文件数据逐个取出，(异步)映射到 CompletableFuture.completedFuture 所指定的函数。
             *     ModelRepositoryManager.save 返回 String 类型的结果。*/
            .map(file -> CompletableFuture.supplyAsync(
                () -> ModelRepositoryManager.save(file, getRepositoryLocation(), Create),getExecutorService()
            ))
            /** 2) 收集结果转成 List 返回。*/
            .collect(Collectors.toList());

        /** 3) join() 函数会阻塞等待，直到 allOf() 函数监控的 future 对象全都完成为止。 */
        try { CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()])).join(); } catch(Throwable t){/* 忽略顶级异常 */}
        finally {
            futures.forEach(cf -> {
                /** 4) 捕获每一个异常．*/
                try {
                    result.add(new HashMap<String, Object>() { { put("SUCCESS", cf.join()); }});
                }
                catch(CompletionException e) {
                    logger.error(e.getCause().getMessage(), e);
                    result.add(new HashMap<String, Object>() { { put("FAILED", e); }});
                }
            });
        }

        /** 6) 返回结果集．*/
        ResponseEntity<Map<String, Object>> entity =  new ResponseEntity(result, HttpStatus.OK);
        return CompletableFuture.completedFuture(entity);
    }

    String getRepositoryLocation();
    Executor getExecutorService();
}
package samples.jpmml.controller.trait;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import samples.jpmml.controller.RealtimeScoringController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

public interface UploadFile extends RealtimeScoringController {
    final Logger logger = LogManager.getLogger(UploadFile.class);

    default public CompletableFuture<ResponseEntity<Map<String, Object>>> release(
            List<MultipartFile> files
    ) {
        List<String> result = new ArrayList<String>();

        /** 利用 CompletableFuture 并行存储上载的文件 */
        List<CompletableFuture> futures = files.stream()
            /** 1) 将 List 中的文件数据逐个取出，(异步)映射到 CompletableFuture.completedFuture 所指定的函数。
             *     ModelRepositoryManager.save 返回 String 类型的结果。*/
            .map(file -> CompletableFuture.supplyAsync(
                () -> { return ModelRepositoryManager.save(file, getRepositoryLocation()); },getExecutorService()
            )
            /** 2) 然后(异步)将运算结果(String)添加到 result 中。 */
            .thenApplyAsync(s -> result.add(s), getExecutorService()))
            /** 3) 收集结果转成 List 返回。*/
            .collect(Collectors.toList());

        /** 4) join() 函数会阻塞等待，直到 allOf() 函数监控的 future 对象全都完成为止。 */
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()])).join();


        ResponseEntity<Map<String, Object>> entity =  new ResponseEntity(result, HttpStatus.OK);
        return CompletableFuture.completedFuture(entity);
    }

    String getRepositoryLocation();
    Executor getExecutorService();
}

package samples.jpmml.service.impl;

import org.jpmml.evaluator.ModelEvaluator;
import org.springframework.scheduling.annotation.Async;
import samples.jpmml.service.ModelManager;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public interface RealtimeScoring<T, U> extends ModelManager<T, U> {
    @Override
    @Async
    default CompletableFuture scoring(String modelName, T data) {
        CompletableFuture future =  CompletableFuture.completedFuture(modelName)
                .thenApply(model -> getModelEvaluator(model))
                .thenApply(evaluator -> predict(evaluator, data) );
        /** 注意!
         *  handler　会被插入到以上 getModelEvaluator 和 predict 函数中去，如果 handler 希望避免抛出异常，那么就要
         *  构造能够满足返回类型的数据，否则 predict "返回" Exception 时会抛出类型不匹配（期待Map）错误. */
        //CompletableFuture handler = future.handle((s, t) ->  (t == null)?s:new HashMap() {{put("Error",t);}});
        //return handler;
        return future;
    }

    ModelEvaluator getModelEvaluator(String modelName) throws PMMLLoadingException;

    U predict(ModelEvaluator evaluator, T data);

}

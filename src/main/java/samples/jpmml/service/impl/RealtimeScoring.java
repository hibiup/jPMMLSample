package samples.jpmml.service.impl;

import org.jpmml.evaluator.ModelEvaluator;
import org.springframework.scheduling.annotation.Async;
import samples.jpmml.service.ModelManager;

import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.completedFuture;

public interface RealtimeScoring<T, U> extends ModelManager<T, U> {
    @Override
    @Async
    default CompletableFuture scoring(String modelName, T data) {
        CompletableFuture future =  CompletableFuture.completedFuture(modelName)
                .thenApply(model -> getModelEvaluator(model))
                .thenApply(evaluator -> predict(evaluator, data) );
        CompletableFuture handler = future.handleAsync((s, t) ->  (t == null)?s:t);
        return handler;
    }

    ModelEvaluator getModelEvaluator(String modelName) throws PMMLLoadingException;

    U predict(ModelEvaluator evaluator, T data);

}

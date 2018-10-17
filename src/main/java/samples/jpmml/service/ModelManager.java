package samples.jpmml.service;


import java.util.concurrent.CompletableFuture;

public interface ModelManager<T, U> {
    CompletableFuture<U> scoring (String modelName, T data);
}

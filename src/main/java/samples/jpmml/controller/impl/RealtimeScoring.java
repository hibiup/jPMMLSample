package samples.jpmml.controller.impl;

import samples.jpmml.service.impl.RealtimeScoringService;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface RealtimeScoring {
    default CompletableFuture scoring(RealtimeScoringService realtimeScoringService, Map<String, Number> input, String model) {
        return CompletableFuture.completedFuture(input).thenApply( i ->
                realtimeScoringService.scoring(model, input)
        );
    }
}

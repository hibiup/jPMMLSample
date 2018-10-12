package samples.jpmml.controller.trait;

import samples.jpmml.controller.RealtimeScoringController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface RealtimeScoring extends RealtimeScoringController {
    default CompletableFuture<Map<String, Object>> scoring() {
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("Message", "Hello Spring Boot 2");
        return CompletableFuture.completedFuture(model);
    }
}

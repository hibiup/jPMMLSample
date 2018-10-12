package samples.jpmml.controller.trait;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import samples.jpmml.controller.RealtimeScoringController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface RealtimeScoring extends RealtimeScoringController {
    default CompletableFuture<ResponseEntity<Map<String, Object>>> scoring() {
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("score", "2");

        ResponseEntity<Map<String, Object>> entity =  new ResponseEntity(model, HttpStatus.OK);
        return CompletableFuture.completedFuture(entity);
    }
}

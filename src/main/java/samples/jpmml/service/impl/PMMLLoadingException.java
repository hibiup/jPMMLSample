package samples.jpmml.service.impl;

public class PMMLLoadingException extends RuntimeException {
    public PMMLLoadingException(Exception e) {
        super(e);
    }
}

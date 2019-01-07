package sr.obep.core.pipeline.explanation;

public class ExplanationException extends RuntimeException {
    public ExplanationException(String unsupported_reasoner) {
        super(unsupported_reasoner);
    }
}

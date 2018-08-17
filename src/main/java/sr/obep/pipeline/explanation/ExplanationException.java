package sr.obep.pipeline.explanation;

public class ExplanationException extends RuntimeException {
    public ExplanationException(String unsupported_reasoner) {
        super(unsupported_reasoner);
    }
}

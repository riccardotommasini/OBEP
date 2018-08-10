package sr.obep.implementations;

public class ExplanationException extends RuntimeException {
    public ExplanationException(String unsupported_reasoner) {
        super(unsupported_reasoner);
    }
}

package sr.obep.engine;

public class OBEPEngineFactory {

    public static OBEPEngine create() {
        return new OBEPEngineImpl();
    }
}

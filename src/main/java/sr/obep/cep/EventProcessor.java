package sr.obep.cep;

import sr.obep.data.SemanticEvent;
import sr.obep.programming.Program;

public interface EventProcessor {

    public void init(OBEPEngine obep);

    public void registerQuery(Program q);

    public void sendEvent(SemanticEvent se);

}
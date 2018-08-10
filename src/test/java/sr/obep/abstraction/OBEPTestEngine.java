package sr.obep.abstraction;

import lombok.Getter;
import org.semanticweb.owlapi.model.OWLOntology;
import sr.obep.data.SemanticEvent;
import sr.obep.programming.parser.OBEPQueryImpl;
import sr.obep.programming.parser.QueryConsumer;

@Getter
public class OBEPTestEngine implements OBEPEngine {

    private SemanticEvent receivedEvent;

    @Override
    public void init(OBEPEngine obep) {

    }

    @Override
    public void setOntology(OWLOntology o) {
        // TODO Auto-generated method stub

    }

    @Override
    public void registerQuery(OBEPQueryImpl q, QueryConsumer c) {

    }


    @Override
    public void sendEvent(SemanticEvent se) {
        this.receivedEvent = se;

    }

}

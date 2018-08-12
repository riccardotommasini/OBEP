package sr.obep.explanation;

import openllet.owlapi.OpenlletReasoner;
import openllet.owlapi.OpenlletReasonerFactory;
import openllet.owlapi.explanation.PelletExplanation;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.OntologyCopy;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import sr.obep.data.events.ContentImpl;
import sr.obep.data.events.SemanticEvent;
import sr.obep.processors.EventProcessor;

import java.util.Set;

public class ExplainerImpl implements Explainer {

    private final String explainer_time = "timestamp.explainer";
    private OWLOntologyManager ma;
    private EventProcessor next;

    public ExplainerImpl() {
        ma = OWLManager.createOWLOntologyManager();
    }

    @Override
    public Set<Set<OWLAxiom>> explain(OWLOntology ontology, OWLNamedIndividual message, OWLClass c) throws ExplanationException {
        PelletExplanation.setup();
        OWLReasoner reasoner = OpenlletReasonerFactory.getInstance().createReasoner(ontology);
        PelletExplanation expGen = new PelletExplanation((OpenlletReasoner) reasoner);
        return expGen.getInstanceExplanations(message, c);
    }

    @Override
    public void send(SemanticEvent e) {
        try {
            OWLOntology copy = this.ma.copyOntology(e.getContent().asOWLOntology(), OntologyCopy.SHALLOW);
            OWLNamedIndividual event = e.getEventInvididual();

            explain(copy, event, e.getType()).forEach(axioms -> {
                SemanticEvent semanticEvent = e.copy();
                semanticEvent.setContent(new ContentImpl(axioms));
                semanticEvent.put(this.explainer_time, System.currentTimeMillis());
                this.next.send(semanticEvent);
            });

        } catch (OWLOntologyCreationException e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public EventProcessor pipe(EventProcessor p) {
        return this.next = p;
    }

}

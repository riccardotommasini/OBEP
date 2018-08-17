package sr.obep.pipeline.explanation;

import openllet.owlapi.OpenlletReasoner;
import openllet.owlapi.OpenlletReasonerFactory;
import openllet.owlapi.explanation.PelletExplanation;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.OntologyCopy;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import sr.obep.data.content.ContentOntology;
import sr.obep.data.events.RawEvent;
import sr.obep.pipeline.processors.EventProcessor;

import java.util.Set;

public class ExplainerImpl implements Explainer {

    private final String explainer_time = "timestamp_explainer";
    private EventProcessor next;

    @Override
    public Set<Set<OWLAxiom>> explain(OWLOntology ontology, OWLNamedIndividual message, OWLClass c) throws ExplanationException {
        PelletExplanation.setup();
        OWLReasoner reasoner = OpenlletReasonerFactory.getInstance().createReasoner(ontology);
        PelletExplanation expGen = new PelletExplanation((OpenlletReasoner) reasoner);
        return expGen.getInstanceExplanations(message, c);
    }

    @Override
    public void send(RawEvent e) {
        try {
            OWLOntology copy = OWLManager.createOWLOntologyManager().copyOntology(e.getContent().asOWLOntology(), OntologyCopy.SHALLOW);
            OWLNamedIndividual event = e.getEventInvididual();

            explain(copy, event, e.getType()).forEach(axioms -> {
                RawEvent rawEvent = e.copy();
                rawEvent.setContent(new ContentOntology(axioms));
                rawEvent.put(this.explainer_time, System.currentTimeMillis());
                this.next.send(rawEvent);
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

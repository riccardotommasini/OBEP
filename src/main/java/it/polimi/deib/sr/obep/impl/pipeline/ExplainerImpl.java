package it.polimi.deib.sr.obep.impl.pipeline;

import it.polimi.deib.sr.obep.impl.RawEvent;
import it.polimi.deib.sr.obep.impl.content.ContentOntology;
import it.polimi.deib.sr.obep.core.pipeline.explanation.Explainer;
import it.polimi.deib.sr.obep.core.pipeline.explanation.ExplanationException;
import it.polimi.deib.sr.obep.core.pipeline.processors.EventProcessor;
import openllet.owlapi.OpenlletReasoner;
import openllet.owlapi.OpenlletReasonerFactory;
import openllet.owlapi.explanation.PelletExplanation;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.OntologyCopy;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import java.util.Set;

public class ExplainerImpl implements Explainer {

    private final String explainer_time = "timestamp_explainer";
    private EventProcessor next;

    private final OWLOntology tbox;

    public ExplainerImpl(OWLOntology ebox) {
        this.tbox = ebox;
    }

    @Override
    public Set<Set<OWLAxiom>> explain(OWLOntology ontology, OWLNamedIndividual message, OWLClass c) throws ExplanationException {
        PelletExplanation.setup();
        OWLReasoner reasoner = OpenlletReasonerFactory.getInstance().createReasoner(ontology);
        PelletExplanation expGen = new PelletExplanation((OpenlletReasoner) reasoner);
        Set<Set<OWLAxiom>> instanceExplanations = expGen.getInstanceExplanations(message, c);
        return instanceExplanations;
    }

    @Override
    public void send(RawEvent e) {
        try {
            final OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            OWLOntology copy = manager.copyOntology(tbox, OntologyCopy.SHALLOW);
            OWLNamedIndividual event = e.getEventInvididual();
            copy.add(e.getContent().asOWLAxioms());

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

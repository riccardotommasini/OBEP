package sr.obep.abstration;

import lombok.Getter;
import lombok.Setter;
import openllet.owlapi.OpenlletReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.OntologyCopy;
import org.semanticweb.owlapi.reasoner.InferenceDepth;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import sr.obep.data.events.ContentImpl;
import sr.obep.data.events.SemanticEvent;
import sr.obep.processors.EventProcessor;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by pbonte on 03/11/2016.
 */

@Setter
@Getter
public class AbstracterImpl implements Abstracter {

    private final OWLOntology tbox;
    private EventProcessor next;
    private final String abstracter_time = "timestamp.abstracter";

    public AbstracterImpl(OWLOntology tbox) {
        this.tbox = tbox;
    }

    public Set<OWLClass> lift(OWLOntology copy, OWLNamedIndividual event) {
        OWLReasoner reasoner = OpenlletReasonerFactory.getInstance().createReasoner(copy);
        reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
        reasoner.flush();
        return reasoner.getTypes(event, InferenceDepth.ALL).entities().collect(Collectors.toSet());

    }

    @Override
    public void send(SemanticEvent e) {
        try {
            final OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            OWLOntology copy = manager.copyOntology(tbox, OntologyCopy.SHALLOW);
            copy.add(e.getContent().asOWLAxioms());

            lift(copy, e.getEventInvididual()).forEach(c -> {
                SemanticEvent event = e.copy();
                event.put(abstracter_time, System.currentTimeMillis());
                event.setType(c);
                event.setContent(new ContentImpl(copy));
                next.send(event);
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

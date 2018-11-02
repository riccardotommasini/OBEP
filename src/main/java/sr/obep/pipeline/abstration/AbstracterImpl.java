package sr.obep.pipeline.abstration;

import lombok.Getter;
import lombok.Setter;
import openllet.owlapi.OWL;
import openllet.owlapi.OpenlletReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.OntologyCopy;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import sr.obep.data.events.RawEvent;
import sr.obep.pipeline.processors.EventProcessor;

import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.base.Predicates.not;

/**
 * Created by pbonte on 03/11/2016.
 */

@Setter
@Getter
public class AbstracterImpl implements Abstracter {

    private final OWLOntology tbox;
    private EventProcessor next;
    private final String abstracter_time = "timestamp_abstracter";

    public AbstracterImpl(OWLOntology tbox) {
        this.tbox = tbox;
    }

    public Set<OWLClass> lift(OWLOntology copy, OWLNamedIndividual event) {
        OWLReasoner reasoner = OpenlletReasonerFactory.getInstance().createReasoner(copy);
        reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
        reasoner.flush();
        NodeSet<OWLClass> types = reasoner.getTypes(event, false);
        return types.entities().filter(not(OWL.Thing::equals)).collect(Collectors.toSet());

    }

    @Override
    public void send(RawEvent e) {
        try {
            final OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            OWLOntology copy = manager.copyOntology(tbox, OntologyCopy.SHALLOW);
            copy.add(e.getContent().asOWLAxioms());


            OWLNamedIndividual eventInvididual = e.getEventInvididual();
            lift(copy, eventInvididual).forEach(c -> {
                RawEvent event = e.copy();
                event.put(abstracter_time, System.currentTimeMillis());
                event.setType(c);
                // event.setContent(new ContentOntology(copy));
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

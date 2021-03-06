package it.polimi.deib.sr.obep.impl.pipeline;
import it.polimi.deib.sr.obep.core.pipeline.processors.EventProcessor;
import lombok.Getter;
import lombok.Setter;
import openllet.owlapi.OWL;
import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.OntologyCopy;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import it.polimi.deib.sr.obep.impl.RawEvent;
import it.polimi.deib.sr.obep.core.pipeline.abstration.Abstracter;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.util.*;

import java.util.ArrayList;
import java.util.Set;
import java.util.List;
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
    private final OWLReasonerFactory factory = new ReasonerFactory();

    public AbstracterImpl(OWLOntology tbox) {
        this.tbox = tbox;
//        OWLReasoner reasoner=factory.createReasoner(tbox);
//        List<InferredAxiomGenerator<? extends OWLAxiom>> generators= new ArrayList<>();
//        generators.add(new InferredSubClassAxiomGenerator());
//        generators.add(new InferredClassAssertionAxiomGenerator());
//
//        InferredOntologyGenerator iog=new InferredOntologyGenerator(reasoner,generators);
//        iog.fillOntology(tbox.getOWLOntologyManager().getOWLDataFactory(), tbox);

    }

    public Set<OWLClass> lift(OWLOntology copy, OWLNamedIndividual event) {
        //  OWLReasoner reasoner = OpenlletReasonerFactory.getInstance().createReasoner(copy);

        OWLReasoner reasoner = factory.createReasoner(copy);

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

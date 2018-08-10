package sr.obep.abstration;

import lombok.Getter;
import lombok.Setter;
import openllet.owlapi.OpenlletReasonerFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.model.parameters.OntologyCopy;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import sr.obep.data.events.SemanticEvent;
import sr.obep.explanation.ExplainerImpl;
import sr.obep.processors.EventProcessor;
import sr.obep.explanation.Explainer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by pbonte on 03/11/2016.
 */

@Setter
@Getter
public class AbstracterImpl implements Abstracter {

    private final OWLOntology tbox;
    private final Explainer explainer;
    private EventProcessor next;

    public AbstracterImpl(OWLOntology tbox) {
        this.tbox = tbox;
        explainer = new ExplainerImpl();
    }

    @Override
    public List<SemanticEvent> lift(SemanticEvent abox) {
        final OWLOntologyManager manager = tbox.getOWLOntologyManager();
        List<SemanticEvent> res = new ArrayList<>();

        try {

            OWLOntology copy = manager.copyOntology(tbox, OntologyCopy.DEEP);
            OWLReasoner reasoner = OpenlletReasonerFactory.getInstance().createReasoner(copy);
            copy.add(abox.getAxioms());
            reasoner.flush();

            OWLNamedIndividual message = abox.getMessage();
            reasoner.getTypes(message, false).entities().forEach(c -> {
                SemanticEvent e = new SemanticEvent(copy.aboxAxioms(Imports.EXCLUDED).collect(Collectors.toSet()), message, abox.getTimeStamp(), c.toStringID());
                e.setType(c);
                res.add(e);
            });

        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }

        //send event back to engie
        return res;
    }

    @Override
    public void send(SemanticEvent e) {
        lift(e).forEach(next::send);
    }

    @Override
    public EventProcessor pipe(EventProcessor p) {
        return this.next = p;
    }

}

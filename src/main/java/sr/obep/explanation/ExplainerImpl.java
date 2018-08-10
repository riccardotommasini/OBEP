package sr.obep.explanation;

import openllet.owlapi.OpenlletReasoner;
import openllet.owlapi.OpenlletReasonerFactory;
import openllet.owlapi.explanation.PelletExplanation;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import sr.obep.data.events.SemanticEvent;
import sr.obep.processors.EventProcessor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Set;

public class ExplainerImpl implements Explainer {

    EventProcessor next;
    private OWLOntology tbox;

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
            OWLOntologyManager ma = OWLManager.createOWLOntologyManager();
            OWLOntology ontology = ma.createOntology(e.getAxioms().stream());

            explain(ontology, e.getMessage(), e.getType()).forEach(axioms -> {
                SemanticEvent semanticEvent = new SemanticEvent(axioms, e.getMessage(), e.getTimeStamp(), e.getStream());
                semanticEvent.setType(e.getType());
                semanticEvent.setData(convert(ma, axioms));
                next.send(semanticEvent);
            });


        } catch (OWLOntologyCreationException e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public EventProcessor pipe(EventProcessor p) {
        return this;
    }


    protected OntModel convert(OWLOntologyManager manager, Set<OWLAxiom> axioms) {
        OntModel noReasoningModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        try {
            OWLOntology ontology = manager.createOntology();

            ontology.add(axioms.toArray(new OWLAxiom[axioms.size()]));
            noReasoningModel.getDocumentManager().setProcessImports(false);
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            try {
                manager.saveOntology(ontology, out);
            } catch (OWLOntologyStorageException e) {
            }

            try {
                noReasoningModel.read(new ByteArrayInputStream(out.toByteArray()), "RDF/XML");
            } catch (Exception e) {
            }


        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }
        return noReasoningModel;
    }
}

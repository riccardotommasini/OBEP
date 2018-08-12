package sr.obep.data.events;

import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.Model;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.Set;

public interface Content {


    OWLOntology asOWLOntology();

    Set<OWLAxiom> asOWLAxioms();

    Model asRDFModel();

    Graph asRDFGraph();

}

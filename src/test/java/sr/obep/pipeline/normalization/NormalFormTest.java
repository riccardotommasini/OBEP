package sr.obep.pipeline.normalization;

import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

public class NormalFormTest {
    IRI base = IRI.create("http://example.org#");

    @Test
    public void epl() throws OWLOntologyCreationException {

        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLOntology o = manager.createOntology(base);

        OWLClass A = factory.getOWLClass(base + "A");

        NormalForm normalForm = new SPARQLNormalForm("SELECT ?s ?o WHERE {?s <http://example.org#p> ?o }", A);

        System.out.println(normalForm.toString());
    }
}

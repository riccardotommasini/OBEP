package sr.obep.core.abs.exp.norm;

import junit.framework.TestCase;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import sr.obep.impl.RawEvent;
import sr.obep.impl.content.ContentOntology;
import sr.obep.impl.pipeline.AbstracterImpl;
import sr.obep.impl.pipeline.ExplainerImpl;
import sr.obep.impl.pipeline.SPARQLNormalForm;
import sr.obep.impl.pipeline.SPARQLNormalizer;
import sr.obep.core.pipeline.abstration.Abstracter;
import sr.obep.core.pipeline.explanation.Explainer;
import sr.obep.core.pipeline.normalization.NormalForm;
import sr.obep.core.pipeline.normalization.Normalizer;
import sr.obep.core.pipeline.processors.EventProcessor;
import sr.obep.core.utils.TestEventProcessor;

import java.util.*;

public class CompletePipelineTest extends TestCase {


    public void test0() throws OWLOntologyCreationException {

        IRI base = IRI.create("http://example.org#");

        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLOntology o = manager.createOntology(base);

        OWLClass A = factory.getOWLClass(base + "A");
        OWLClass B = factory.getOWLClass(base + "B");
        OWLObjectProperty p = factory.getOWLObjectProperty(base + "p");

        o.add(factory.getOWLDeclarationAxiom(A));
        o.add(factory.getOWLDeclarationAxiom(B));
        o.add(factory.getOWLDeclarationAxiom(p));
        OWLEquivalentClassesAxiom equivalentClassesAxiom = factory.getOWLEquivalentClassesAxiom(A,
                factory.getOWLObjectSomeValuesFrom(p, B));
        o.add(equivalentClassesAxiom);


        // ABOX axioms

        OWLNamedIndividual b = factory.getOWLNamedIndividual(base + "b");
        OWLNamedIndividual a = factory.getOWLNamedIndividual(base + "a");
        OWLObjectPropertyAssertionAxiom bpa = factory.getOWLObjectPropertyAssertionAxiom(p, a, b);
        OWLClassAssertionAxiom baB = factory.getOWLClassAssertionAxiom(B, b);

        Set<OWLAxiom> axioms = new HashSet<>();
        axioms.add(baB);
        axioms.add(bpa);

        Abstracter abstracter = new AbstracterImpl(o);

        Explainer explainer = new ExplainerImpl(o);

        Normalizer normalizer = new SPARQLNormalizer(o, new HashMap<>(), "");
        NormalForm normalForm = new SPARQLNormalForm("", " SELECT * WHERE {?s<http://example.org#p> ?o }", A);
        normalizer.addNormalForm(normalForm);

        List<RawEvent> actual = new ArrayList<>();

        abstracter.pipe(explainer).

                pipe(normalizer).

                pipe(new TestEventProcessor() {

                    @Override
                    public void send(RawEvent e) {
                        actual.add(e);
                    }

                    @Override
                    public EventProcessor pipe(EventProcessor p) {
                        return null;
                    }
                });

        RawEvent message = new RawEvent("http://example.org#a");
        message.setStream_uri("test1");
        message.setTimeStamp(System.currentTimeMillis());
        message.setContent(new ContentOntology(axioms));

        abstracter.send(message);

        Set<OWLAxiom> expected_explanation = new HashSet<>();

        expected_explanation.add(baB);
        expected_explanation.add(bpa);
        expected_explanation.add(equivalentClassesAxiom);

        assertEquals(1, actual.size());
        RawEvent actual_event = actual.get(0);

        assertEquals(A, actual_event.getType());

        assertEquals(3, actual_event.getContent().

                asOWLAxioms().

                size());

        assertTrue(actual_event.containsKey("timestamp_abstracter"));

        assertTrue(actual_event.containsKey("timestamp_explainer"));

        assertTrue(actual_event.containsKey("timestamp_normalizer"));

        assertTrue(actual_event.containsKey("s"));

        assertTrue(actual_event.containsKey("o"));

        assertEquals(actual_event.get("s"), a.

                toStringID());

        assertEquals(actual_event.get("o"), b.

                toStringID());


    }

}

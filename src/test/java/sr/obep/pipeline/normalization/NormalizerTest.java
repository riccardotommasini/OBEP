package sr.obep.pipeline.normalization;

import junit.framework.TestCase;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import sr.obep.TestEventProcessor;
import sr.obep.data.content.ContentOntology;
import sr.obep.data.events.RawEvent;
import sr.obep.pipeline.explanation.Explainer;
import sr.obep.pipeline.explanation.ExplainerImpl;
import sr.obep.pipeline.processors.EventProcessor;

import java.util.*;

public class NormalizerTest extends TestCase {


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
        Set<OWLAxiom> contentA = new HashSet<>();

        OWLNamedIndividual b = factory.getOWLNamedIndividual(base + "b");
        OWLNamedIndividual a = factory.getOWLNamedIndividual(base + "a");
        OWLObjectPropertyAssertionAxiom bpa = factory.getOWLObjectPropertyAssertionAxiom(p, a, b);
        OWLClassAssertionAxiom baB = factory.getOWLClassAssertionAxiom(B, b);

        o.add(baB, bpa);

        contentA.add(baB);
        contentA.add(bpa);
        contentA.add(equivalentClassesAxiom);


        Normalizer normalizer = new SPARQLNormalizer(o, new HashMap<>(), "");
        NormalForm normalForm = new SPARQLNormalForm("", "SELECT * WHERE {?s <http://example.org#p> ?o }", A);
        normalizer.addNormalForm(normalForm);

        List<Map<String, Object>> actual_normal_forms = normalizer.normalize(A, new ContentOntology(contentA));

        assertEquals(1, actual_normal_forms.size());
        assertEquals(2, actual_normal_forms.get(0).size());
        assertTrue(actual_normal_forms.get(0).containsKey("s"));
        assertEquals(actual_normal_forms.get(0).get("s"), a.toStringID());
        assertTrue(actual_normal_forms.get(0).containsKey("o"));
        assertEquals(actual_normal_forms.get(0).get("o"), b.toStringID());

    }

    @Test
    public static void test2() throws OWLOntologyCreationException {

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
        OWLEquivalentClassesAxiom owlEquivalentClassesAxiom = factory.getOWLEquivalentClassesAxiom(A,
                factory.getOWLObjectSomeValuesFrom(p, B));
        o.add(owlEquivalentClassesAxiom);

        //Input Semantic Event

        // add the axioms
        Set<OWLAxiom> axioms = new HashSet<>();

        OWLNamedIndividual b = factory.getOWLNamedIndividual(base + "b");
        OWLNamedIndividual event = factory.getOWLNamedIndividual(base + "a");
        axioms.add(factory.getOWLObjectPropertyAssertionAxiom(p, event, b));

        Set<OWLAxiom> expected_explanation = new HashSet<>();

        OWLObjectPropertyAssertionAxiom bpa = factory.getOWLObjectPropertyAssertionAxiom(p, event, b);
        OWLClassAssertionAxiom baB = factory.getOWLClassAssertionAxiom(B, b);

        axioms.add(factory.getOWLClassAssertionAxiom(B, b));

        o.add(axioms);

        RawEvent message = new RawEvent("http://example.org#a");
        message.setType(A);
        message.setStream_uri("test1");
        message.setTimeStamp(System.currentTimeMillis());
        message.setContent(new ContentOntology(o));

        //Expected Output
        expected_explanation.add(baB);
        expected_explanation.add(bpa);
        expected_explanation.add(owlEquivalentClassesAxiom);

        RawEvent eventA = new RawEvent("http://example.org#a");
        eventA.setContent(new ContentOntology(expected_explanation));
        eventA.setStream_uri("test1");
        eventA.setType(A);


        final Set<RawEvent> expected_events = new HashSet<>();
        expected_events.add(eventA);

        final List<RawEvent> actual_events = new ArrayList<>();

        TestEventProcessor tester = new TestEventProcessor() {

            @Override
            public void send(RawEvent e) {
                actual_events.add(e);
            }

            @Override
            public EventProcessor pipe(EventProcessor p) {
                return this;
            }
        };

        Explainer explainer = new ExplainerImpl(o);
        explainer.pipe(tester);

        explainer.send(message);

        assertEquals(1, actual_events.size());

        RawEvent rawEvent = actual_events.get(0);

        assertTrue(rawEvent.containsKey("timestamp.explainer"));
        assertTrue(rawEvent.containsKey("event_type"));
        assertTrue(rawEvent.get("event_type") != null);
        assertTrue(expected_explanation.containsAll(rawEvent.getContent().asOWLAxioms()));
        assertEquals(A, rawEvent.getType());

    }


}

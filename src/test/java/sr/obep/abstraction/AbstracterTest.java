package sr.obep.abstraction;

import junit.framework.TestCase;
import openllet.owlapi.OWL;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.OntologyCopy;
import sr.obep.utils.TestEventProcessor;
import sr.obep.pipeline.abstration.Abstracter;
import sr.obep.pipeline.abstration.AbstracterImpl;
import sr.obep.data.content.ContentOntology;
import sr.obep.data.events.RawEvent;
import sr.obep.pipeline.processors.EventProcessor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AbstracterTest extends TestCase {

    /**
     * Simple test that shows how abstracter works
     **/
    @Test
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
        o.add(factory.getOWLEquivalentClassesAxiom(A,
                factory.getOWLObjectSomeValuesFrom(p, B)));

        Abstracter abstracter = new AbstracterImpl(o);

        // add the axioms
        Set<OWLAxiom> axioms = new HashSet<>();

        OWLNamedIndividual b = factory.getOWLNamedIndividual(base + "b");
        OWLNamedIndividual event = factory.getOWLNamedIndividual(base + "a");
        axioms.add(factory.getOWLObjectPropertyAssertionAxiom(p, event, b));
        axioms.add(factory.getOWLClassAssertionAxiom(B, b));

        Set<OWLClass> types = abstracter.lift(o, event);

        Set<OWLClass> expected_types = new HashSet<>();
        expected_types.add(A);
        expected_types.add(OWL.Thing);

        assertEquals(types.size(), 2);
        assertTrue(types.containsAll(expected_types));

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
        o.add(factory.getOWLEquivalentClassesAxiom(A,
                factory.getOWLObjectSomeValuesFrom(p, B)));

        //Input Semantic Event

        // add the axioms
        Set<OWLAxiom> axioms = new HashSet<>();

        OWLNamedIndividual b = factory.getOWLNamedIndividual(base + "b");
        OWLNamedIndividual event = factory.getOWLNamedIndividual(base + "a");
        axioms.add(factory.getOWLObjectPropertyAssertionAxiom(p, event, b));

        axioms.add(factory.getOWLClassAssertionAxiom(B, b));

        RawEvent message = new RawEvent("http://example.org#a");
        message.setStream_uri("test1");
        message.setTimeStamp(System.currentTimeMillis());
        message.setContent(new ContentOntology(axioms));


        //Expected Output
        //TBox
        OWLOntology copy = OWLManager.createOWLOntologyManager().copyOntology(o, OntologyCopy.SHALLOW);
        //ABox
        copy.add(axioms);

        RawEvent thing = new RawEvent("http://example.org#a");
        thing.setContent(new ContentOntology(copy));
        thing.setStream_uri("test1");
        thing.setType(OWL.Thing);
        RawEvent eventA = new RawEvent("http://example.org#a");
        eventA.setContent(new ContentOntology(copy));
        eventA.setStream_uri("test1");
        eventA.setType(A);


        final Set<RawEvent> expected_events = new HashSet<>();
        expected_events.add(eventA);
        expected_events.add(thing);

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

        Abstracter abstracter = new AbstracterImpl(o);
        abstracter.pipe(tester);

        abstracter.send(message);

        assertEquals(expected_events.size(), actual_events.size());
        actual_events.forEach(semanticEvent -> {
            assertTrue(semanticEvent.containsKey("timestamp.abstracter"));
            assertTrue(semanticEvent.containsKey("event_type"));
            assertTrue(semanticEvent.get("event_type") != null);
        });

        assertEquals(OWL.Thing,
                actual_events.get(0).getType());
        assertEquals(A,
                actual_events.get(1).getType());

    }
}

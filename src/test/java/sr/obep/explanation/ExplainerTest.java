package sr.obep.explanation;

import junit.framework.TestCase;
import openllet.owlapi.OpenlletReasonerFactory;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import sr.obep.TestEventProcessor;
import sr.obep.data.events.ContentImpl;
import sr.obep.data.events.SemanticEvent;
import sr.obep.processors.EventProcessor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ExplainerTest extends TestCase {


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
        Set<OWLAxiom> expected_explanation = new HashSet<>();

        OWLNamedIndividual b = factory.getOWLNamedIndividual(base + "b");
        OWLNamedIndividual a = factory.getOWLNamedIndividual(base + "a");
        OWLObjectPropertyAssertionAxiom bpa = factory.getOWLObjectPropertyAssertionAxiom(p, a, b);
        OWLClassAssertionAxiom baB = factory.getOWLClassAssertionAxiom(B, b);

        o.add(baB, bpa);

        OWLReasoner reasoner = OpenlletReasonerFactory.getInstance().createReasoner(o);

        expected_explanation.add(baB);
        expected_explanation.add(bpa);
        expected_explanation.add(equivalentClassesAxiom);

        Explainer explainer = new ExplainerImpl();

        List<OWLClass> collect = reasoner.getTypes(a, true).entities().collect(Collectors.toList());
        assertEquals(1, collect.size());

        OWLClass explected_type = collect.get(0);

        Set<Set<OWLAxiom>> actual_explanations = explainer.explain(o, a, explected_type);

        assertEquals(1, actual_explanations.size());

        Set<OWLAxiom> actual_explanation = actual_explanations.iterator().next();
        actual_explanation.forEach(System.out::println);

        assertEquals(expected_explanation.size(), actual_explanation.size());
        assertTrue(actual_explanation.containsAll(expected_explanation));
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

        SemanticEvent message = new SemanticEvent("http://example.org#a");
        message.setType(A);
        message.setStream_uri("test1");
        message.setTimeStamp(System.currentTimeMillis());
        message.setContent(new ContentImpl(o));

        //Expected Output
        expected_explanation.add(baB);
        expected_explanation.add(bpa);
        expected_explanation.add(owlEquivalentClassesAxiom);

        SemanticEvent eventA = new SemanticEvent("http://example.org#a");
        eventA.setContent(new ContentImpl(expected_explanation));
        eventA.setStream_uri("test1");
        eventA.setType(A);


        final Set<SemanticEvent> expected_events = new HashSet<>();
        expected_events.add(eventA);

        final List<SemanticEvent> actual_events = new ArrayList<>();

        TestEventProcessor tester = new TestEventProcessor() {

            @Override
            public void send(SemanticEvent e) {
                actual_events.add(e);
            }

            @Override
            public EventProcessor pipe(EventProcessor p) {
                return this;
            }
        };

        Explainer explainer = new ExplainerImpl();
        explainer.pipe(tester);

        explainer.send(message);

        assertEquals(1, actual_events.size());

        SemanticEvent semanticEvent = actual_events.get(0);

        assertTrue(semanticEvent.containsKey("timestamp.explainer"));
        assertTrue(semanticEvent.containsKey("event_type"));
        assertTrue(semanticEvent.get("event_type") != null);
        assertTrue(expected_explanation.containsAll(semanticEvent.getContent().asOWLAxioms()));
        assertEquals(A, semanticEvent.getType());

    }


}

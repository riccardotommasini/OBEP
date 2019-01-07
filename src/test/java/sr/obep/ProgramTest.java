package sr.obep;

import com.espertech.esper.client.soda.*;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import sr.obep.data.content.ContentAxioms;
import sr.obep.data.events.CompositeEvent;
import sr.obep.data.events.LogicalEvent;
import sr.obep.data.events.RawEvent;
import sr.obep.data.streams.EventStream;
import sr.obep.data.streams.WritableEventStream;
import sr.obep.engine.OBEPEngineImpl;
import sr.obep.pipeline.normalization.NormalForm;
import sr.obep.pipeline.normalization.SPARQLNormalForm;
import sr.obep.programming.Program;
import sr.obep.programming.parser.delp.data.CompositeEventImpl;
import sr.obep.programming.parser.delp.data.LogicalEventImpl;
import sr.obep.programming.parser.sparql.Prefix;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ProgramTest {

    public static final IRI base = IRI.create("http://example.org#");
    public static final IRI engine = IRI.create("http://obep.org#");

    public static void main(String[] args) throws OWLOntologyCreationException {

        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLDataFactory df = manager.getOWLDataFactory();
        OWLOntology dbox = manager.createOntology(base);

        OWLClass B = df.getOWLClass(base + "B");
        OWLClass C = df.getOWLClass(base + "C");
        OWLClass E = df.getOWLClass(base + "E");
        OWLClass F = df.getOWLClass(base + "F");
        OWLObjectProperty p = df.getOWLObjectProperty(base + "p");
        OWLObjectProperty q = df.getOWLObjectProperty(base + "q");

        dbox.add(df.getOWLDeclarationAxiom(C));
        dbox.add(df.getOWLDeclarationAxiom(B));
        dbox.add(df.getOWLDeclarationAxiom(E));
        dbox.add(df.getOWLDeclarationAxiom(F));
        dbox.add(df.getOWLDeclarationAxiom(q));
        dbox.add(df.getOWLDeclarationAxiom(p));

        // ABOX axioms

        //Event1 A

        OWLNamedIndividual b = df.getOWLNamedIndividual(base + "1");
        OWLClassAssertionAxiom baB = df.getOWLClassAssertionAxiom(B, b);
        OWLNamedIndividual c = df.getOWLNamedIndividual(base + "c");
        OWLClassAssertionAxiom caB = df.getOWLClassAssertionAxiom(C, c);
        OWLObjectPropertyAssertionAxiom bpa = df.getOWLObjectPropertyAssertionAxiom(p, b, c);

        Set<OWLAxiom> axioms = new HashSet<>();
        axioms.add(baB);
        axioms.add(bpa);
        axioms.add(caB);

        //Event1 D

        OWLNamedIndividual e = df.getOWLNamedIndividual(base + "2");
        OWLClassAssertionAxiom eaE = df.getOWLClassAssertionAxiom(E, e);
        OWLNamedIndividual f = df.getOWLNamedIndividual(base + "f");
        OWLClassAssertionAxiom faF = df.getOWLClassAssertionAxiom(F, f);

        OWLObjectPropertyAssertionAxiom eqf = df.getOWLObjectPropertyAssertionAxiom(q, e, f);

        Set<OWLAxiom> axioms2 = new HashSet<>();
        axioms2.add(eaE);
        axioms2.add(eqf);
        axioms2.add(faF);

        dbox.axioms().forEach(System.out::println);

        Set<Prefix> prefixes = new HashSet<>();
        prefixes.add(new Prefix("", base.getIRIString()));

        Set<EventStream> inputs = new HashSet<>();
        WritableEventStream sin = new WritableEventStream("test1");
        inputs.add(sin);

        //NAMED
        Set<String> outputs = new HashSet<>();
        outputs.add("H");
        // outputs.add("A");

        //RETURN FORMAT RDF | EVENT define which listener to use

        Set<LogicalEvent> les = new HashSet<>();
        les.add(new LogicalEventImpl("A", ":B and :p some :C"));
        les.add(new LogicalEventImpl("D", ":E and :q some :F"));

        // TODO les.add(new LogicalEventImpl("B := :q some :B"));
        // EVENT H MATCH every (A -> C)
        // IF {
        //     EVENT A {?s <http://example.org#p> ?b }
        //     EVENT C {?s <http://example.org#p> ?c }
        // }

        Set<CompositeEvent> ces = new HashSet<>();

        //todo add to composite event the a map <epl_var_name,event_name>
        String head = "H";

        PatternFollowedByExpr followedby = new PatternFollowedByExpr(new PatternFilterExpr(Filter.create("A"), "a"), new PatternFilterExpr(Filter.create("D"), "b"));
        PatternStream body = PatternStream.create(new PatternEveryExpr(followedby));

        NormalForm nfa = new SPARQLNormalForm(head, "SELECT * WHERE {?s <http://example.org#p> ?c }", df.getOWLClass(engine + "A"));
        NormalForm nfc = new SPARQLNormalForm(head, "SELECT * WHERE {?s <http://example.org#q> ?f }", df.getOWLClass(engine + "D"));

        Map<String, NormalForm> normalFormMap = new HashMap<>();
        normalFormMap.put("A", nfa);
        normalFormMap.put("D", nfc);

        Map<String, String> alias = new HashMap<>();
        alias.put("A", "a");
        alias.put("D", "b");

        ces.add(new CompositeEventImpl(head, body, normalFormMap, alias));

        Program program = new Program() {

            @Override
            public OWLOntology getOntology() {
                return dbox;
            }

            @Override
            public Set<Prefix> getPrefixes() {
                return prefixes;
            }

            @Override
            public Set<EventStream> getInputStreams() {
                return inputs;
            }

            @Override
            public Set<LogicalEvent> getLogicalEvents() {
                return les;
            }

            @Override
            public Set<CompositeEvent> getCompositeEvents() {
                return ces;
            }

            @Override
            public Set<String> getOutputStreams() {
                return outputs;
            }
        };

        OBEPEngineImpl obepEngine = new OBEPEngineImpl(engine);

        //TODO modificare il parser to extract the match rule decomposed:
        // pattern, normal forms, filters
        obepEngine.register(program);

        RawEvent event = new RawEvent(base + "1");
        event.setContent(new ContentAxioms(axioms));
        event.setStream_uri("test1");
        event.setTimeStamp(System.currentTimeMillis());

        sin.put(event);

        RawEvent event1 = new RawEvent(base + "2");
        event1.setContent(new ContentAxioms(axioms2));
        event1.setStream_uri("test1");
        event1.setTimeStamp(System.currentTimeMillis());

        sin.put(event1);
        System.out.println("<---------END--------->");

    }
}

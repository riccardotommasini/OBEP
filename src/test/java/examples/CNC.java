package examples;

import it.polimi.deib.sr.obep.core.OBEPEngine;
import it.polimi.deib.sr.obep.core.data.streams.EventStream;
import it.polimi.deib.sr.obep.core.data.streams.EventStreamImpl;
import it.polimi.deib.sr.obep.core.programming.Program;
import it.polimi.deib.sr.obep.impl.OBEPEngineImpl;
import it.polimi.deib.sr.obep.impl.RawEvent;
import it.polimi.deib.sr.obep.impl.content.ContentAxioms;
import it.polimi.deib.sr.obep.impl.parser.delp.parser.OBEPParser;
import it.polimi.deib.sr.obep.impl.parser.delp.parser.OBEPParserOutput;
import org.apache.commons.io.IOUtils;
import org.apache.jena.riot.system.IRIResolver;
import org.parboiled.Parboiled;
import org.parboiled.errors.ParseError;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.StringDocumentSource;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.vocab.OWL2Datatype;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class CNC {

    private static final OWLOntologyManager m = OWLManager.createOWLOntologyManager();
    private static final OWLDataFactory df = m.getOWLDataFactory();
    private static final IRI base = IRI.create("https://www.datasemantic.in/CncOntology.owl#");
    private static final IRI cnc = IRI.create("https://www.datasemantic.in/CncOntology.owl#");

    static OWLClass Device = df.getOWLClass(cnc + "Device");
    static OWLDataProperty receivedTime = df.getOWLDataProperty(cnc + "receivedTime");
    static OWLObjectProperty observerdProperty = df.getOWLObjectProperty(cnc + "ObservedProperty");
    static OWLObjectProperty observes = df.getOWLObjectProperty(cnc + "observes");
    static OWLObjectProperty hasContext = df.getOWLObjectProperty(cnc + "hasContext");
    static OWLDataProperty value = df.getOWLDataProperty(cnc + "hasValue");

    public static void main(String[] args) {

        URL query = CNC.class.getResource("/cnc.query");

        System.out.println(query.getPath());
        OBEPParser parser = Parboiled.createParser(OBEPParser.class);

        parser.setResolver(IRIResolver.create(cnc.getIRIString()));
        Program program = getProgram(parser, getFile(query.getPath()));

        OBEPEngine obepEngine = new OBEPEngineImpl(cnc);

        obepEngine.register(program);

        //runtime parser

        EventStream sin = new EventStreamImpl(program.getInputStreams().iterator().next());

        OWLNamedIndividual device1 = df.getOWLNamedIndividual(base + "cnc-26142");
        OWLNamedIndividual device2 = df.getOWLNamedIndividual(base + "cnc-26143");


        send_event(sin, "e1", getAxioms2("e1", "105", OWL2Datatype.XSD_DOUBLE, "Temperature", device1), 0);
        send_event(sin, "e2", getAxioms2("e2", "2020", OWL2Datatype.XSD_DOUBLE, "SpindleSpeed", device2), 10);
        send_event(sin, "e3", getAxioms2("e3", "4242", OWL2Datatype.XSD_DOUBLE, "SpindleSpeed", device1), 10);
//        send_event(sin, "cnc-26142", getAxioms2(201, "TactTime"), 0);
//        send_event(sin, "cnc-26142", getAxioms2(3000, "EnergyConsumption"), 0);

        System.out.println("<---------END--------->");

    }

    private static Program getProgram(OBEPParser parser, String query) {
        ParsingResult<OBEPParserOutput> result = new ReportingParseRunner(parser.Query()).run(query);

        if (result.hasErrors()) {
            for (ParseError arg : result.parseErrors) {
                System.err.println(query.substring(0, arg.getStartIndex()) + "|->" + query.substring(arg.getStartIndex(), arg.getEndIndex()) + "<-|" + query.substring(arg.getEndIndex() + 1, query.length() - 1));
            }
        }

        OBEPParserOutput q = result.resultValue;

        System.out.println(q);
        //runtime engine and program

        return q.asProgram();
    }

    private static void send_event(EventStream sin, String e, Set<OWLAxiom> axioms1, long delay) {
        RawEvent event = new RawEvent(base + e);
        event.setContent(new ContentAxioms(axioms1));
        event.setStream_uri("http://localhost:8112/stream1");
        event.setTimeStamp(System.currentTimeMillis() + delay);
        sin.put(event);
    }

    private static OWLOntology loadOntologyFromString(String onto) throws OWLOntologyCreationException {
        return m.loadOntologyFromOntologyDocument(new StringDocumentSource(onto));
    }


    private static String getFile(String fileName) {

        String result = "";


        try {
            result = IOUtils.toString(new File(fileName).toURI());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;

    }


    //


    public static Set<OWLAxiom> getAxioms2(String id, String v, OWL2Datatype xsdDouble, String prop, OWLNamedIndividual device) {
        //Event2: u1 observerdProperty square1 D


//        OWLClassAssertionAxiom baB = df.getOWLClassAssertionAxiom(Device, device);
//        OWLNamedIndividual p = df.getOWLNamedIndividual(cnc + prop);
//        OWLObjectPropertyAssertionAxiom eqf = df.getOWLObjectPropertyAssertionAxiom(observerdProperty, device, p);

        OWLNamedIndividual e = df.getOWLNamedIndividual(cnc + id);
        OWLClass propertyClass = df.getOWLClass(cnc + prop);
        OWLClassAssertionAxiom toe = df.getOWLClassAssertionAxiom(propertyClass, e);
        OWLObjectPropertyAssertionAxiom obs = df.getOWLObjectPropertyAssertionAxiom(observes, device, e);
        OWLDataPropertyAssertionAxiom def2 = df.getOWLDataPropertyAssertionAxiom(value, e, df.getOWLLiteral(v, xsdDouble));

        Set<OWLAxiom> axioms = new HashSet<>();
//        axioms.add(eqf);
//        axioms.add(baB);
        axioms.add(toe);
        axioms.add(def2);
        axioms.add(obs);

        return axioms;

    }
}

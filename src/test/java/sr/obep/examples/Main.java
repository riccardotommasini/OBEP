package sr.obep.examples;

import org.apache.commons.io.IOUtils;
import org.parboiled.Parboiled;
import org.parboiled.errors.ParseError;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.StringDocumentSource;
import org.semanticweb.owlapi.model.*;
import sr.obep.data.content.ContentAxioms;
import sr.obep.data.events.RawEvent;
import sr.obep.data.streams.WritableEventStream;
import sr.obep.engine.OBEPEngineImpl;
import sr.obep.programming.Program;
import sr.obep.programming.parser.delp.DELPParser;
import sr.obep.programming.parser.delp.OBEPParserOutput;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class Main {

    static OWLOntologyManager m = OWLManager.createOWLOntologyManager();
    static OWLDataFactory df = m.getOWLDataFactory();
    public static final IRI base = IRI.create("http://www.example.org/geometry#");

    //Classes
    static OWLClass Plane = df.getOWLClass(base + "Plane");
    static OWLClass Square = df.getOWLClass(base + "Square");
    static OWLClass Solid = df.getOWLClass(base + "Solid");
    static OWLObjectProperty transforms = df.getOWLObjectProperty(base + "transforms");
    static OWLObjectProperty rotates = df.getOWLObjectProperty(base + "rotates");

    //Events
    static OWLClass ObsFig2D = df.getOWLClass(base + "ObsFig2D");
    static OWLClass ObsFig3D = df.getOWLClass(base + "ObsFig3D");
    static OWLClass ObsFigTransform = df.getOWLClass(base + "ObsFigTransform");

    public static void main(String[] args) throws OWLOntologyCreationException {

        WritableEventStream sin = new WritableEventStream("http://www.stream.org/stream1");

        URL resource = ParserTest.class.getResource("/shapes.query");

        DELPParser parser = Parboiled.createParser(DELPParser.class);

        String query = getFile(resource.getPath());

        ParsingResult<OBEPParserOutput> result = new ReportingParseRunner(parser.Query()).run(query);

        if (result.hasErrors()) {
            for (ParseError arg : result.parseErrors) {
                System.err.println(query.substring(0, arg.getStartIndex()) + "|->" + query.substring(arg.getStartIndex(), arg.getEndIndex()) + "<-|" + query.substring(arg.getEndIndex() + 1, query.length() - 1));
            }
        }

        OBEPParserOutput q = result.resultValue;

        //runtime engine and program

        Program program = q.asProgram();

        program.getInputStreams().add(sin);

        OBEPEngineImpl obepEngine = new OBEPEngineImpl(base);
        obepEngine.register(program);


        //runtime data
        RawEvent event = new RawEvent(base + "square1");
        event.setContent(new ContentAxioms(getAxioms1()));
        event.setStream_uri("http://www.stream.org/stream1");
        event.setTimeStamp(System.currentTimeMillis());

        sin.put(event);

        RawEvent event1 = new RawEvent(base + "u1");
        event1.setContent(new ContentAxioms(getAxioms2()));
        event1.setStream_uri("http://www.stream.org/stream1");
        event1.setTimeStamp(System.currentTimeMillis());

        sin.put(event1);
        System.out.println("<---------END--------->");


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

    public static Set<OWLAxiom> getAxioms1() {

        //Event1: square1 a Square

        OWLNamedIndividual b = df.getOWLNamedIndividual(base + "square1");
        OWLClassAssertionAxiom baB = df.getOWLClassAssertionAxiom(Square, b);

        Set<OWLAxiom> axioms = new HashSet<>();
        axioms.add(baB);

        return axioms;
    }

    public static Set<OWLAxiom> getAxioms2() {
        //Event2: u1 rotates square1 D

        OWLNamedIndividual e = df.getOWLNamedIndividual(base + "u1");
        OWLNamedIndividual f = df.getOWLNamedIndividual(base + "square1");

        OWLObjectPropertyAssertionAxiom eqf = df.getOWLObjectPropertyAssertionAxiom(rotates, e, f);

        Set<OWLAxiom> axioms = new HashSet<>();
        axioms.add(eqf);
        return axioms;

    }
}

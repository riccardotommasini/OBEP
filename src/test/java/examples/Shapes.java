package examples;

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
import sr.obep.data.streams.EventStream;
import sr.obep.engine.OBEPEngine;
import sr.obep.engine.OBEPEngineImpl;
import sr.obep.parser.ParserTest;
import sr.obep.programming.Program;
import sr.obep.programming.parser.delp.OBEPParser;
import sr.obep.programming.parser.delp.OBEPParserOutput;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class Shapes {

    private static final OWLOntologyManager m = OWLManager.createOWLOntologyManager();
    private static final OWLDataFactory df = m.getOWLDataFactory();
    private static final IRI base = IRI.create("http://www.example.org/geometry#");

    public static void main(String[] args) {

        URL query = ParserTest.class.getResource("/shapes.query");

        OBEPParser parser = Parboiled.createParser(OBEPParser.class);

        Program program = getProgram(parser, getFile(query.getPath()));

        OBEPEngine obepEngine = new OBEPEngineImpl(base);

        obepEngine.register(program);

        //runtime data

        EventStream sin = program.getInputStreams().iterator().next();

        send_event(sin, "square1", getAxioms1());
        send_event(sin, "u1", getAxioms2());

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

        //runtime engine and program

        return q.asProgram();
    }

    private static void send_event(EventStream sin, String square1, Set<OWLAxiom> axioms1) {
        RawEvent event = new RawEvent(base + square1);
        event.setContent(new ContentAxioms(axioms1));
        event.setStream_uri("http://www.stream.org/stream1");
        event.setTimeStamp(System.currentTimeMillis());
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

    public static Set<OWLAxiom> getAxioms1() {

        OWLClass Square = df.getOWLClass(base + "Square");
        OWLNamedIndividual b = df.getOWLNamedIndividual(base + "square1");
        OWLClassAssertionAxiom baB = df.getOWLClassAssertionAxiom(Square, b);

        Set<OWLAxiom> axioms = new HashSet<>();
        axioms.add(baB);

        return axioms;
    }

    public static Set<OWLAxiom> getAxioms2() {
        //Event2: u1 rotates square1 D
        OWLObjectProperty rotates = df.getOWLObjectProperty(base + "rotates");

        OWLNamedIndividual e = df.getOWLNamedIndividual(base + "u1");
        OWLNamedIndividual f = df.getOWLNamedIndividual(base + "square1");

        OWLObjectPropertyAssertionAxiom eqf = df.getOWLObjectPropertyAssertionAxiom(rotates, e, f);

        Set<OWLAxiom> axioms = new HashSet<>();
        axioms.add(eqf);
        return axioms;

    }
}

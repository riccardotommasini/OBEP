package sr.obep.examples;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.parboiled.Parboiled;
import org.parboiled.errors.ParseError;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.expression.OWLEntityChecker;
import org.semanticweb.owlapi.expression.ShortFormEntityChecker;
import org.semanticweb.owlapi.io.StringDocumentSource;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.BidirectionalShortFormProviderAdapter;
import org.semanticweb.owlapi.util.OntologyAxiomPair;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;
import org.semanticweb.owlapi.util.mansyntax.ManchesterOWLSyntaxParser;
import sr.obep.programming.parser.delp.DELPParser;
import sr.obep.programming.parser.delp.OBEPParserOutput;
import sr.obep.programming.parser.delp.data.ComplexEventDeclaration;
import sr.obep.programming.parser.delp.data.CompositeEventDeclaration;
import sr.obep.programming.parser.delp.data.LogicalEventDeclaration;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import static org.semanticweb.owlapi.util.OWLAPIStreamUtils.asList;

public class ParserTest {


    @Test
    public void test0() throws OWLOntologyCreationException {

        URL resource = ParserTest.class.getResource("/shapes.query");

        DELPParser parser = Parboiled.createParser(DELPParser.class);

        String program = getFile(resource.getPath());

        ParsingResult<OBEPParserOutput> result = new ReportingParseRunner(parser.Query()).run(program);

        if (result.hasErrors()) {
            for (ParseError arg : result.parseErrors) {
                System.out.println(program.substring(0, arg.getStartIndex()) + "|->" + program.substring(arg.getStartIndex(), arg.getEndIndex()) + "<-|" + program.substring(arg.getEndIndex() + 1, program.length() - 1));
            }
        }

        OBEPParserOutput q = result.resultValue;

        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLDataFactory df = manager.getOWLDataFactory();

        new HashSet<>(q.getPrefixes()).forEach(System.out::println);

        System.out.println("VOCAB");

        System.out.println(q.getVocab());

        OWLOntology o = manager.loadOntologyFromOntologyDocument(IRI.create(q.getVocab()));

        System.out.println("STATIC");

        q.getGraphURIs().forEach(System.out::println);

        System.out.println("STREAMS");

        q.getStreamUris().forEach(System.out::println);

        q.getLogicalEvents().forEach(System.out::println);

        q.getLogicalEvents().stream().map(ComplexEventDeclaration::getHead)
                .map(df::getOWLClass).map(df::getOWLDeclarationAxiom).forEach(o::addAxiom);

        q.getCompositeEvents().stream().map(ComplexEventDeclaration::getHead)
                .map(df::getOWLClass).map(df::getOWLDeclarationAxiom).forEach(o::addAxiom);

        ManchesterOWLSyntaxParser mparser = OWLManager.createManchesterParser();

        BidirectionalShortFormProviderAdapter adapter = new BidirectionalShortFormProviderAdapter(
                asList(manager.ontologies()), new SimpleShortFormProvider());
        OWLEntityChecker entityChecker = new ShortFormEntityChecker(adapter);
        mparser.setOWLEntityChecker(entityChecker);

        mparser.setDefaultOntology(o);

        q.getLogicalEvents().stream().map(LogicalEventDeclaration::toString)
                .flatMap(s -> {
                    mparser.setStringToParse(s);
                    return mparser.parseClassFrame().stream();
                }).map(OntologyAxiomPair::getAxiom).forEach(o::add);

        o.axioms().forEach(System.out::println);

        q.getCompositeEvents().stream().map(ced -> ced.getExpr().toString()).forEach(System.err::println);

        Set<CompositeEventDeclaration> compositeEvents = q.getCompositeEvents();


    }


    private String getFile(String fileName) {

        String result = "";


        try {
            result = IOUtils.toString(new File(fileName).toURI());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;

    }


    private OWLOntology loadOntologyFromString(OWLOntologyManager m, String onto) throws OWLOntologyCreationException {
        OWLOntologyLoaderConfiguration config = new OWLOntologyLoaderConfiguration();
        return m.loadOntologyFromOntologyDocument(new StringDocumentSource(onto), config);
    }
}

package sr.obep.engine;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.manchestersyntax.parser.ManchesterOWLSyntaxParserImpl;
import org.semanticweb.owlapi.model.*;
import sr.obep.abstration.Abstracter;
import sr.obep.abstration.AbstracterImpl;
import sr.obep.explanation.Explainer;
import sr.obep.explanation.ExplainerImpl;
import sr.obep.extraction.Extractor;
import sr.obep.extraction.ExtractorImpl;
import sr.obep.processors.CEP;
import sr.obep.programming.Program;
import sr.obep.programming.ProgramManager;
import sr.obep.programming.ProgramManagerImpl;

import java.util.StringJoiner;

public class OBEPEngineImpl implements OBEPEngine {

    public static final IRI base = IRI.create("http://example.org#");
    public static final ProgramManager manager = new ProgramManagerImpl();

    @Override
    public void register(Program q) {
        CEP cep = new CEP();
        try {

            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            OWLOntology tbox = manager.createOntology(base);

            ManchesterOWLSyntaxParserImpl parser = new ManchesterOWLSyntaxParserImpl(new OntologyConfigurator(), manager.getOWLDataFactory());

            //Prefixes
            PrefixManager prefixManager = parser.getPrefixManager();
            prefixManager.setDefaultPrefix(base.getIRIString());
            q.getPrefixes().forEach(prefix -> prefixManager.setPrefix(prefix.getPrefix(), prefix.getUri()));

            //Logical Events
            StringJoiner ontology_builder = new StringJoiner(System.getProperty("line.separator"));
            StringJoiner epl_program_builder = new StringJoiner(System.getProperty("line.separator"));

            q.getLogicalEvents().forEach(logicalEvent -> {

                        String head = logicalEvent.getHead();
                        String body = logicalEvent.getBody();
                        String axiom = "Class: <" + head + ">\n    " + "EquivalentTo:\n        " + body;
                        ontology_builder.add(axiom);

                        //for logging purposes
                        String new_event = "create schema " + head + " as ()";
                        epl_program_builder.add(new_event);
                        cep.register_event_schema(new_event);

                    }
            );

            //Composite Events
            q.getCompositeEvents().forEach(ce -> {

                String head = ce.getHead();
                String body = ce.getBody();
                String axiom = "Class: <" + head + ">";

                ontology_builder.add(axiom);

                //for logging purposes
                String new_event = "create schema " + head + " as ()";
                epl_program_builder.add(new_event);
                cep.register_event_schema(new_event);

                String new_event_stream = "insert into" + head + "\n" + body;
                epl_program_builder.add(new_event_stream);
                cep.register_event_stream(new_event_stream);

            });

            parser.setDefaultOntology(tbox);
            parser.setStringToParse(ontology_builder.toString());
            parser.parseOntology(tbox);


            Abstracter abstracter = new AbstracterImpl(tbox);

            q.getInputStreams().forEach(s -> {
                s.connectTo(abstracter);
            });


            Explainer explainer = new ExplainerImpl();
            Extractor extractor = new ExtractorImpl();

            abstracter.pipe(explainer).pipe(extractor).pipe(cep);

            //Physical Events
            //TODO register event stream schema?

            //TODO output streams?

        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void register(String q) {
        register(manager.parse(q));
    }

//    public void sendEvent(SemanticEvent se) {
//        //TODO add check controls
//        if (se.getTriggeredFilterIRIs() == null) {
//            abstracter.lift(se);
//        }
//        if (se.getTriggeredFilterIRIs() != null && !se.getTriggeredFilterIRIs().isEmpty() && se.getProperties() == null) {
//            extractor.extract(se);
//        }
//        if (se.getProperties() != null) {
//            cep.sendEvent(se);
//        }
//    }
}

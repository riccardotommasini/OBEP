package sr.obep.engine;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.soda.*;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.manchestersyntax.parser.ManchesterOWLSyntaxParserImpl;
import org.semanticweb.owlapi.model.*;
import sr.obep.pipeline.abstration.Abstracter;
import sr.obep.pipeline.abstration.AbstracterImpl;
import sr.obep.pipeline.explanation.Explainer;
import sr.obep.pipeline.explanation.ExplainerImpl;
import sr.obep.pipeline.normalization.NormalForm;
import sr.obep.pipeline.normalization.Normalizer;
import sr.obep.pipeline.normalization.SPARQLNormalizer;
import sr.obep.pipeline.processors.CEP;
import sr.obep.programming.Program;
import sr.obep.programming.ProgramManager;
import sr.obep.programming.ProgramManagerImpl;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

public class OBEPEngineImpl implements OBEPEngine {

    public final IRI base;
    public static final ProgramManager manager = new ProgramManagerImpl();
    private final String outStream = "OutStream";

    public final OWLOntology tbox;

    public OBEPEngineImpl(IRI base, OWLOntology tbox) {
        this.base = base;
        this.tbox = tbox;
    }

    @Override
    public void register(Program q) {
        CEP cep = new CEP();
        try {

            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            OWLOntology ebox = manager.createOntology(tbox.axioms(), base);

            OWLDataFactory owlDataFactory = manager.getOWLDataFactory();
            ManchesterOWLSyntaxParserImpl parser = new ManchesterOWLSyntaxParserImpl(new OntologyConfigurator(), owlDataFactory);

            //Prefixes
            PrefixManager prefixManager = parser.getPrefixManager();
            prefixManager.setDefaultPrefix(base.getIRIString());
            prefixManager.setPrefix("evt", base.getIRIString());
            q.getPrefixes().forEach(prefix -> prefixManager.setPrefix(prefix.getPrefix(), prefix.getUri()));

            //Logical Events
            StringJoiner ontology_builder = new StringJoiner(System.getProperty("line.separator"));
            StringJoiner epl_program_builder = new StringJoiner(System.getProperty("line.separator"));
            StringJoiner epl_out_program_builder = new StringJoiner(System.getProperty("line.separator"));

//            String out_schema = "create schema " + outStream + " as (content java.lang.Object)";
//            epl_program_builder.add(out_schema);
//            cep.register_event_schema(out_schema);

            Set<String> outputStreams = q.getOutputStreams();
            q.getLogicalEvents().forEach(logicalEvent -> {

                        String head = logicalEvent.getHead().trim();
                        String body = logicalEvent.getBody().trim();
                        String axiom = "Class: evt:" + head + "\n    " + "EquivalentTo:\n        " + body;
                        ontology_builder.add(axiom);

                        //I still create schemaless logical streams because i want the schemafull ones to inherit from these.
                        String new_event = "create schema _" + head + " as (event_content java.lang.Object)";
                        epl_program_builder.add(new_event);
                        cep.register_event_schema(new_event);
                        if (outputStreams.stream().anyMatch(eventStream -> eventStream.equals("_" + head))) {
                            String s = connectToOut(epl_out_program_builder, "_" + head, new String[]{"event_content"});
                            cep.register_event_pattern_stream(s);
                        }

                    }
            );

            Normalizer normalizer = new SPARQLNormalizer();

            //Composite Events
            q.getCompositeEvents().forEach(ce -> {

                String head = ce.getHead();
                String body = ce.getBody();
                String axiom = "Class: evt:" + head + "";

                ontology_builder.add(axiom);

                // for logging purposes
                // String new_event = "create schemas " + head_node + " as ()";
                // epl_program_builder.add(new_event);
                // cep.register_event_schema(new_event);

                Arrays.stream(ce.named()).forEach(s -> {

                    NormalForm q1 = ce.normal_forms().get(s);
                    normalizer.addNormalForm(q1);

                    String new_event = q1.toString();

                    epl_program_builder.add(new_event);
                    cep.register_event_schema(new_event);

                    if (outputStreams.stream().anyMatch(eventStream -> eventStream.equals(s))) {
                        String out = connectToOut(epl_out_program_builder, s, new String[]{"event_content"});
                        cep.register_event_pattern_stream(out);
                    }

                });

                //String new_event_stream = "insert into " + head + "\n select * from pattern [" + body + "]";

                String[] named = ce.named();
                String[] projections = new String[named.length];
                for (int i = 0; i < named.length; i++) {
                    projections[i] = ce.alias(named[i]) + ".event_content";
                }

                String new_event_stream = connectTo(epl_program_builder, head, "pattern [" + body + "]", projections);

                cep.register_event_pattern_stream(new_event_stream);

                if (outputStreams.stream().anyMatch(eventStream -> eventStream.equals(head))) {
                    String pattern = connectToOut(epl_out_program_builder, head, null);
                    cep.register_event_pattern_stream(pattern);
                }

            });

            parser.setDefaultOntology(ebox);
            String s1 = ontology_builder.toString();
            System.out.println(s1);
            parser.setStringToParse(s1);
            parser.parseOntology(ebox);

            Abstracter abstracter = new AbstracterImpl(ebox);

            q.getInputStreams().forEach(s -> s.connectTo(abstracter));

            Explainer explainer = new ExplainerImpl();

            abstracter.pipe(explainer).pipe(normalizer).pipe(cep);

            //If NAMED, all event definitions with "NAMED" will be added to this.
            //If ALL -> every stream is connected to out (also input stream)
            //IF LOGIC -> only the logic ones
            //IF COMPOSITE -> only the composite ones
            //No logic has to be performred here, assume that getOutputStream contains already all the stream to output
            //Partitions in output consist of multiple listeners

            //I should introduce input stream format, which can be RDF|ONTOLOGY|EVENT, that requires a special kind of mapping

            //Output stream has also a format, which can be RDF|ONTOLOGY|EVENT


            //Physical Events
            //TODO register event stream schemas?

            //TODO output streams?

            String pattern = "select * from " + outStream;

            epl_out_program_builder.add(pattern);

            String output = epl_out_program_builder.toString();

            epl_program_builder.add(output);

            System.out.println(epl_program_builder.toString());

            cep.register_event_pattern_stream(pattern).addListener((newEvents, oldEvents, statement, epServiceProvider) -> {
                if (newEvents != null) {
                    Arrays.stream(newEvents)
                            .map(EventBean::getUnderlying)
                            .map(o -> (Map) o)
                            .flatMap(map -> map.values().stream())
                            .forEach(System.out::println);
                }
            });


        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }
    }

    private String connectTo(StringJoiner output_stream_builder, String head, String stream, String[] projections) {
        EPStatementObjectModel model = new EPStatementObjectModel();
        model.insertInto(InsertIntoClause.create(head));
        model.setFromClause(FromClause.create(FilterStream.create(stream)));
        SelectClause selectClause;
        if (projections == null) {
            selectClause = SelectClause.createWildcard();
        } else {
            selectClause = SelectClause.create(projections);
        }
        model.setSelectClause(selectClause);
        String newElement = model.toEPL();
        output_stream_builder.add(newElement);
        return newElement;
    }

    private String connectToOut(StringJoiner output_stream_builder, String head, String[] projections) {
        return connectTo(output_stream_builder, outStream, head, projections);
    }

    @Override
    public void register(String q) {
        register(manager.parse(q));
    }

//    public void sendEvent(RawEvent se) {
//        //TODO add check controls
//        if (se.getTriggeredFilterIRIs() == null) {
//            abstracter.lift(se);
//        }
//        if (se.getTriggeredFilterIRIs() != null && !se.getTriggeredFilterIRIs().isEmpty() && se.getProperties() == null) {
//            extractor.normalize(se);
//        }
//        if (se.getProperties() != null) {
//            cep.sendEvent(se);
//        }
//    }
}

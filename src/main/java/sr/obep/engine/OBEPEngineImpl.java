package sr.obep.engine;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.soda.*;
import lombok.extern.log4j.Log4j;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.manchestersyntax.parser.ManchesterOWLSyntaxParserImpl;
import org.semanticweb.owlapi.model.*;
import sr.obep.data.content.MergeContentExpression;
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

import java.util.*;

@Log4j
public class OBEPEngineImpl implements OBEPEngine {

    public final IRI base;
    public static final ProgramManager manager = new ProgramManagerImpl();
    private final String outStream = "OutStream";
    private final String out_pattern = "select * from " + outStream;
    private CEP cep;

    public OBEPEngineImpl(IRI base) {
        this.base = base;
        this.cep = new CEP();
    }

    @Override
    public void register(Program q) {
        try {

            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            OWLOntology ebox = manager.createOntology(q.getOntology().axioms(), base);

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


            Set<String> connected_to_out = new HashSet<>();

            Set<String> outputStreams = q.getOutputStreams();
            q.getLogicalEvents().forEach(logicalEvent -> {

                        String head = logicalEvent.getHead().trim();
                        String body = logicalEvent.getBody().trim();
                        String axiom = "Class: evt:" + head + "\n    " + "EquivalentTo:\n        " + body;
                        ontology_builder.add(axiom);

                        //I still create schemaless logical streams because i want the schemafull ones to inherit from these.
                        //TODO THE SCHEMA MUST DEPEND ON THE NORMAL FORM
                        String new_event = "create schema " + head + " as (event_content sr.obep.data.events.Content)";

                        epl_program_builder.add(new_event);
                        cep.register_event_schema(new_event);

                    }
            );

            Map<OWLClass, NormalForm> active_normal_forms = new HashMap<>();

            //Composite Events

            List<Normalizer> normalizers = new ArrayList<>();

            q.getCompositeEvents().forEach(ce -> {

                String head = ce.getHead();
                String body = ce.getBody();
                String axiom = "Class: evt:" + head + "";

                ontology_builder.add(axiom);

                Arrays.stream(ce.named()).forEach(name -> {

                    NormalForm nf = ce.normal_forms().get(name);

                    String new_event = nf.toString();

                    epl_program_builder.add(new_event);
                    cep.register_event_schema(new_event);

                    active_normal_forms.put(nf.event(), nf);

                });


                normalizers.add(new SPARQLNormalizer(ebox, active_normal_forms, head));

                String[] named = ce.named();
                String[] projections = new String[named.length];
                for (int i = 0; i < named.length; i++) {
                    projections[i] = ce.alias(named[i]) + ".event_content";
                }

                String new_event_stream = connectTo(epl_program_builder, "merge2", head, body, projections);

                log.debug(new_event_stream);
                cep.register_event_pattern_stream(new_event_stream);

            });

            parser.setDefaultOntology(ebox);
            String s1 = ontology_builder.toString();
            System.out.println(s1);
            parser.setStringToParse(s1);
            parser.parseOntology(ebox);

            //Setting Up the Engine


            Abstracter abstracter = new AbstracterImpl(ebox);

            Explainer explainer = new ExplainerImpl(ebox);

            normalizers.forEach(normalizer -> abstracter.pipe(explainer).pipe(normalizer).pipe(cep));

            //CONNECT INPUT AND OUTPUTS STREAMS
            q.getInputStreams().forEach(s -> s.connectTo(abstracter));

            q.getOutputStreams().forEach(s -> cep.register_event_pattern_stream(connectToOut(epl_out_program_builder, s, new String[]{})));

            cep.register_event_pattern_stream(out_pattern).addListener((newEvents, oldEvents, statement, epServiceProvider) -> {
                if (newEvents != null) {
                    Arrays.stream(newEvents)
                            .map(EventBean::getUnderlying)
                            .map(o -> (Map) o)
                            .flatMap(map -> map.values().stream())
                            .forEach(System.out::println);
                }
            });

            System.out.println(epl_program_builder.toString());
            StringJoiner add = epl_out_program_builder.add(out_pattern);
            System.out.println(add.toString());


        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }

    }

    private String connectTo(StringJoiner output_stream_builder, String udf, String head, String stream, String[] projections) {
        EPStatementObjectModel model = new EPStatementObjectModel();
        model.insertInto(InsertIntoClause.create(head));
        model.setFromClause(FromClause.create(FilterStream.create(stream)));
        SelectClause selectClause = null;
        if (udf == null || udf.isEmpty()) {
            if (projections == null) {
            } else {
                selectClause = SelectClause.create(projections);
            }
        } else {
            selectClause = SelectClause.create();
            selectClause.add(merge(projections), "event_content");
        }
        model.setSelectClause(selectClause);

        String newElement = model.toEPL();
        output_stream_builder.add(newElement);
        return newElement;
    }

    private String connectToOut(StringJoiner output_stream_builder, String head, String[] projections) {
        return connectTo(output_stream_builder, "", outStream, head, projections);
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

    public MergeContentExpression merge(String... moreProperties) {
        return new MergeContentExpression(moreProperties);
    }

}

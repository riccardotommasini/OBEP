package it.polimi.deib.sr.obep.impl;

import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.soda.*;
import it.polimi.deib.sr.obep.core.OBEPEngine;
import it.polimi.deib.sr.obep.core.data.streams.EventStream;
import it.polimi.deib.sr.obep.core.pipeline.abstration.Abstracter;
import it.polimi.deib.sr.obep.core.pipeline.normalization.NormalForm;
import it.polimi.deib.sr.obep.core.pipeline.normalization.Normalizer;
import it.polimi.deib.sr.obep.core.programming.Program;
import it.polimi.deib.sr.obep.core.programming.ProgramExecution;
import it.polimi.deib.sr.obep.core.programming.ProgramExecutionImpl;
import it.polimi.deib.sr.obep.core.programming.ProgramManager;
import it.polimi.deib.sr.obep.impl.content.MergeContentExpression;
import it.polimi.deib.sr.obep.impl.pipeline.AbstracterImpl;
import it.polimi.deib.sr.obep.impl.pipeline.CEP;
import it.polimi.deib.sr.obep.impl.pipeline.EPLFactory;
import it.polimi.deib.sr.obep.impl.pipeline.SPARQLNormalizer;
import it.polimi.deib.sr.obep.impl.programming.ProgramManagerImpl;
import lombok.extern.log4j.Log4j;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.manchestersyntax.parser.ManchesterOWLSyntaxParserImpl;
import org.semanticweb.owlapi.model.*;

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
    public EventStream register(String uri) {
        return null;
    }

    @Override
    public ProgramExecutionImpl register(Program q) {
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
                        String new_event = "create schema " + head + " as (event_content it.polimi.deib.sr.obep.core.data.events.Content)";

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
                    String name1 = ce.getHead() + "_" + name;
                    NormalForm nf = ce.normal_forms().get(name);
                    String new_event;
                    if (nf != null) {
                        new_event = nf.toString();
                        active_normal_forms.put(nf.event(), nf);
                    } else {
                        new_event = EPLFactory.toEPLSchema(name1, new ArrayList<>(), name);
                    }
                    cep.register_event(name1);
                    epl_program_builder.add(new_event);
                    cep.register_event_schema(new_event);

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

            //  Explainer explainer = new ExplainerImpl(ebox);

            normalizers.forEach(normalizer -> abstracter//.pipe(explainer)
                    .pipe(normalizer).pipe(cep));

            //CONNECT INPUT AND OUTPUTS STREAMS

            q.getOutputStreams().forEach(s -> cep.register_event_pattern_stream(connectToOut(epl_out_program_builder, s, new String[]{})));

            EPStatement epStatement = cep.register_event_pattern_stream(out_pattern);

            System.out.println(epl_program_builder.toString());
            StringJoiner add = epl_out_program_builder.add(out_pattern);
            System.out.println(add.toString());

            return new ProgramExecutionImpl(cep, abstracter, normalizers, epStatement);

        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
            return null;
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

    public MergeContentExpression merge(String... moreProperties) {
        return new MergeContentExpression(moreProperties);
    }

}

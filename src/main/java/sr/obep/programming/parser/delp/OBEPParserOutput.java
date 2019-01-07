package sr.obep.programming.parser.delp;

import com.espertech.esper.client.soda.PatternStream;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Node_URI;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.QueryBuildException;
import org.apache.jena.sparql.core.Prologue;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.graph.NodeConst;
import org.apache.jena.sparql.syntax.*;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.expression.ShortFormEntityChecker;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.BidirectionalShortFormProviderAdapter;
import org.semanticweb.owlapi.util.OntologyAxiomPair;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;
import org.semanticweb.owlapi.util.mansyntax.ManchesterOWLSyntaxParser;
import sr.obep.data.events.CompositeEvent;
import sr.obep.data.events.LogicalEvent;
import sr.obep.data.streams.EventStream;
import sr.obep.data.streams.WritableEventStream;
import sr.obep.pipeline.normalization.NormalForm;
import sr.obep.pipeline.normalization.SPARQLNormalForm;
import sr.obep.programming.ProgramImpl;
import sr.obep.programming.parser.delp.data.*;
import sr.obep.programming.parser.sparql.Prefix;
import sr.obep.programming.parser.sparql.SPARQLQuery;

import java.util.*;
import java.util.stream.Collectors;

import static org.semanticweb.owlapi.util.OWLAPIStreamUtils.asList;

/**
 * Created by Riccardo on 05/08/16.
 */
@Log4j
@Data
@EqualsAndHashCode(callSuper = false)

@NoArgsConstructor
public class OBEPParserOutput extends SPARQLQuery implements ProgramDeclaration {

    protected VarExprList MQLprojectVars = new VarExprList();
    private Map<String, ComplexEventDeclaration> eventDeclarations;
    private Set<CompositeEventDeclaration> compositeEvents;
    private Set<LogicalEventDeclaration> logicalEvents;
    private boolean MQLQyeryStar, emitQuery;
    private boolean MQLresultVarsSet;
    private Set<Prefix> prefixes = new HashSet<>();

    private Set<String> staticGraphURIs = new HashSet<>();
    private Set<String> streamUris = new HashSet<>();
    private String vocab;

    private OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
    private OWLDataFactory df = manager.getOWLDataFactory();
    private ResultClause rc;

    public OBEPParserOutput(Prologue prologue) {
        super(prologue);
    }

    @Override
    public OBEPParserOutput addElement(ElementGroup sub) {
        setQueryPattern(sub);

        // TODO UNION?
        if (this.isEmitQuery()) {
            Template template = new Template(buildConstruct(new TripleCollectorBGP(), sub).getBGP());
            setQConstructTemplate(template);
        }
        return this;
    }

    private TripleCollectorBGP buildConstruct(TripleCollectorBGP collector, Element element) {
        if (element instanceof ElementGroup) {
            for (Element e : ((ElementGroup) element).getElements()) {
                buildConstruct(collector, e);
            }
            return collector;
        } else if (element instanceof ElementNamedGraph) {
            collector.addTriple(new Triple(((ElementNamedGraph) element).getGraphNameNode(), NodeConst.nodeRDFType, NodeFactory.createURI("https://www.w3.org/TR/sparql11-service-description/#sd-namedGraphs")));
            Element namedGraph = ((ElementNamedGraph) element).getElement();
            return buildConstruct(collector, namedGraph);
        } else if (element instanceof ElementPathBlock) {
            ElementPathBlock epb = (ElementPathBlock) element;
            List<TriplePath> list = epb.getPattern().getList();
            for (TriplePath triplePath : list) {
                collector.addTriple(triplePath.asTriple());
            }
        }
        return collector;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    public OBEPParserOutput addEventDecl(ComplexEventDeclaration ed) {

        if (eventDeclarations == null)
            eventDeclarations = new HashMap<>();
        eventDeclarations.put(ed.getUri(), ed);

        OWLClass owlClass = df.getOWLClass(ed.getUri());
        ed.setHead(owlClass);

        if (ed instanceof CompositeEventDeclaration) {
            return addEventCalculusDecl((CompositeEventDeclaration) ed);
        } else if (ed instanceof LogicalEventDeclaration) {
            return addDLEventDecl((LogicalEventDeclaration) ed);

        }
        return this;
    }

    private OBEPParserOutput addDLEventDecl(LogicalEventDeclaration ed) {

        if (logicalEvents == null)
            logicalEvents = new HashSet<>();

        logicalEvents.add(ed);
        return this;
    }

    private OBEPParserOutput addEventCalculusDecl(CompositeEventDeclaration ed) {
        if (compositeEvents == null)
            compositeEvents = new HashSet<>();

        compositeEvents.add(ed);
        return this;
    }

    private OBEPParserOutput _addMQLVar(VarExprList varlist, Var v) {
        if (varlist == null)
            varlist = new VarExprList();

        if (varlist.contains(v)) {
            Expr expr = varlist.getExpr(v);
            if (expr != null)
                throw new QueryBuildException(
                        "Duplicate variable (had an expression) in result projection '" + v + "'");
        }
        varlist.add(v);
        return this;
    }

    public OBEPParserOutput setOBEPQueryStar() {
        this.MQLQyeryStar = true;
        return this;
    }

    public ComplexEventDeclaration getEventDecl(Node peek) {
        if (eventDeclarations == null) {
            return null;
        }
        return eventDeclarations.get(peek);
    }

    public SPARQLQuery setQBaseURI(String match) {
        setBaseURI(match);
        prefixes.add(new Prefix("base", match));
        return this;
    }

    public SPARQLQuery setPrefix(Prefix pop) {
        setPrefix(pop.getPrefix(), pop.getUri());
        prefixes.add(pop);
        return this;
    }

    @Override
    public ProgramImpl asProgram() {
        try {

            OBEPParserOutput q = this;

            Set<Prefix> prefixes = q.getPrefixes();

            OWLOntology dbox = manager.loadOntologyFromOntologyDocument(IRI.create(q.getVocab()));

            //Setup Input Streams

            Set<EventStream> inputstreams = q.getStreamUris().stream().map(WritableEventStream::new).collect(Collectors.toSet());


            //ADD Logical Event Axioms from LE declarations
            logicalEvents.stream().map(ComplexEventDeclaration::getHead).map(df::getOWLDeclarationAxiom).forEach(dbox::addAxiom);

            //ADD Composite Event Axioms from CE declarations
            compositeEvents.stream().map(ComplexEventDeclaration::getHead)
                    .map(df::getOWLClass).map(df::getOWLDeclarationAxiom).forEach(dbox::addAxiom);

            //Set the ontology to parse

            ManchesterOWLSyntaxParser mparser = OWLManager.createManchesterParser();
            mparser.setOWLEntityChecker(new ShortFormEntityChecker(new BidirectionalShortFormProviderAdapter(
                    asList(manager.ontologies()), new SimpleShortFormProvider())));
            mparser.setDefaultOntology(dbox);

            //Parse each of the axioms in the logical events declarations
            logicalEvents.stream().map(LogicalEventDeclaration::toString).forEach(System.out::println);
            logicalEvents.stream().map(LogicalEventDeclaration::toString)
                    .flatMap(s -> {
                        mparser.setStringToParse(s);
                        return mparser.parseClassFrame().stream();
                    }).map(OntologyAxiomPair::getAxiom).forEach(dbox::add);


            //Build LogicalEvent Set
            Set<LogicalEvent> les = logicalEvents.stream()
                    .map(led -> new LogicalEventImpl(led.getName(), led.getDlbody())
                    ).collect(Collectors.toSet());

            //Build CompositeEvent Set
            Set<CompositeEvent> ces = compositeEvents.stream()
                    .map(ced -> {
                        log.info(ced.toEpl());
                        Map<String, NormalForm> normalforms = new HashMap<>();
                        ced.getFilter_events().forEach(nfd -> {
                            IRI name = IRI.create(nfd.getVar().getURI());
                            IRI ctx = IRI.create(nfd.getContext());

                            normalforms.put(name.getShortForm(),
                                    new SPARQLNormalForm(ctx.getShortForm(), nfd.toSPARQL(),
                                            df.getOWLClass(name)));
                        });

                        return new CompositeEventImpl(ced.getName(), PatternStream.create(ced.toEpl()), normalforms, ced.getVariableAliases());

                    }).collect(Collectors.toSet());


            //Set out Output Streams

            Set<String> outpustreams = new HashSet<>();

            if (rc.isAll()) {
                les.forEach(le -> outpustreams.add(le.getHead()));
                ces.forEach(ce -> outpustreams.add(ce.getHead()));
            } else if (rc.isNamed()) {

                les.forEach(le -> outpustreams.add(le.getHead()));
                ces.forEach(ce -> outpustreams.add(ce.getHead()));

            } else {
                rc.getEvents().forEach(s -> outpustreams.add(IRI.create(s).getShortForm()));
            }

            //Create the Program
            return new ProgramImpl(
                    prefixes,
                    dbox,
                    inputstreams,
                    les,
                    ces,
                    outpustreams);


        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
            return null;
        }
    }

    public OBEPParserOutput addStaticGraphURI(Node_URI s) {
        if (staticGraphURIs == null)
            staticGraphURIs = new HashSet<>();
        staticGraphURIs.add(s.getURI());
        return this;
    }

    public OBEPParserOutput addStreamURI(Node_URI s) {
        if (streamUris == null)
            streamUris = new HashSet<>();
        streamUris.add(s.getURI());
        return this;
    }

    public OBEPParserOutput addVocabURI(Node_URI s) {
        vocab = s.getURI();
        return this;
    }

    public OBEPParserOutput addResultClause(ResultClause rc) {
        this.rc = rc;
        return this;
    }
}

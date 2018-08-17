package sr.obep.programming.parser.delp;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.QueryBuildException;
import org.apache.jena.sparql.core.Prologue;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.graph.NodeConst;
import org.apache.jena.sparql.syntax.*;
import sr.obep.programming.Program;
import sr.obep.programming.parser.delp.data.ComplexEventDeclaration;
import sr.obep.programming.parser.delp.data.CompositeEventDeclaration;
import sr.obep.programming.parser.delp.data.LogicalEventDeclaration;
import sr.obep.programming.parser.sparql.Prefix;
import sr.obep.programming.parser.sparql.SPARQLQuery;

import java.util.*;

/**
 * Created by Riccardo on 05/08/16.
 */
@Data
@NoArgsConstructor
public class OBEPParserOutput extends SPARQLQuery implements ProgramDeclaration {

    protected VarExprList MQLprojectVars = new VarExprList();
    private Map<Node, ComplexEventDeclaration> eventDeclarations;
    private Set<CompositeEventDeclaration> compositeEvents;
    private Set<LogicalEventDeclaration> logicalEvents;
    private boolean MQLQyeryStar, emitQuery;
    private boolean MQLresultVarsSet;
    private Set<Prefix> prefixes = new HashSet<>();

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
        eventDeclarations.put(ed.getHead_node(), ed);

        if (ed instanceof CompositeEventDeclaration)
            return addEventCalculusDecl((CompositeEventDeclaration) ed);
        else if (ed instanceof LogicalEventDeclaration)
            return addDLEventDecl((LogicalEventDeclaration) ed);

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
    public Program asProgram() {
        return null;
    }
}

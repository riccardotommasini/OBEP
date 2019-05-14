package it.polimi.deib.sr.obep.impl.parser.delp.declarations;

import com.espertech.esper.client.soda.EPStatementFormatter;
import com.espertech.esper.client.soda.FromClause;
import com.espertech.esper.client.soda.PatternExpr;
import com.espertech.esper.client.soda.PatternStream;
import lombok.Data;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.ExprFunction;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementFilter;
import it.polimi.deib.sr.obep.core.pipeline.normalization.NormalForm;

import java.io.StringWriter;
import java.util.*;

/**
 * Created by Riccardo on 17/08/16.
 */
public class CompositeEventDeclaration extends ComplexEventDeclaration {

    private PatternDeclaration expr;
    private List<NormalFormDeclaration> filter_events = new ArrayList<>();
    private List<NormalFormDeclaration> filters_var = new ArrayList<>();
    private List<ElementFilter> filters = new ArrayList<>();
    private Map<String, String> var_named = new HashMap<>();
    private Map<String, String> aliases = new HashMap<>();

    private Map<String, NormalForm> nfs = new HashMap<>();

    public CompositeEventDeclaration(Node head) {
        super(head);
    }

    public Set<Var> getJoinVariables() {
        Set<Var> joinVariables = null;
        if (filter_events != null)
            for (NormalFormDeclaration f : filter_events) {
                if (joinVariables == null) {
                    joinVariables = new HashSet<Var>(f.getVars());
                }
                joinVariables.retainAll(f.getVars());
            }
        if (filters_var != null)
            for (NormalFormDeclaration f : filters_var) {
                if (joinVariables == null) {
                    joinVariables = new HashSet<Var>(f.getVars());
                }
                joinVariables.addAll(f.getVars());
            }
        return joinVariables;
    }

    public PatternExpr toEpl() {
        PatternExpr pattern = expr.toEPL(head.getIRI().getShortForm(), filter_events, var_named, aliases);
        return pattern;
    }

    public void addEventFilter(NormalFormDeclaration pop) {
        if (filter_events == null) {
            filter_events = new ArrayList<>();
        }
        if (filters_var == null) {
            filters_var = new ArrayList<>();
        }
        if (pop.isDecl()) {
            filter_events.add(pop);
            for (NormalFormDeclaration fe : filters_var) {
                for (Element element : fe.getClause().getElements()) {
                    if (!pop.getClause().getElements().contains(element)) {
                        pop.addElement(element);
                    }
                }
            }
        } else if (pop.isVar()) {
            filters_var.add(pop);
            for (NormalFormDeclaration fe : filter_events) {
                for (Element element : pop.getClause().getElements()) {
                    if (!fe.getClause().getElements().contains(element)) {
                        fe.addElement(element);
                    }
                }
            }
        }
    }

    @Override
    public String toString() {
        FromClause fromClause = FromClause.create(PatternStream.create(toEpl()));
        StringWriter writer = new StringWriter();
        EPStatementFormatter formatter = new EPStatementFormatter();
        fromClause.getStreams().get(0).toEPL(writer, formatter);
        return writer.toString();
    }

    public void addFilter(ElementFilter pop) {

        //look if this filter belong to any of the pre existing if declarations
        if (pop.getExpr().isFunction()) {
            ExprFunction ef = (ExprFunction) pop.getExpr();
            ef.getArgs().forEach(expr1 -> {
                if (expr1.isVariable()) {
                    filter_events.forEach(nf -> {
                        if (nf.getVars().stream().anyMatch(var -> var.getName().equals(expr1.getVarName()))) {
                            nf.addElement(pop);
                            return;
                        }
                    });
                }
            });
        }
        //ELSE, is a generic filter to evaluate at the EPL Level
        filters.add(pop);
    }

    public Map<String, String> getVariableAliases() {
        return aliases;
    }

    public PatternDeclaration getExpr() {
        return expr;
    }

    public void setExpr(PatternDeclaration expr) {
        this.expr = expr;
    }

    public List<NormalFormDeclaration> getFilter_events() {
        return filter_events;
    }

    public void setFilter_events(List<NormalFormDeclaration> filter_events) {
        this.filter_events = filter_events;
    }
}

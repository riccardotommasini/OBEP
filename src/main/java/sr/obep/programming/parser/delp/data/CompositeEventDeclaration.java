package sr.obep.programming.parser.delp.data;

import com.espertech.esper.client.soda.*;
import lombok.Data;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.ExprFunction;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementFilter;

import java.util.*;

/**
 * Created by Riccardo on 17/08/16.
 */
@Data
public class CompositeEventDeclaration extends ComplexEventDeclaration {

    private PatternDeclaration expr;
    private List<NormalFormDeclaration> filter_events;
    private List<NormalFormDeclaration> filters_var;
    private List<ElementFilter> filters = new ArrayList<>();
    private Map<String, String> var_named = new HashMap<>();

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

    public EPStatementObjectModel toEpl() {
        EPStatementObjectModel model = new EPStatementObjectModel();
        model.setSelectClause(SelectClause.createWildcard());
        PatternExpr pattern = expr.toEPL(filter_events, var_named);
        model.setFromClause(FromClause.create(PatternStream.create(pattern)));
        return model;
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
        return toEpl().toEPL();
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
}

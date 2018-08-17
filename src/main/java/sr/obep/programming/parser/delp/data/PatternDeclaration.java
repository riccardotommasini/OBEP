package sr.obep.programming.parser.delp.data;

import com.espertech.esper.client.soda.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Node_URI;
import org.apache.jena.sparql.core.Var;

import java.util.*;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Riccardo on 17/08/16.
 */

@Getter
@Setter
@RequiredArgsConstructor
public class PatternDeclaration {
    final private String regex = "([0-9]+)\\s*(ms|s|m|h|d)";
    final private Pattern p = Pattern.compile(regex);

    private String operator;
    private Node var;
    private List<PatternDeclaration> patterns;
    private ComplexEventDeclaration eventDecl;
    private NormalFormDeclaration eventFilter;
    private boolean bracketed = false;
    private String name;
    private String within;
    private List<NormalFormDeclaration> global_filters;


    public PatternDeclaration(PatternDeclaration pop) {
        addPattern(pop);
        bracketed = true;
    }

    public PatternDeclaration(String s) {
        this.operator = s;
    }

    public PatternDeclaration(ComplexEventDeclaration ef, Node var) {
        this.var = var;
        this.eventDecl = ef;
    }

    public PatternDeclaration(String match, PatternDeclaration pop) {
        operator = "WITHIN";
        PatternDeclaration var = new PatternDeclaration();
        var.setWithin(match); //TODO
        if (patterns == null)
            patterns = new ArrayList<>();
        patterns.add(pop);
        patterns.add(var);

    }

    private boolean isVar() {
        return var != null;
    }

    public void addPattern(PatternDeclaration p) {
        if (patterns == null)
            patterns = new ArrayList<PatternDeclaration>();
        patterns.add(p);
    }

    @Override
    public String toString() {
        String s = "";

        if (within != null)
            return "(" + within + ")";

        if (isVar()) {
            return getVarName();
        }

        if (operator != null && ("every".equals(operator.toLowerCase()) || "not".equals(operator.toLowerCase()))) {
            return operator + " (" + patterns.get(0) + ")";
        }

        if (operator == null && patterns.size() == 1) {
            s += bracketed ? "(" : "";
            s += patterns.get(0).toString();
            s += bracketed ? ")" : "";
            return s;
        }

        s += bracketed ? "(" : "";

        PatternDeclaration pc;
        for (int i = 0; i < patterns.size() - 1; i++) {
            pc = patterns.get(i);

            s += pc.toString();

            s += " " + operator + " ";
        }

        pc = patterns.get(patterns.size() - 1);

        s += pc.toString();

        s += bracketed ? ")" : "";

        return s;
    }

    private TimePeriodExpression toTimeExpr() {
        Matcher matcher = p.matcher(within);
        if (matcher.find()) {
            MatchResult res = matcher.toMatchResult();
            if ("ms".equals(res.group(2))) {
                return Expressions.timePeriod(null, null, null, null, Integer.parseInt(res.group(1)));
            } else if ("s".equals(res.group(2))) {
                return Expressions.timePeriod(null, null, null, Integer.parseInt(res.group(1)), null);
            } else if ("m".equals(res.group(2))) {
                return Expressions.timePeriod(null, null, Integer.parseInt(res.group(1)), null, null);
            } else if ("h".equals(res.group(2))) {
                return Expressions.timePeriod(null, Integer.parseInt(res.group(1)), null, null, null);
            } else if ("d".equals(res.group(2))) {
                return Expressions.timePeriod(Integer.parseInt(res.group(1)), null, null, null, null);
            }
        }
        return null;
    }

    private PatternExpr createFilter(int currentIndex, List<NormalFormDeclaration> filters) {
        Conjunction andExpr = Expressions.and();
        for (int j = 0; j < filters.size(); j++) {
            if (j == currentIndex) {
                continue;
            }


            Set<Var> vars = new HashSet<Var>(eventFilter.getVars());
            NormalFormDeclaration id = filters.get(j);
            id.build();
            vars.retainAll(id.getVars());

            for (Var v : vars) {
                if (id.getName() != null) {
                    andExpr.add(Expressions.eqProperty(v.getVarName(), id.getName() + "." + v.getVarName()));
                }
            }
        }

        if (andExpr.getChildren() == null || andExpr.getChildren().isEmpty()) {
            return Patterns.filter(getVarName(), this.name = getVarName() + currentIndex);

        } else if (andExpr.getChildren().size() == 1) {
            return Patterns.filter(Filter.create(getVarName(), andExpr.getChildren().get(0)), getVarName() + currentIndex);
        }

        return Patterns.filter(Filter.create(getVarName(), andExpr), getVarName() + currentIndex);
    }

    public PatternExpr toEPL(List<NormalFormDeclaration> filters_event, Map<String, String> var_named) {

        if (var != null) {
            if (filters_event != null && !filters_event.isEmpty()) {
                for (int i = 0; i < filters_event.size(); i++) {
                    eventFilter = filters_event.get(i);
                    eventFilter.build();
                    if (eventFilter.getName() == null) {
                        String name = getVarName() + i;
                        eventFilter.setName(name);
                    }

                    var_named.put(var.getURI(), eventFilter.getName());

                    if (var.equals(filters_event.get(i).getVar())) {
                        return createFilter(i, filters_event);
                    }
                }
            }

            return Patterns.filter(getVarName(), this.name = getVarName() + 0);
        }

        if (bracketed || (operator == null || operator.isEmpty()) && patterns != null && patterns.size() == 1) {
            return patterns.get(0).toEPL(filters_event, var_named);
        }

        PatternExpr pattern = null;
        if (operator != null) {
            if ("within".equals(operator.toLowerCase())) {
                TimePeriodExpression timeExpr = patterns.get(1).toTimeExpr();
                return Patterns.guard("timer", "within", new Expression[]{timeExpr}, patterns.get(0).toEPL(filters_event, var_named));
            } else if ("every".equals(operator.toLowerCase())) {
                return Patterns.every(patterns.get(0).toEPL(filters_event, var_named));
            } else if ("not".equals(operator.toLowerCase())) {
                return Patterns.not(patterns.get(0).toEPL(filters_event, var_named));
            } else if ("->".equals(operator.toLowerCase())) {
                pattern = Patterns.followedBy();
                for (PatternDeclaration p : patterns) {
                    ((PatternFollowedByExpr) pattern).add(p.toEPL(filters_event, var_named));
                }
            } else if ("or".equals(operator.toLowerCase())) {
                pattern = Patterns.or();
                for (PatternDeclaration p : patterns) {
                    ((PatternOrExpr) pattern).add(p.toEPL(filters_event, var_named));
                }
            } else if ("and".equals(operator.toLowerCase())) {
                pattern = Patterns.and();
                for (PatternDeclaration p : patterns) {
                    ((PatternAndExpr) pattern).add(p.toEPL(filters_event, var_named));
                }

            }
        }

        return pattern;

    }


    private String getVarName() {
        if (var != null && var instanceof Node_URI) {
            return var.getURI().replace(var.getNameSpace(), "");
        }
        return var.getName();
    }

    private List<NormalFormDeclaration> getIfDeclarations() {
        List<NormalFormDeclaration> ifDeclarations = new ArrayList<NormalFormDeclaration>();
        if (isVar() && eventDecl != null)
            ifDeclarations.add(eventFilter);

        if (patterns != null) {
            for (PatternDeclaration p : patterns) {
                ifDeclarations.addAll(p.getIfDeclarations());
            }
        }
        return ifDeclarations;
    }

    public boolean isURI() {
        return var != null && var instanceof Node_URI;
    }
}

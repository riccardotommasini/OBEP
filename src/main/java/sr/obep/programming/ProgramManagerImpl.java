package sr.obep.programming;

import com.espertech.esper.client.soda.EPStatementObjectModel;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.parboiled.Parboiled;
import org.parboiled.errors.ParseError;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;
import sr.obep.data.events.LogicalEvent;
import sr.obep.programming.parser.delp.DELPParser;
import sr.obep.programming.parser.delp.data.LogicalEventDeclaration;
import sr.obep.programming.parser.delp.data.CompositeEventDeclaration;
import sr.obep.programming.parser.delp.data.ComplexEventDeclaration;
import sr.obep.programming.parser.delp.OBEPParserOutput;
import sr.obep.programming.parser.sparql.Prefix;

import java.util.Map;
import java.util.Set;

public class ProgramManagerImpl implements ProgramManager {


    @Override
    public Program parse(String program) {

        ProgramImpl res = new ProgramImpl();


        Set<Prefix> prefixes = res.getPrefixes();


        DELPParser parser = Parboiled.createParser(DELPParser.class);

        ParsingResult<OBEPParserOutput> result = new ReportingParseRunner(parser.Query()).run(program);

        if (result.hasErrors()) {
            for (ParseError arg : result.parseErrors) {
                System.out.println(program.substring(0, arg.getStartIndex()) + "|->" + program.substring(arg.getStartIndex(), arg.getEndIndex()) + "<-|" + program.substring(arg.getEndIndex() + 1, program.length() - 1));
            }
        }

        OBEPParserOutput q = result.resultValue;

        q.getPrefixes().forEach(prefixes::add);

        if (q.getEventDeclarations() != null) {
            Map<Node, ComplexEventDeclaration> eventDeclarations = q.getEventDeclarations();
            Set<Map.Entry<Node, ComplexEventDeclaration>> entries = eventDeclarations.entrySet();
            for (Map.Entry<Node, ComplexEventDeclaration> entry : entries) {
                ComplexEventDeclaration value = entry.getValue();

                if (value instanceof CompositeEventDeclaration) {

                    CompositeEventDeclaration e = (CompositeEventDeclaration) value;
                    System.out.println(e.toString());

                    Set<Var> joinVariables = e.getJoinVariables();

                    if (q.getEventDeclarations() != null) {
                        for (Map.Entry<Node, ComplexEventDeclaration> en : entries) {
                            ComplexEventDeclaration v = en.getValue();

                            if (v instanceof LogicalEventDeclaration) {
                                final LogicalEventDeclaration dl = (LogicalEventDeclaration) v;

                                String s = dl.toEPLSchema(joinVariables);
                                System.out.println(s);
                                res.getLogicalEvents().add(new LogicalEvent() {
                                    @Override
                                    public String getHead() {
                                        return dl.getHead_node().toString();
                                    }

                                    @Override
                                    public String getBody() {
                                        return dl.getBody();
                                    }
                                });

                            }

                        }
                    }

                    EPStatementObjectModel epStatementObjectModel = e.toEpl();
                    System.out.println(epStatementObjectModel.toEPL());
                }

            }
        }

        return res;
    }

    @Override
    public Program register(String program) {
        return null;
    }
}






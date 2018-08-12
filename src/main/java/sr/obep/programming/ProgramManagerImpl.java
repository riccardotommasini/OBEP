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
import sr.obep.programming.parser.delp.DLEventDecl;
import sr.obep.programming.parser.delp.EventCalculusDecl;
import sr.obep.programming.parser.delp.EventDecl;
import sr.obep.programming.parser.OBEPQueryImpl;

import java.util.Map;
import java.util.Set;

public class ProgramManagerImpl implements ProgramManager {


    @Override
    public Program parse(String program) {

        ProgramImpl res = new ProgramImpl();

        DELPParser parser = Parboiled.createParser(DELPParser.class);

        ParsingResult<OBEPQueryImpl> result = new ReportingParseRunner(parser.Query()).run(program);

        if (result.hasErrors()) {
            for (ParseError arg : result.parseErrors) {
                System.out.println(program.substring(0, arg.getStartIndex()) + "|->" + program.substring(arg.getStartIndex(), arg.getEndIndex()) + "<-|" + program.substring(arg.getEndIndex() + 1, program.length() - 1));
            }
        }

        OBEPQueryImpl q = result.resultValue;

        if (q.getEventDeclarations() != null) {
            Map<Node, EventDecl> eventDeclarations = q.getEventDeclarations();
            Set<Map.Entry<Node, EventDecl>> entries = eventDeclarations.entrySet();
            for (Map.Entry<Node, EventDecl> entry : entries) {
                EventDecl value = entry.getValue();

                if (value instanceof EventCalculusDecl) {

                    EventCalculusDecl e = (EventCalculusDecl) value;
                    System.out.println(e.toString());

                    Set<Var> joinVariables = e.getJoinVariables();

                    if (q.getEventDeclarations() != null) {
                        for (Map.Entry<Node, EventDecl> en : entries) {
                            EventDecl v = en.getValue();

                            if (v instanceof DLEventDecl) {
                                final DLEventDecl dl = (DLEventDecl) v;

                                String s = dl.toEPLSchema(joinVariables);
                                System.out.println(s);
                                res.getLogicalEvents().add(new LogicalEvent() {
                                    @Override
                                    public String getHead() {
                                        return dl.getHead().toString();
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






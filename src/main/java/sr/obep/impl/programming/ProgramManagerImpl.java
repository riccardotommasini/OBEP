package sr.obep.impl.programming;

import org.apache.jena.sparql.core.Var;
import org.parboiled.Parboiled;
import org.parboiled.errors.ParseError;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;
import sr.obep.core.data.events.LogicalEvent;
import sr.obep.core.programming.Program;
import sr.obep.core.programming.ProgramManager;
import sr.obep.core.programming.ProgramImpl;
import sr.obep.impl.parser.delp.parser.OBEPParser;
import sr.obep.impl.parser.delp.declarations.LogicalEventDeclaration;
import sr.obep.impl.parser.delp.declarations.CompositeEventDeclaration;
import sr.obep.impl.parser.delp.declarations.ComplexEventDeclaration;
import sr.obep.impl.parser.delp.parser.OBEPParserOutput;
import sr.obep.impl.parser.sparql.Prefix;

import java.util.Map;
import java.util.Set;

public class ProgramManagerImpl implements ProgramManager {


    @Override
    public Program parse(String program) {

        ProgramImpl res = null;


        Set<Prefix> prefixes = res.getPrefixes();


        OBEPParser parser = Parboiled.createParser(OBEPParser.class);

        ParsingResult<OBEPParserOutput> result = new ReportingParseRunner(parser.Query()).run(program);

        if (result.hasErrors()) {
            for (ParseError arg : result.parseErrors) {
                System.out.println(program.substring(0, arg.getStartIndex()) + "|->" + program.substring(arg.getStartIndex(), arg.getEndIndex()) + "<-|" + program.substring(arg.getEndIndex() + 1, program.length() - 1));
            }
        }

        OBEPParserOutput q = result.resultValue;

        q.getPrefixes().forEach(prefixes::add);

        if (q.getEventDeclarations() != null) {
            Map<String, ComplexEventDeclaration> eventDeclarations = q.getEventDeclarations();
            Set<Map.Entry<String, ComplexEventDeclaration>> entries = eventDeclarations.entrySet();
            for (Map.Entry<String, ComplexEventDeclaration> entry : entries) {
                ComplexEventDeclaration value = entry.getValue();

                if (value instanceof CompositeEventDeclaration) {

                    CompositeEventDeclaration e = (CompositeEventDeclaration) value;
                    System.out.println(e.toString());

                    Set<Var> joinVariables = e.getJoinVariables();

                    if (q.getEventDeclarations() != null) {
                        for (Map.Entry<String, ComplexEventDeclaration> en : entries) {
                            ComplexEventDeclaration v = en.getValue();

                            if (v instanceof LogicalEventDeclaration) {
                                final LogicalEventDeclaration dl = (LogicalEventDeclaration) v;

                                String s = dl.toEPLSchema(joinVariables);
                                System.out.println(s);
                                res.getLogicalEvents().add(new LogicalEvent() {
                                    @Override
                                    public String getHead() {
                                        return dl.getUri();
                                    }

                                    @Override
                                    public String getBody() {
                                        return dl.getDlbody();
                                    }
                                });

                            }

                        }
                    }

                    System.out.println(e.toEpl().toString());
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






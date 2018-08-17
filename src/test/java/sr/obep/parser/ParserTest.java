package sr.obep.parser;

import com.espertech.esper.client.soda.EPStatementObjectModel;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.parboiled.Parboiled;
import org.parboiled.errors.ParseError;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;
import sr.obep.programming.parser.delp.OBEPParserOutput;
import sr.obep.programming.parser.delp.DELPParser;
import sr.obep.programming.parser.delp.data.CompositeEventDeclaration;
import sr.obep.programming.parser.sparql.Prefix;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class ParserTest {


    @Test
    public void test0() {
        URL resource = ParserTest.class.getResource("/test.query");

        sr.obep.programming.ProgramImpl res = new sr.obep.programming.ProgramImpl();

        DELPParser parser = Parboiled.createParser(DELPParser.class);

        String program = getFile(resource.getPath());

        ParsingResult<OBEPParserOutput> result = new ReportingParseRunner(parser.Query()).run(program);

        if (result.hasErrors()) {
            for (ParseError arg : result.parseErrors) {
                System.out.println(program.substring(0, arg.getStartIndex()) + "|->" + program.substring(arg.getStartIndex(), arg.getEndIndex()) + "<-|" + program.substring(arg.getEndIndex() + 1, program.length() - 1));
            }
        }


        OBEPParserOutput q = result.resultValue;

        Set<Prefix> prefixes = new HashSet<>(q.getPrefixes());

        q.getLogicalEvents().forEach(System.out::println);

        q.getCompositeEvents().stream().map(CompositeEventDeclaration::toEpl).map(EPStatementObjectModel::toEPL).forEach(System.out::println);

        q.getCompositeEvents().stream().flatMap(ce -> ce.getVar_named().entrySet().stream())
                .forEach(e -> System.out.println(e.getKey() + " " + e.getValue()));
    }


    private String getFile(String fileName) {

        String result = "";


        try {
            result = IOUtils.toString(new File(fileName).toURI());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;

    }
}

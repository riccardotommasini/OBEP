package sr.obep.impl.pipeline;

import com.espertech.esper.client.soda.*;
import lombok.extern.log4j.Log4j;
import org.apache.jena.sparql.core.Var;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * Created by riccardo on 04/09/2017.
 */
@Log4j
public class EPLFactory {


    public static EPStatementObjectModel toEPL(int step, String unitStep, String s, View window, List<AnnotationPart> annotations) {
        EPStatementObjectModel stmt = new EPStatementObjectModel();

        stmt.setAnnotations(annotations);
        InsertIntoClause insertIntoClause = InsertIntoClause.create("");

        SelectClause selectClause = SelectClause.create().addWildcard();
        stmt.setSelectClause(selectClause);
        FromClause fromClause = FromClause.create();
        FilterStream stream = FilterStream.create(s);
        stream.addView(window);
        fromClause.add(stream);
        stmt.setFromClause(fromClause);

        OutputLimitClause outputLimitClause;

        TimePeriodExpression timePeriod = getTimePeriod(step, unitStep);
        outputLimitClause = OutputLimitClause.create(OutputLimitSelector.SNAPSHOT, timePeriod);

        stmt.setOutputLimitClause(outputLimitClause);
        return stmt;
    }


    public static List<AnnotationPart> getAnnotations(String name1, int range1, int step1, String s) {
        AnnotationPart name = new AnnotationPart();
        name.setName("Name");
        name.addValue(name1);

        AnnotationPart range = new AnnotationPart();
        range.setName("Tag");
        range.addValue("name", "range");
        range.addValue("value", range1 + "");

        AnnotationPart slide = new AnnotationPart();
        slide.setName("Tag");
        slide.addValue("name", "step");
        slide.addValue("value", step1 + "");

        AnnotationPart stream_uri = new AnnotationPart();
        stream_uri.setName("Tag");
        stream_uri.addValue("name", "stream");
        stream_uri.addValue("value", s);

        return Arrays.asList(name, stream_uri, range, slide);
    }


    public static View getWindow(int range, String unitRange) {
        View view;
        ArrayList<Expression> parameters = new ArrayList<Expression>();
        parameters.add(getTimePeriod(range, unitRange));
        view = View.create("win", "time", parameters);
        return view;
    }

    private static TimePeriodExpression getTimePeriod(Integer omega, String unit_omega) {
        String unit = unit_omega.toLowerCase();
        if ("ms".equals(unit) || "millis".equals(unit) || "milliseconds".equals(unit)) {
            return Expressions.timePeriod(null, null, null, null, omega);
        } else if ("s".equals(unit) || "seconds".equals(unit) || "sec".equals(unit)) {
            return Expressions.timePeriod(null, null, null, omega, null);
        } else if ("m".equals(unit) || "minutes".equals(unit) || "min".equals(unit)) {
            return Expressions.timePeriod(null, null, omega, null, null);
        } else if ("h".equals(unit) || "hours".equals(unit) || "hour".equals(unit)) {
            return Expressions.timePeriod(null, omega, null, null, null);
        } else if ("d".equals(unit) || "days".equals(unit)) {
            return Expressions.timePeriod(omega, null, null, null, null);
        }
        return null;
    }

    public static String toEPLSchema(String name, List<Var> vars, String... inherits) {
        CreateSchemaClause schema = new CreateSchemaClause();
        schema.setSchemaName(name);
        schema.setInherits(new HashSet<>(Arrays.asList(inherits)));
        List<SchemaColumnDesc> columns = new ArrayList<>();

//        Arrays.asList(
//                //    new SchemaColumnDesc("sys_timestamp", "long", false),
//                //   new SchemaColumnDesc("app_timestamp", "long", false),
//                new SchemaColumnDesc("content", Object.class.getTypeName(), false))

        vars.forEach(var -> columns.add(new SchemaColumnDesc(var.getVarName(), "string", false)));

        schema.setColumns(columns);
        StringWriter writer = new StringWriter();
        schema.toEPL(writer);
        return writer.toString();
    }


    public static String toEPL(PatternStream body) {
        StringWriter writer = new StringWriter();
        EPStatementFormatter formatter = new EPStatementFormatter();
        body.toEPL(writer, formatter);
        return writer.toString();
    }
}

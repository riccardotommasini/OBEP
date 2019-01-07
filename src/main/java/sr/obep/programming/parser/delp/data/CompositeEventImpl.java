package sr.obep.programming.parser.delp.data;

import com.espertech.esper.client.soda.EPStatementFormatter;
import com.espertech.esper.client.soda.PatternExpr;
import com.espertech.esper.client.soda.PatternStream;
import lombok.AllArgsConstructor;
import sr.obep.data.events.CompositeEvent;
import sr.obep.pipeline.normalization.NormalForm;

import java.io.StringWriter;
import java.util.Map;

@AllArgsConstructor
public class CompositeEventImpl implements CompositeEvent {

    private String head;
    private PatternStream body;
    protected Map<String, NormalForm> normal_forms;
    protected Map<String, String> alias;

    @Override
    public String getHead() {
        return head;
    }

    @Override
    public String getBody() {
        StringWriter writer = new StringWriter();
        EPStatementFormatter formatter = new EPStatementFormatter();
        body.toEPL(writer, formatter);
        return writer.toString();
    }

    @Override
    public String[] named() {
        return alias.keySet().toArray(new String[alias.size()]);
    }

    @Override
    public Map<String, String> body_schemas() {
        return null;
    }

    @Override
    public Map<String, NormalForm> normal_forms() {
        return normal_forms;
    }

    @Override
    public String alias(String name) {
        return alias.get(name);
    }
}

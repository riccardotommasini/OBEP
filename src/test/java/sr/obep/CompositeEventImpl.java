package sr.obep;

import lombok.AllArgsConstructor;
import sr.obep.data.events.CompositeEvent;
import sr.obep.pipeline.normalization.NormalForm;

import java.util.Map;

@AllArgsConstructor
public class CompositeEventImpl implements CompositeEvent {

    private String head, body;
    protected Map<String, NormalForm> normal_forms;
    protected Map<String, String> alias;

    @Override
    public String getHead() {
        return head;
    }

    @Override
    public String getBody() {
        return body;
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

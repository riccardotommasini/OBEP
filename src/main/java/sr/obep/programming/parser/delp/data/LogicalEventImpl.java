package sr.obep.programming.parser.delp.data;

import lombok.AllArgsConstructor;
import sr.obep.data.events.LogicalEvent;

@AllArgsConstructor
public class LogicalEventImpl implements LogicalEvent {

    private String head, body;

    @Override
    public String getHead() {
        return head;
    }

    @Override
    public String getBody() {
        return body;
    }

}

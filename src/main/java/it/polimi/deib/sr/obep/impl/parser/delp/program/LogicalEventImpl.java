package it.polimi.deib.sr.obep.impl.parser.delp.program;

import it.polimi.deib.sr.obep.core.data.events.LogicalEvent;
import lombok.AllArgsConstructor;

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

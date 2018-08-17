package sr.obep;

import sr.obep.data.events.LogicalEvent;

public class LogicalEventImpl implements LogicalEvent {

    private String head, body;

    public LogicalEventImpl(String e) {
        String[] split = e.split(":=");
        head = split[0];
        body = split[1];
    }

    @Override
    public String getHead() {
        return head;
    }

    @Override
    public String getBody() {
        return body;
    }

}

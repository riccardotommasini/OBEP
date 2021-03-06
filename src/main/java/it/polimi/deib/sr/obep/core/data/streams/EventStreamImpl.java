package it.polimi.deib.sr.obep.core.data.streams;

import it.polimi.deib.sr.obep.core.pipeline.processors.EventProcessor;
import lombok.RequiredArgsConstructor;
import it.polimi.deib.sr.obep.impl.RawEvent;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class EventStreamImpl implements EventStream {

    private List<EventProcessor> processors = new ArrayList<>();

    private final String name;

    @Override
    public EventStream connectTo(EventProcessor processor) {
        processors.add(processor);
        return this;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public void put(RawEvent e) {
        processors.forEach(eventProcessor -> eventProcessor.send(e));
    }
}

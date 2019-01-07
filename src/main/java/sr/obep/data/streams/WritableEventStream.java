package sr.obep.data.streams;

import lombok.RequiredArgsConstructor;
import sr.obep.data.events.RawEvent;
import sr.obep.pipeline.processors.EventProcessor;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class WritableEventStream implements EventStream {

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

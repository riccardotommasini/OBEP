package sr.obep.data.streams;

import sr.obep.processors.EventProcessor;

public interface EventStream {
    EventStream connectTo(EventProcessor processor);
}

package sr.obep.data;

import sr.obep.processors.EventProcessor;

public interface EventStream {
    EventStream connectTo(EventProcessor processor);
}

package sr.obep.data.streams;

import sr.obep.pipeline.processors.EventProcessor;

public interface EventStream {
    EventStream connectTo(EventProcessor processor);

    String name();

}

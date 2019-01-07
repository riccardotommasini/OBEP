package sr.obep.core.data.streams;

import sr.obep.impl.RawEvent;
import sr.obep.core.pipeline.processors.EventProcessor;

//TODO change to respect the definition and start from RDF Stream
public interface EventStream {

    EventStream connectTo(EventProcessor processor);

    String name();

    void put(RawEvent e);

}

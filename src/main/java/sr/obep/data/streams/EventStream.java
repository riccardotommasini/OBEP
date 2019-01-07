package sr.obep.data.streams;

import sr.obep.data.events.RawEvent;
import sr.obep.pipeline.processors.EventProcessor;

//TODO change to respect the definition and start from RDF Stream
public interface EventStream {

    EventStream connectTo(EventProcessor processor);

    String name();

    void put(RawEvent e);

}

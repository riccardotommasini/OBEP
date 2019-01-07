package it.polimi.deib.sr.obep.core.data.streams;

import it.polimi.deib.sr.obep.impl.RawEvent;
import it.polimi.deib.sr.obep.core.pipeline.processors.EventProcessor;

//TODO change to respect the definition and start from RDF Stream
public interface EventStream {

    EventStream connectTo(EventProcessor processor);

    String name();

    void put(RawEvent e);

}

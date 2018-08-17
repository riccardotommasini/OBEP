package sr.obep.programming;

import sr.obep.data.events.CompositeEvent;
import sr.obep.data.events.LogicalEvent;
import sr.obep.data.streams.EventStream;
import sr.obep.programming.parser.sparql.Prefix;

import java.util.Set;

/**
 * Created by Riccardo on 03/11/2016.
 */
public interface Program {


    Set<Prefix> getPrefixes();

    Set<EventStream> getInputStreams();

    Set<LogicalEvent> getLogicalEvents();

    Set<CompositeEvent> getCompositeEvents();

    Set<String> getOutputStreams();

}

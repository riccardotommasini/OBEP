package sr.obep.programming;

import sr.obep.data.events.CompositeEvent;
import sr.obep.data.events.LogicalEvent;
import sr.obep.data.streams.EventStream;
import sr.obep.programming.parser.sparql.Prefix;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Riccardo on 03/11/2016.
 */

public class ProgramImpl implements Program {

    private final Set<Prefix> prefixes = new HashSet<>();
    private final Set<EventStream> eventStreams = new HashSet<>();
    private final Set<LogicalEvent> logicalEvents = new HashSet<>();
    private final Set<CompositeEvent> compositeEvents = new HashSet<>();


    @Override
    public Set<Prefix> getPrefixes() {
        return prefixes;
    }

    @Override
    public Set<EventStream> getInputStreams() {
        return eventStreams;
    }

    @Override
    public Set<LogicalEvent> getLogicalEvents() {
        return getLogicalEvents();
    }

    @Override
    public Set<CompositeEvent> getCompositeEvents() {
        return getCompositeEvents();
    }
}

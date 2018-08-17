package sr.obep.programming;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Setter;
import sr.obep.data.events.CompositeEvent;
import sr.obep.data.events.LogicalEvent;
import sr.obep.data.streams.EventStream;
import sr.obep.programming.parser.sparql.Prefix;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Riccardo on 03/11/2016.
 */
@AllArgsConstructor
public final class ProgramImpl implements Program {

    @Setter(AccessLevel.PACKAGE)
    private final Set<Prefix> prefixes = new HashSet<>();
    private final Set<EventStream> inputstreams = new HashSet<>();
    private final Set<String> outpustreams = new HashSet<>();
    private final Set<LogicalEvent> logicalEvents = new HashSet<>();
    private final Set<CompositeEvent> compositeEvents = new HashSet<>();

    @Override
    public Set<Prefix> getPrefixes() {
        return prefixes;
    }

    @Override
    public Set<EventStream> getInputStreams() {
        return inputstreams;
    }

    @Override
    public Set<LogicalEvent> getLogicalEvents() {
        return logicalEvents;
    }

    @Override
    public Set<CompositeEvent> getCompositeEvents() {
        return compositeEvents;
    }

    @Override
    public Set<String> getOutputStreams() {
        return outpustreams;
    }

}

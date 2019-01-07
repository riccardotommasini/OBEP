package sr.obep.core.programming;

import lombok.AllArgsConstructor;
import org.semanticweb.owlapi.model.OWLOntology;
import sr.obep.core.data.events.LogicalEvent;
import sr.obep.core.data.streams.EventStream;
import sr.obep.core.data.events.CompositeEvent;
import sr.obep.impl.parser.sparql.Prefix;

import java.util.Set;

/**
 * Created by Riccardo on 03/11/2016.
 */
@AllArgsConstructor
public final class ProgramImpl implements Program {

    private final Set<Prefix> prefixes;

    private final OWLOntology dbox;

    private final Set<EventStream> inputstreams;

    private final Set<LogicalEvent> logicalEvents;
    private final Set<CompositeEvent> compositeEvents;

    private final Set<String> outpustreams;

    @Override
    public OWLOntology getOntology() {
        return dbox;
    }

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

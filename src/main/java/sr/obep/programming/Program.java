package sr.obep.programming;

import org.semanticweb.owlapi.model.OWLOntology;
import sr.obep.data.events.CompositeEvent;
import sr.obep.data.events.LogicalEvent;
import sr.obep.data.streams.EventStream;
import sr.obep.programming.parser.sparql.Prefix;

import java.util.Set;

/**
 * Created by Riccardo on 03/11/2016.
 */
public interface Program {

    OWLOntology getOntology();

    Set<Prefix> getPrefixes();

    Set<EventStream> getInputStreams();

    Set<? extends LogicalEvent> getLogicalEvents();

    Set<? extends CompositeEvent> getCompositeEvents();

    Set<String> getOutputStreams();

}

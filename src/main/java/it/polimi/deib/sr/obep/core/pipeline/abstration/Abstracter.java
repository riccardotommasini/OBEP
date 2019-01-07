package it.polimi.deib.sr.obep.core.pipeline.abstration;

import it.polimi.deib.sr.obep.core.pipeline.processors.EventProcessor;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.Set;

/**
 * Created by Riccardo on 03/11/2016.
 */

public interface Abstracter extends EventProcessor {

    Set<OWLClass> lift(OWLOntology copy, OWLNamedIndividual event);

}

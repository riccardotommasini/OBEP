package it.polimi.deib.sr.obep.core.pipeline.explanation;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import it.polimi.deib.sr.obep.core.pipeline.processors.EventProcessor;

import java.util.Set;

/**
 * This interface returns the set of all the owl axioms that entail the given event class
 */

public interface Explainer extends EventProcessor {

    Set<Set<OWLAxiom>> explain(OWLOntology ontology, OWLNamedIndividual message, OWLClass c) throws ExplanationException;
}

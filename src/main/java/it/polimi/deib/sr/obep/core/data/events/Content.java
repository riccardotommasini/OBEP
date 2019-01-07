package it.polimi.deib.sr.obep.core.data.events;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.Set;

public interface Content {

    OWLOntology asOWLOntology();

    Set<OWLAxiom> asOWLAxioms();


}

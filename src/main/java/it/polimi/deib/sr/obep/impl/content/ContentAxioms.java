package it.polimi.deib.sr.obep.impl.content;

import it.polimi.deib.sr.obep.core.data.events.Content;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class ContentAxioms implements Content {

    private Set<OWLAxiom> axioms;

    public ContentAxioms(Set<OWLAxiom> axioms) {
        this.axioms = axioms;
    }

    @Override
    public OWLOntology asOWLOntology() {
        try {
            return OWLManager.createOWLOntologyManager().createOntology(axioms.stream());
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    @Override
    public Set<OWLAxiom> asOWLAxioms() {
        return axioms;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContentAxioms content = (ContentAxioms) o;
        Optional<Boolean> reduce = axioms.stream()
                .map(owlAxiom -> !content.axioms.contains(owlAxiom)).reduce((aBoolean, aBoolean2) -> aBoolean && aBoolean2);
        return reduce.orElse(Boolean.TRUE);

    }

    @Override
    public int hashCode() {
        return Objects.hash(axioms);
    }

    @Override
    public String toString() {
        return axioms.stream().map(OWLAxiom::toString).reduce("\n", String::concat);
    }
}

package sr.obep.impl.content;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import sr.obep.core.data.events.Content;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class ContentOntology implements Content {

    OWLOntology ontology;
    Model model;

    public ContentOntology(OWLOntology ontology) {
        this.ontology = ontology;
      //  this.model = convert(ontology.getOWLOntologyManager(), ontology.axioms().collect(Collectors.toSet()));
    }

    public ContentOntology(Set<OWLAxiom> axioms) {
        content(axioms);
    }

    @Override
    public OWLOntology asOWLOntology() {
        try {
            return OWLManager.createOWLOntologyManager().createOntology(ontology.aboxAxioms(Imports.EXCLUDED));
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    @Override
    public Set<OWLAxiom> asOWLAxioms() {
        return ontology.axioms().collect(Collectors.toSet());
    }

    public void content(Set<OWLAxiom> axioms) {
        try {
            OWLOntology ontology = OWLManager.createOWLOntologyManager().createOntology();
            axioms.forEach(ontology::add);
            content(ontology);
            this.model = convert(this.ontology.getOWLOntologyManager(), axioms);
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }
    }

    public void content(OWLOntology ontology) {
        this.ontology = ontology;
    }

    protected OntModel convert(OWLOntologyManager manager, Set<OWLAxiom> axioms) {
        //TODO refactor
        OntModel noReasoningModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        try {
            OWLOntology ontology = manager.createOntology();

            ontology.add(axioms.toArray(new OWLAxiom[axioms.size()]));
            noReasoningModel.getDocumentManager().setProcessImports(false);
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            try {
                manager.saveOntology(ontology, out);
            } catch (OWLOntologyStorageException e) {
            }

            try {
                noReasoningModel.read(new ByteArrayInputStream(out.toByteArray()), "RDF/XML");
            } catch (Exception e) {
            }


        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }
        return noReasoningModel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContentOntology content = (ContentOntology) o;
        return ontology.compareTo(content.ontology) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ontology, model);
    }

    @Override
    public String toString() {
        return ontology.axioms().map(a -> a.toString() + "\n").reduce(String::concat).orElse("");
    }
}

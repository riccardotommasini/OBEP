package sr.obep.pipeline.normalization;

import lombok.RequiredArgsConstructor;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import sr.obep.data.events.Content;
import sr.obep.pipeline.processors.EPLFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.*;

@RequiredArgsConstructor
public class SPARQLNormalForm implements NormalForm {

    private final Query query;
    private final OWLClass type;

    public SPARQLNormalForm(String q, OWLClass c) {
        this.query = QueryFactory.create(q);
        this.type = c;
    }

    @Override
    public String toString() {
        String name = type.getIRI().getShortForm();
        return EPLFactory.toEPLSchema(name, query.getProjectVars(), "_" + name);
    }

    @Override
    public OWLClass event() {
        return type;
    }

    @Override
    public List<Map<String, Object>> apply(Content c) {
        try {
            return exec(convert(c.asOWLOntology()), query);


        } catch (OWLOntologyStorageException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    private static List<Map<String, Object>> exec(Model model, Query sparql) {
        List<Map<String, Object>> results = new ArrayList<>();
        QueryExecution qExec = QueryExecutionFactory.create(sparql, model);
        ResultSet result = qExec.execSelect();

        while (result != null && result.hasNext()) {
            Map<String, Object> tempMap = new HashMap<>();

            QuerySolution solution = result.next();
            Iterator<String> it = solution.varNames();

            // Iterate over all results
            while (it.hasNext()) {
                String varName = it.next();
                RDFNode rdfNode = solution.get(varName);
                Object value = rdfNode.isLiteral() ? rdfNode.asLiteral().getValue() : rdfNode.asResource().toString();
                tempMap.put(varName, value);
            }
            // Only add if we have some objects in temp map
            if (tempMap.size() > 0) {
                results.add(tempMap);
            }
        }

        return results;
    }

    protected OntModel convert(OWLOntology ontology) throws OWLOntologyStorageException {
        //TODO refactor
        OntModel noReasoningModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        noReasoningModel.getDocumentManager().setProcessImports(false);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ontology.getOWLOntologyManager().saveOntology(ontology, out);
        noReasoningModel.read(new ByteArrayInputStream(out.toByteArray()), "RDF/XML");
        return noReasoningModel;
    }


}

package sr.obep.pipeline.normalization;

import lombok.RequiredArgsConstructor;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.impl.InfModelImpl;
import org.apache.jena.reasoner.InfGraph;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;
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

    private final String context;
    private final Query query;
    private final OWLClass type;

    private OWLOntology tbox;
    private OntModel tboxrdf;
    Reasoner reasoner = ReasonerRegistry.getOWLMiniReasoner();

    public SPARQLNormalForm(String ctx, String q, OWLClass c) {
        this.query = QueryFactory.create(q);
        this.type = c;
        this.context = ctx;
    }

    @Override
    public String toString() {
        return toEPL(this.context);
    }


    public String toEPL(String ctx) {
        return EPLFactory.toEPLSchema(ctx + "_" + type.getIRI().getShortForm(), query.getProjectVars(), type.getIRI().getShortForm());
    }


    @Override
    public OWLClass event() {
        return type;
    }

    @Override
    public List<Map<String, Object>> apply(Content abox) {
        reasoner = reasoner.bindSchema(tboxrdf);
        try {
            OntModel converted_abox = convert(abox.asOWLOntology());
            InfGraph bind = reasoner.bind(converted_abox.getGraph());
            List<Map<String, Object>> exec = exec(new InfModelImpl(bind), query);
            return exec;

        } catch (OWLOntologyStorageException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    @Override
    public void tbox(OWLOntology tbox) {
        this.tbox = tbox;
        try {
            this.tboxrdf = convert(tbox);
        } catch (OWLOntologyStorageException e) {
            e.printStackTrace();
        }
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

package sr.obep.extraction;

import lombok.extern.slf4j.Slf4j;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import sr.obep.data.events.SemanticEvent;
import sr.obep.processors.EventProcessor;

import java.util.*;
import java.util.Map.Entry;

/**
 * Created by pbonte on 03/11/2016.
 */
@Slf4j
public class ExtractorImpl implements Extractor {

    private OWLOntologyManager manager;
    private List<Query> queries;
    private EventProcessor next;

    public ExtractorImpl() {
        queries = new ArrayList<>();
        manager = OWLManager.createOWLOntologyManager();
    }

    @Override
    public SemanticEvent extract(SemanticEvent se) {
        Map<String, String> props = new HashMap<>();
        for (Query q : queries) {
            List<Map<String, String>> results = exec(se.getData(), q);
            for (Map<String, String> resultItem : results) {
                for (Entry<String, String> entry : resultItem.entrySet()) {
                    props.put(entry.getKey(), entry.getValue());
                }
            }
        }
        se.setProperties(props);
        return se;
    }

    private static List<Map<String, String>> exec(Model model, Query sparql) {
        List<Map<String, String>> results = new ArrayList<>();
        QueryExecution qExec = QueryExecutionFactory.create(sparql, model);
        ResultSet result = qExec.execSelect();

        while (result != null && result.hasNext()) {
            Map<String, String> tempMap = new HashMap<>();

            QuerySolution solution = result.next();
            Iterator<String> it = solution.varNames();

            // Iterate over all results
            while (it.hasNext()) {
                String varName = it.next();
                String varValue = solution.get(varName).toString();

                tempMap.put(varName, varValue);

            }
            // Only add if we have some objects in temp map
            if (tempMap.size() > 0) {
                results.add(tempMap);
            }
        }

        return results;
    }

    @Override
    public void send(SemanticEvent e) {
        next.send(extract(e));
    }

    @Override
    public EventProcessor pipe(EventProcessor p) {
        return this;
    }
}

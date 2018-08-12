package sr.obep.normalization;

import lombok.extern.slf4j.Slf4j;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.semanticweb.owlapi.model.OWLClass;
import sr.obep.data.events.SemanticEvent;
import sr.obep.processors.EventProcessor;

import java.util.*;
import java.util.Map.Entry;

/**
 * Created by pbonte on 03/11/2016.
 */
@Slf4j
public class SPARQLNormalizer implements Normalizer {

    private EventProcessor next;
    private final Map<OWLClass, Query> queries;
    private final String extractor_time = "timestamp.normalizer";

    public SPARQLNormalizer(Map<OWLClass, Query> queries) {
        this.queries = queries;
    }

    public SPARQLNormalizer() {
        this(new HashMap<>());
    }

    @Override
    public SemanticEvent normalize(SemanticEvent se) {
        List<Map<String, String>> results = exec(se.getContent().asRDFModel(), queries.get(se.getType()));
        for (Map<String, String> resultItem : results) {
            for (Entry<String, String> entry : resultItem.entrySet()) {
                //adding the property to the event using the variable name as key
                se.put(entry.getKey(), entry.getValue());

            }
        }
        se.put(extractor_time, System.currentTimeMillis());
        return se;
    }

    @Override
    public void addNormalizationQuery(OWLClass c, Query q) {
        queries.put(c, q);
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
        next.send(normalize(e));
    }

    @Override
    public EventProcessor pipe(EventProcessor p) {
        return next = p;
    }
}

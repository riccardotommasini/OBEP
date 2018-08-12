package sr.obep.normalization;

import lombok.extern.slf4j.Slf4j;
import org.semanticweb.owlapi.model.OWLClass;
import sr.obep.data.events.Content;
import sr.obep.data.events.SemanticEvent;
import sr.obep.processors.EventProcessor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by pbonte on 03/11/2016.
 */
@Slf4j
public class SPARQLNormalizer implements Normalizer {

    private EventProcessor next;
    private final Map<OWLClass, NormalForm> nfs;
    private final String extractor_time = "timestamp.normalizer";

    public SPARQLNormalizer(Map<OWLClass, NormalForm> queries) {
        this.nfs = queries;
    }

    public SPARQLNormalizer() {
        this(new HashMap<>());
    }

    @Override
    public List<Map<String, Object>> normalize(OWLClass type, Content content) {
        return nfs.get(type).apply(content);
    }

    @Override
    public void addNormalizationQuery(OWLClass c, NormalForm q) {
        nfs.put(c, q);
    }


    @Override
    public void send(SemanticEvent e) {
        //The multeplicity can grow here as well. It is an invariant only under the
        // condition that i apply extraction before normalization
        normalize(e.getType(), e.getContent()).forEach(resultItem -> {
            SemanticEvent copy = e.copy();
            copy.put(extractor_time, System.currentTimeMillis());
            resultItem.forEach(copy::put);
            next.send(copy);
        });
    }

    @Override
    public EventProcessor pipe(EventProcessor p) {
        return next = p;
    }
}

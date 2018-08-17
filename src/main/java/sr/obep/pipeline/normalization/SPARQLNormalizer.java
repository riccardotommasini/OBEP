package sr.obep.pipeline.normalization;

import lombok.extern.slf4j.Slf4j;
import org.semanticweb.owlapi.model.OWLClass;
import sr.obep.data.events.Content;
import sr.obep.data.events.RawEvent;
import sr.obep.pipeline.processors.EventProcessor;

import java.util.Collections;
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
    private final String extractor_time = "timestamp_normalizer";

    public SPARQLNormalizer(Map<OWLClass, NormalForm> queries) {
        this.nfs = queries;
    }

    public SPARQLNormalizer() {
        this(new HashMap<>());
    }

    @Override
    public List<Map<String, Object>> normalize(OWLClass type, Content content) {
        if (nfs.containsKey(type))
            return nfs.get(type).apply(content);
        return Collections.emptyList();
    }

    @Override
    public void addNormalForm(NormalForm q) {
        nfs.put(q.event(), q);
    }


    @Override
    public void send(RawEvent e) {
        //The multeplicity can grow here as well. It is an invariant only under the
        // condition that i apply extraction before normalization
        normalize(e.getType(), e.getContent()).forEach(resultItem -> {
            RawEvent copy = e.copy();
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

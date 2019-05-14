package it.polimi.deib.sr.obep.impl.pipeline;

import it.polimi.deib.sr.obep.core.data.events.Content;
import it.polimi.deib.sr.obep.core.pipeline.normalization.NormalForm;
import it.polimi.deib.sr.obep.core.pipeline.normalization.Normalizer;
import it.polimi.deib.sr.obep.core.pipeline.processors.EventProcessor;
import it.polimi.deib.sr.obep.impl.RawEvent;
import lombok.extern.slf4j.Slf4j;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;

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
    private final OWLOntology tbox;
    private final String context;


    public SPARQLNormalizer(OWLOntology ebox, Map<OWLClass, NormalForm> active_normal_forms, String context) {
        this.nfs = active_normal_forms;
        this.tbox = ebox;
        this.context = context;
        this.nfs.values().forEach(v -> v.tbox(ebox));
    }

    @Override
    public List<Map<String, Object>> normalize(OWLClass type, Content content) {
        if (nfs.containsKey(type))
            return nfs.get(type).apply(content);
        return null;
    }

    @Override
    public void addNormalForm(NormalForm q) {
        nfs.put(q.event(), q);
    }


    @Override
    public void send(RawEvent e) {
        //The multeplicity can grow here as well. It is an invariant only under the
        // condition that i apply extraction before normalization
        List<Map<String, Object>> normalize = normalize(e.getType(), e.getContent());
        if (normalize != null)
            normalize.forEach(resultItem -> {
                RawEvent copy = e.copy();
                copy.put(extractor_time, System.currentTimeMillis());
                resultItem.forEach(copy::put);
                copy.setContext(context);
                next.send(copy);
            });
        else {
            RawEvent copy = e.copy();
            copy.put(extractor_time, System.currentTimeMillis());
            copy.setContext(context);
            next.send(copy);
        }
    }

    @Override
    public EventProcessor pipe(EventProcessor p) {
        return next = p;
    }
}

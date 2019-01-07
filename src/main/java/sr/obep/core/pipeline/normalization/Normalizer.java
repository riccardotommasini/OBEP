package sr.obep.core.pipeline.normalization;

import org.semanticweb.owlapi.model.OWLClass;
import sr.obep.core.data.events.Content;
import sr.obep.core.pipeline.processors.EventProcessor;

import java.util.List;
import java.util.Map;

/**
 * Created by pbonte on 03/11/2016.
 */
public interface Normalizer extends EventProcessor {

    List<Map<String, Object>> normalize(OWLClass type, Content content);

    void addNormalForm(NormalForm q);
}

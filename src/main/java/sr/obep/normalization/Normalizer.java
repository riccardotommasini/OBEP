package sr.obep.normalization;

import org.apache.jena.query.Query;
import org.semanticweb.owlapi.model.OWLClass;
import sr.obep.data.events.SemanticEvent;
import sr.obep.processors.EventProcessor;

/**
 * Created by pbonte on 03/11/2016.
 */
public interface Normalizer extends EventProcessor {

    SemanticEvent normalize(SemanticEvent se);

    void addNormalizationQuery(OWLClass c, Query q);
}

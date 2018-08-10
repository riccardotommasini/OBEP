package sr.obep.extraction;

import sr.obep.data.events.SemanticEvent;
import sr.obep.processors.EventProcessor;

/**
 * Created by pbonte on 03/11/2016.
 */
public interface Extractor extends EventProcessor {

    SemanticEvent extract(SemanticEvent se);
}

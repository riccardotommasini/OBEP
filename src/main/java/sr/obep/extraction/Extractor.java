package sr.obep.interfaces;

import sr.obep.implementations.SemanticEvent;

/**
 * Created by pbonte on 03/11/2016.
 */
public interface Extractor extends EventProcessor {

    SemanticEvent extract(SemanticEvent se);
}

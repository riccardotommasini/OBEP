package sr.obep.interfaces;

import sr.obep.implementations.SemanticEvent;

/**
 * Created by Riccardo on 03/11/2016.
 */

public interface EventProcessor {

    void send(SemanticEvent e);

    EventProcessor pipe(EventProcessor p);

}

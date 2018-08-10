package sr.obep.processors;


import sr.obep.data.events.SemanticEvent;

/**
 * Created by Riccardo on 03/11/2016.
 */

public interface EventProcessor {

    void send(SemanticEvent e);

    EventProcessor pipe(EventProcessor p);

}

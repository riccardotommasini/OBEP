package it.polimi.deib.sr.obep.core.pipeline.processors;


import it.polimi.deib.sr.obep.impl.RawEvent;

/**
 * Created by Riccardo on 03/11/2016.
 */

public interface EventProcessor {

    void send(RawEvent e);

    EventProcessor pipe(EventProcessor p);
}

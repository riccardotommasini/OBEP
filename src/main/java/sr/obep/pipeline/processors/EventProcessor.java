package sr.obep.pipeline.processors;


import sr.obep.data.events.RawEvent;

/**
 * Created by Riccardo on 03/11/2016.
 */

public interface EventProcessor {

    void send(RawEvent e);

    EventProcessor pipe(EventProcessor p);
}

package it.polimi.deib.sr.obep.impl.pipeline;

import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.StatementAwareUpdateListener;
import com.espertech.esper.event.map.MapEventBean;
import it.polimi.deib.sr.obep.impl.RawEvent;
import lombok.extern.log4j.Log4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

/**
 * Created by Riccardo on 03/11/2016.
 */
@Log4j
public class EventListenerImpl implements StatementAwareUpdateListener {

    @Override
    public void update(EventBean[] newEvents, EventBean[] oldEvents, EPStatement statement, EPServiceProvider epServiceProvider) {
        // EventBean event = newEvents[0];
        log.info("EventListenerImpl update");
        List<RawEvent> activatedEvents = new ArrayList<>();
        if (newEvents != null) {
            log.info("New events: (" + this + ") " + newEvents.length);
            for (EventBean e : newEvents) {
                if (e instanceof MapEventBean) {
                    MapEventBean meb = (MapEventBean) e;
                    for (Entry<String, Object> entry : meb.getProperties().entrySet()) {
                        if (entry.getValue() instanceof MapEventBean) {
                            MapEventBean mapEvent = (MapEventBean) entry.getValue();
                            if (mapEvent.getProperties().containsKey("content")) {
                                RawEvent message = (RawEvent) mapEvent.getProperties().get("content");
                                activatedEvents.add(message);

                            }
                        }
                    }
                    log.info("" + meb.getProperties());

                }
            }
        }
        if (oldEvents != null) {
            log.info("Old events:" + oldEvents.length);

        }
        for (RawEvent message : activatedEvents) {
            //service.receive(message);
        }
    }

}

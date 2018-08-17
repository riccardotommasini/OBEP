package sr.obep.pipeline.processors;

import com.espertech.esper.client.*;
import lombok.extern.log4j.Log4j;
import sr.obep.data.events.RawEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Riccardo on 03/11/2016.
 */
@Log4j
public class CEP implements EventProcessor, EventStreamManager {

    private EPServiceProvider epService;
    private EPAdministrator cepAdm;
    private final EPRuntime cep;

    public CEP() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("packedId", "string");

        Class<RawEvent> value = RawEvent.class;
        properties.put("content", value);
        properties.put("ts", "long");

        Configuration configuration = new Configuration();
        configuration.addEventType("TEvent", properties);
        configuration.getEngineDefaults().getThreading().setInternalTimerEnabled(false);
        configuration.getEngineDefaults().getLogging().setEnableExecutionDebug(true);
        configuration.getEngineDefaults().getLogging().setEnableTimerDebug(true);

        this.epService = EPServiceProviderManager.getDefaultProvider(configuration);
        this.cep = this.epService.getEPRuntime();
        this.cepAdm = this.epService.getEPAdministrator();

    }

    /**
     * Strings the prefixes from the filter e.g. test.prefix/test.owl#Filter
     * becomes Filter.
     *
     * @param longName The name of the filter containing the prefixes
     * @return
     */
    private String stripFilterName(String longName) {
        if (longName.contains("#")) {
            return longName.substring(longName.lastIndexOf('#') + 1);

        } else {
            return longName.substring(longName.lastIndexOf('/') + 1);
        }
    }

    @Override
    public void send(RawEvent se) {
        //TODO refactor the event definition
        System.out.println("Sending an ["+stripFilterName(se.getType().toStringID())+"]");
        this.cep.sendEvent(se, stripFilterName(se.getType().toStringID()));
    }

    @Override
    public EventProcessor pipe(EventProcessor p) {
        //TODO il piping avviene tramite Listener
        return this;
    }


    @Override
    public EPStatement register_event_schema(String event) {
        return this.cepAdm.createEPL(event);
    }

    @Override
    public EPStatement register_event_pattern_stream(String pattern) {
        return this.cepAdm.createEPL(pattern);
    }
}

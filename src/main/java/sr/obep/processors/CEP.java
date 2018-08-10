package sr.obep.processors;

import com.espertech.esper.client.*;
import lombok.extern.log4j.Log4j;
import org.apache.jena.query.Query;
import sr.obep.data.SemanticEvent;
import sr.obep.exceptions.EventStreamManager;
import sr.obep.programming.parser.delp.EventDecl;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Riccardo on 03/11/2016.
 */
@Log4j
public class CEP implements EventProcessor, EventStreamManager {

    private EPServiceProvider epService;
    private Map<EventDecl, Query> filterQueries;
    private EPAdministrator cepAdm;
    private final EPRuntime cep;
    private final EPAdministrator adm;

    public CEP() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("packedId", "string");

        Class<SemanticEvent> value = SemanticEvent.class;
        properties.put("content", value);
        properties.put("ts", "long");

        Configuration configuration = new Configuration();
        configuration.addEventType("TEvent", properties);
        configuration.getEngineDefaults().getThreading().setInternalTimerEnabled(false);
        configuration.getEngineDefaults().getLogging().setEnableExecutionDebug(true);
        configuration.getEngineDefaults().getLogging().setEnableTimerDebug(true);

        this.epService = EPServiceProviderManager.getDefaultProvider(configuration);
        this.cep = this.epService.getEPRuntime();
        this.adm = this.epService.getEPAdministrator();

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
    public void send(SemanticEvent se) {
        //TODO refactor the event definition
        se.getTriggeredFilterIRIs().forEach(trigger -> {
            String eventType = stripFilterName(trigger);
            Map<String, Object> map = new HashMap<>();
            map.put("packedId", se.getPacketID());
            map.put("ts", System.currentTimeMillis());
            map.put("content", se);
            map.putAll(se.getProperties());
            this.cep.sendEvent(map, eventType);
        });
    }

    @Override
    public EventProcessor pipe(EventProcessor p) {
        return this;
    }


    @Override
    public void register_event_schema(String event) {
        this.cepAdm.createEPL(event);
    }

    @Override
    public void register_event_stream(String event) {
        this.cepAdm.createEPL(event);
    }
}

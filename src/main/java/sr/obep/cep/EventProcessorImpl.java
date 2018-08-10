package sr.obep.cep;

import com.espertech.esper.client.*;
import lombok.extern.log4j.Log4j;
import org.apache.jena.query.Query;
import sr.obep.data.SemanticEvent;
import sr.obep.programming.parser.delp.EventDecl;
import sr.obep.programming.Program;

import java.util.*;

/**
 * Created by Riccardo on 03/11/2016.
 */
@Log4j
public class EventProcessorImpl implements EventProcessor {

    private EPServiceProvider epService;
    private Map<EventDecl, Query> filterQueries;
    private EPAdministrator cepAdm;

    public void sendEvent(SemanticEvent se) {
        Set<String> triggeredFilters = se.getTriggeredFilterIRIs();

        for (String trigger : triggeredFilters) {
            String eventType = stripFilterName(trigger);

            Map<String, Object> eventMap = new HashMap<String, Object>();
            eventMap.put("packedId", se.getPacketID());
            eventMap.put("ts", System.currentTimeMillis());
            eventMap.put("content", se);
            eventMap.putAll(se.getProperties());

            log.info("Adding Event (" + this + ") " + eventMap);
            epService.getEPRuntime().sendEvent(eventMap, eventType);
        }
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

    public void init(OBEPEngine obep) {
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
        this.cepAdm = epService.getEPAdministrator();

        // disable internal clock

    }


    public void registerQuery(Program query) {
        this.filterQueries = new HashMap<>();



    }


}

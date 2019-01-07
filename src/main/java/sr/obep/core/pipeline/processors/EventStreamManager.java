package sr.obep.core.pipeline.processors;

import com.espertech.esper.client.EPStatement;

/**
 * Created by Riccardo on 03/11/2016.
 */

public interface EventStreamManager {

    EPStatement register_event_schema(String event);

    EPStatement register_event_pattern_stream(String event);
}

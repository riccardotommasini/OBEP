package sr.obep.processors;

/**
 * Created by Riccardo on 03/11/2016.
 */

public interface EventStreamManager {

    void register_event_schema(String event);

    void register_event_stream(String event);
}

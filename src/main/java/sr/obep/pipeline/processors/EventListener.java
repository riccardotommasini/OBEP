package sr.obep.pipeline.processors;


/**
 * Created by Riccardo on 03/11/2016.
 */

public interface EventListener {

    EventListener pipe(EventListener p);
}

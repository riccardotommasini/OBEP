package sr.obep.abstration;

import sr.obep.data.events.SemanticEvent;
import sr.obep.processors.EventProcessor;

import java.util.List;

/**
 * Created by Riccardo on 03/11/2016.
 */

public interface Abstracter extends EventProcessor {

    List<SemanticEvent> lift(SemanticEvent abox);

}

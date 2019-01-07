package it.polimi.deib.sr.obep.core.data.events;

import it.polimi.deib.sr.obep.core.pipeline.normalization.NormalForm;

import java.util.Map;

public interface CompositeEvent extends ComplexEvent {

    String[] named();

    Map<String, String> body_schemas();

    Map<String, NormalForm> normal_forms();

    String alias(String name);
}

package it.polimi.deib.sr.obep.core;


import it.polimi.deib.sr.obep.core.data.streams.EventStream;
import it.polimi.deib.sr.obep.core.programming.Program;
import it.polimi.deib.sr.obep.core.programming.ProgramExecution;

/**
 * Created by Riccardo on 03/11/2016.
 */
public interface OBEPEngine {

    EventStream register(String uri);

    ProgramExecution register(Program q);

}

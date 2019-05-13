package it.polimi.deib.sr.obep.core;


import it.polimi.deib.sr.obep.core.programming.Program;
import it.polimi.deib.sr.obep.core.programming.ProgramExecution;

/**
 * Created by Riccardo on 03/11/2016.
 */
public interface OBEPEngine {

    ProgramExecution register(Program q);

    ProgramExecution register(String q);

}

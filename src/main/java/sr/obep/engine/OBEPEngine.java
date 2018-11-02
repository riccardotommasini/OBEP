package sr.obep.engine;


import sr.obep.pipeline.processors.CEP;
import sr.obep.programming.Program;

/**
 * Created by Riccardo on 03/11/2016.
 */
public interface OBEPEngine {

    CEP register(Program q);

    CEP register(String q);

}

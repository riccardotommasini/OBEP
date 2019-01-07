package sr.obep.core.engine;


import sr.obep.core.programming.Program;

/**
 * Created by Riccardo on 03/11/2016.
 */
public interface OBEPEngine {

    void register(Program q);

    void register(String q);

}

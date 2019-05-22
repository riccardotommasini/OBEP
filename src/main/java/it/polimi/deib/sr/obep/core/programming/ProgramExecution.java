package it.polimi.deib.sr.obep.core.programming;

import it.polimi.deib.sr.obep.core.data.streams.EventStream;

public interface ProgramExecution {

    void add(EventStream in);

    void remove(EventStream in);

    void add(ResultFormatter o);

    void remove(ResultFormatter o);


}

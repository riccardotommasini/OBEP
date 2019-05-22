package it.polimi.deib.sr.obep.core.programming;

import com.espertech.esper.client.EPStatement;
import it.polimi.deib.sr.obep.core.data.streams.EventStream;
import it.polimi.deib.sr.obep.core.pipeline.abstration.Abstracter;
import it.polimi.deib.sr.obep.core.pipeline.normalization.Normalizer;
import it.polimi.deib.sr.obep.impl.pipeline.CEP;

import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.Set;

public final class ProgramExecutionImpl extends Observable implements ProgramExecution {
    private final Abstracter abstracter;
    private final List<Normalizer> normalizers;
    private final EPStatement output;
    private final CEP cep;
    private final Set<EventStream> inputs = new HashSet<>();

    public ProgramExecutionImpl(CEP cep, Abstracter abstracter, List<Normalizer> normalizers, EPStatement out_pattern) {
        this.abstracter = abstracter;
        this.normalizers = normalizers;
        this.output = out_pattern;
        this.cep = cep;
    }

    @Override
    public void add(EventStream in) {
        inputs.add(in);
        in.connectTo(abstracter);
    }

    @Override
    public void remove(EventStream in) {
        //TODO
        inputs.remove(in);
    }

    @Override
    public void add(ResultFormatter o) {
        output.addListener(o);
    }

    @Override
    public void remove(ResultFormatter o) {
        output.removeListener(o);
    }
}

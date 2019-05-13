package it.polimi.deib.sr.obep.core.programming;

import com.espertech.esper.client.EPStatement;
import it.polimi.deib.sr.obep.core.pipeline.abstration.Abstracter;
import it.polimi.deib.sr.obep.core.pipeline.normalization.Normalizer;
import it.polimi.deib.sr.obep.impl.pipeline.CEP;

import java.util.List;
import java.util.Observable;

public final class ProgramExecutionImpl extends Observable implements ProgramExecution {
    private final Abstracter abstracter;
    private final List<Normalizer> normalizers;
    private final EPStatement output;

    public ProgramExecutionImpl(CEP cep, Abstracter abstracter, List<Normalizer> normalizers, EPStatement out_pattern) {
        this.abstracter = abstracter;
        this.normalizers = normalizers;
        this.output = out_pattern;
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

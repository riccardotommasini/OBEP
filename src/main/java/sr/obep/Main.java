package sr.obep;

import sr.obep.abstration.Abstracter;
import sr.obep.abstration.AbstracterImpl;
import sr.obep.data.streams.EventStream;
import sr.obep.engine.OBEPEngineFactory;
import sr.obep.explanation.ExplainerImpl;
import sr.obep.extraction.Extractor;
import sr.obep.extraction.ExtractorImpl;
import sr.obep.explanation.Explainer;
import sr.obep.engine.OBEPEngine;
import sr.obep.processors.CEP;

public class Main {

    public static void main(String[] args) {

        Abstracter abstracter = new AbstracterImpl(null);

        Explainer explainer = new ExplainerImpl();
        Extractor extractor = new ExtractorImpl();
        CEP runtime = new CEP();

        EventStream stream = processor -> null;

        //Connect API
        EventStream abstracted_stream = stream.connectTo(abstracter);

        abstracted_stream.connectTo(explainer);

        //OR
        //Pipe API

        abstracter.pipe(explainer).pipe(extractor).pipe(runtime);

        //OR Engine

        String program = null;

        OBEPEngine engine = OBEPEngineFactory.create();
        engine.register(program);

    }
}

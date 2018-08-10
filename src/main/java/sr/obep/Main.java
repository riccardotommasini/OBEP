package sr.obep.implementations;

import sr.obep.extraction.ExtractorImpl;
import sr.obep.interfaces.Abstracter;
import sr.obep.interfaces.Explainer;
import sr.obep.interfaces.Extractor;
import sr.obep.interfaces.declarations.EventStream;

public class Main {

    public static void main(String[] args) {

        Abstracter abstracter = new AbstracterImpl(null);

        Explainer explainer = new ExplainerImpl();
        Extractor extractor = new ExtractorImpl();
        OBEPRuntime runtime = new OBEPRuntime();

        EventStream stream = processor -> null;

        //Connect API
        EventStream abstracted_stream = stream.connectTo(abstracter);

        abstracted_stream.connectTo(explainer);

        //OR
        //Pipe API

        abstracter.pipe(explainer).pipe(extractor).pipe(runtime);

    }
}

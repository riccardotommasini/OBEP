package sr.obep;

import openllet.owlapi.OWL;
import org.apache.jena.query.QueryFactory;
import sr.obep.abstration.Abstracter;
import sr.obep.abstration.AbstracterImpl;
import sr.obep.data.streams.EventStream;
import sr.obep.engine.OBEPEngine;
import sr.obep.engine.OBEPEngineFactory;
import sr.obep.explanation.Explainer;
import sr.obep.explanation.ExplainerImpl;
import sr.obep.normalization.Normalizer;
import sr.obep.normalization.SPARQLNormalForm;
import sr.obep.normalization.SPARQLNormalizer;
import sr.obep.processors.CEP;

public class Main {

    public static void main(String[] args) {

        Abstracter abstracter = new AbstracterImpl(null);

        Explainer explainer = new ExplainerImpl();
        Normalizer normalizer = new SPARQLNormalizer(null);

        normalizer.addNormalizationQuery(OWL.Thing, new SPARQLNormalForm(QueryFactory.create("SELECT * WHERE {?s ?p ?o}")));

        CEP runtime = new CEP();

        EventStream stream = processor -> null;

        //Connect API
        EventStream abstracted_stream = stream.connectTo(abstracter);

        abstracted_stream.connectTo(explainer);

        //OR
        //Pipe API

        abstracter.pipe(explainer).pipe(normalizer).pipe(runtime);

        //OR Engine

        String program = null;

        OBEPEngine engine = OBEPEngineFactory.create();
        engine.register(program);

    }
}

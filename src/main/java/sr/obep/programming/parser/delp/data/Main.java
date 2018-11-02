package sr.obep.programming.parser.delp.data;

import openllet.owlapi.OWL;
import org.apache.jena.query.QueryFactory;
import org.semanticweb.owlapi.model.OWLClass;
import sr.obep.data.streams.EventStream;
import sr.obep.pipeline.abstration.Abstracter;
import sr.obep.pipeline.abstration.AbstracterImpl;
import sr.obep.pipeline.explanation.Explainer;
import sr.obep.pipeline.explanation.ExplainerImpl;
import sr.obep.pipeline.normalization.Normalizer;
import sr.obep.pipeline.normalization.SPARQLNormalForm;
import sr.obep.pipeline.normalization.SPARQLNormalizer;
import sr.obep.pipeline.processors.CEP;
import sr.obep.pipeline.processors.EventProcessor;

public class Main {

    public static void main(String[] args) {

        Abstracter abstracter = new AbstracterImpl(null);

        Explainer explainer = new ExplainerImpl(null);
        Normalizer normalizer = new SPARQLNormalizer(null, null, "");

        OWLClass thing = OWL.Thing;
        normalizer.addNormalForm(new SPARQLNormalForm("", QueryFactory.create("SELECT * WHERE {?s ?p ?o}"), thing));

        CEP runtime = new CEP();

        EventStream stream = new EventStream() {
            @Override
            public EventStream connectTo(EventProcessor processor) {
                return null;
            }

            @Override
            public String name() {
                return null;
            }
        };

        //Connect API
        EventStream abstracted_stream = stream.connectTo(abstracter);

        abstracted_stream.connectTo(explainer);

        //OR
        //Pipe API

        abstracter.pipe(explainer).pipe(normalizer).pipe(runtime);

        //OR Engine

        String program = null;


    }
}

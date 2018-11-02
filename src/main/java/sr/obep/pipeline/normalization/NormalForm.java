package sr.obep.pipeline.normalization;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import sr.obep.data.events.Content;

import java.util.List;
import java.util.Map;

public interface NormalForm {

    OWLClass event();

    List<Map<String, Object>> apply(Content c);

    void tbox(OWLOntology tbox);

}

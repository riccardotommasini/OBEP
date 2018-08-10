package sr.obep.data.events;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.jena.rdf.model.Model;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Riccardo on 03/11/2016.
 */
@Data
@RequiredArgsConstructor
public class SemanticEvent implements Serializable {

    private OWLClass type;
    @NonNull
    private Set<OWLAxiom> axioms;
    private Model data;
    @NonNull
    private OWLNamedIndividual message;
    private String packetID;
    private Set<String> triggeredFilterIRIs;
    @NonNull
    private long timeStamp;
    @NonNull
    private String stream;
    private Map<String, String> properties;

    public SemanticEvent(OWLNamedIndividual message, String packetID, long timeStamp, String stream) {
        this.axioms = new HashSet<>();
        this.message = message;
        this.packetID = packetID;
        this.timeStamp = timeStamp;
        this.stream = stream;
    }

    public void addAxiom(OWLAxiom ax) {
        this.axioms.add(ax);
    }

    public Set<String> getTriggeredFilters() {
        return null;
    }
}

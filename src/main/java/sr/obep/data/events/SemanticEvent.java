package sr.obep.data.events;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Objects;

/**
 * Created by Riccardo on 03/11/2016.
 */
@RequiredArgsConstructor
public class SemanticEvent extends HashMap<String, Object> implements Serializable {

    private static final String type = "event_type";
    private static final String content = "event_content";
    private static final String ingestion_time = "timestamp_sys";
    private static final String event_time = "timestamp_event";

    public SemanticEvent(String packetID) {
        put(ingestion_time, System.currentTimeMillis());
        this.packetID = packetID;
        this.eventInvididual = OWLManager.createOWLOntologyManager().getOWLDataFactory().getOWLNamedIndividual(IRI.create(this.getPacketID()));

    }

    @Getter
    private String packetID;

    @Getter
    private OWLNamedIndividual eventInvididual;

    @Setter
    @Getter
    @NonNull
    private long timeStamp;

    @Setter
    @Getter
    @NonNull
    private String stream_uri;

    public void setType(OWLClass c) {
        put(type, c);
    }

    public OWLClass getType() {
        return (OWLClass) get(type);
    }

    public Content getContent() {
        return (Content) get(content);
    }

    public void setContent(Content c) {
        put(content, c);
    }

    public SemanticEvent copy() {
        SemanticEvent copy = new SemanticEvent(packetID);
        this.keySet().forEach(k -> copy.put(k, get(k)));
        return copy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SemanticEvent that = (SemanticEvent) o;
        return Objects.equals(packetID, that.packetID) &&
                Objects.equals(stream_uri, that.stream_uri) &&
                Objects.equals(getContent(), that.getContent()) &&
                Objects.equals(getType(), that.getType());
    }

    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), packetID, timeStamp, stream_uri);
    }
}

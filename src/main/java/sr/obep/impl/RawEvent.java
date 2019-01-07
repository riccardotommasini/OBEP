package sr.obep.impl;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import sr.obep.core.data.events.Content;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Objects;

/**
 * Created by Riccardo on 03/11/2016.
 */
@RequiredArgsConstructor
public class RawEvent extends HashMap<String, Object> implements Serializable {

    private static final String type = "event_type";
    private static final String content = "event_content";
    private static final String ingestion_time = "timestamp_sys";
    private static final String event_time = "timestamp_event";
    private static final String context = "context";

    public RawEvent(String packetID) {
        put(ingestion_time, System.currentTimeMillis());
        this.packetID = packetID;
        this.eventInvididual = OWLManager.createOWLOntologyManager().getOWLDataFactory().getOWLNamedIndividual(IRI.create(this.getPacketID()));
    }

    @Getter
    private String packetID;

    @Getter
    @Setter
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

    public RawEvent copy() {
        RawEvent copy = new RawEvent(packetID);
        this.keySet().forEach(k -> copy.put(k, get(k)));
        return copy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        RawEvent that = (RawEvent) o;
        return Objects.equals(packetID, that.packetID) &&
                Objects.equals(stream_uri, that.stream_uri) &&
                Objects.equals(getContent(), that.getContent()) &&
                Objects.equals(getType(), that.getType());
    }

    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), packetID, timeStamp, stream_uri);
    }

    public void setContext(String ctx) {
        put(context, ctx);
    }

    public String getContext() {
        return (String) get(context);
    }
}

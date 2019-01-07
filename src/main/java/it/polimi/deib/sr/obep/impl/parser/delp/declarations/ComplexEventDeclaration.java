package it.polimi.deib.sr.obep.impl.parser.delp.declarations;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.jena.graph.Node;
import org.semanticweb.owlapi.model.OWLClass;

/**
 * Created by Riccardo on 23/08/16.
 */

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ComplexEventDeclaration {

    @Setter
    protected OWLClass head;

    @Setter

    protected String uri;

    public ComplexEventDeclaration(Node uri) {
        this.uri = uri.toString();
    }

    public String getName() {
        return head.getIRI().getShortForm();
    }

    public String getNamespace() {
        return head.getIRI().getNamespace();
    }
}
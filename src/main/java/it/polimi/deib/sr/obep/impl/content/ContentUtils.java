package it.polimi.deib.sr.obep.impl.content;

import it.polimi.deib.sr.obep.core.data.events.Content;
import org.semanticweb.owlapi.model.parameters.Imports;

import java.util.Arrays;
import java.util.stream.Collectors;

public class ContentUtils {

    public static Content merge2(Content... cs) {
        return new ContentAxioms(Arrays.stream(cs).flatMap(c -> c.asOWLOntology().aboxAxioms(Imports.EXCLUDED)).collect(Collectors.toSet()));
    }

}

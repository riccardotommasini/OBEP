package sr.obep.programming.parser.delp;

import lombok.Getter;
import org.apache.jena.graph.Node;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ResultClause {

    private boolean all = false;
    private boolean named = false;
    private boolean rdf = false;
    private boolean event = false;

    private List<String> events = new ArrayList<>();

    public ResultClause setAll() {
        all = true;
        return this;
    }

    public ResultClause setNamed() {
        named = true;
        return this;
    }

    public ResultClause setRDF() {
        rdf = true;
        return this;
    }

    public ResultClause setEvent() {
        event = true;
        return this;
    }

    public ResultClause addReturnEvent(Node pop) {
        events.add(pop.getURI());
        return this;
    }
}

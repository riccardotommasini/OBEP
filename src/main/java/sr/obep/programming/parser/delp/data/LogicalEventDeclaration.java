package sr.obep.programming.parser.delp.data;

import com.espertech.esper.client.soda.CreateSchemaClause;
import com.espertech.esper.client.soda.SchemaColumnDesc;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import sr.obep.data.events.LogicalEvent;

import java.io.StringWriter;
import java.util.*;

/**
 * Created by Riccardo on 16/08/16. This class represents a logic event declaration
 * using DL manchester syntax. - The head_node consists of the left part of the DL
 * rule. - The body, //TODO parse it, is the right part of the DL rule. - filter_events
 * is a SPARQL-Like constraint for the event instances.
 */
@EqualsAndHashCode
@ToString
@Data
public class LogicalEventDeclaration extends ComplexEventDeclaration implements LogicalEvent {

    private String dlbody;
    private int occurrence = -1;

    public LogicalEventDeclaration(Node head) {
        super(head);
    }

    public String toEPLSchema(Set<Var> vars) {
        CreateSchemaClause schema = new CreateSchemaClause();
        schema.setSchemaName(getHead_node().getURI().replace(getHead_node().getNameSpace(), "")); //TODO
        schema.setInherits(new HashSet<String>(Arrays.asList(new String[]{"TEvent"})));
        List<SchemaColumnDesc> columns = new ArrayList<SchemaColumnDesc>();
        for (Var var : vars) {
            SchemaColumnDesc scd = new SchemaColumnDesc();
            scd.setArray(false);
            scd.setType("String");
            scd.setName(var.getName());
            columns.add(scd);
        }

        schema.setColumns(columns);
        StringWriter writer = new StringWriter();
        schema.toEPL(writer);

        return writer.toString();
    }

    public String toEPLSchema() {
        return toEPLSchema(new HashSet<Var>());
    }


    @Override
    public String getHead() {
        return head_node.toString();
    }

    @Override
    public String getBody() {
        return dlbody;
    }
}

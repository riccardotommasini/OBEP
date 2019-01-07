package it.polimi.deib.sr.obep.impl.parser.delp.declarations;

import com.espertech.esper.client.soda.CreateSchemaClause;
import com.espertech.esper.client.soda.SchemaColumnDesc;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;

import java.io.StringWriter;
import java.util.*;

/**
 * Created by Riccardo on 16/08/16. This class represents a logic event declaration
 * using DL manchester syntax. - The head_node consists of the left part of the DL
 * rule. - The body, //TODO parse it, is the right part of the DL rule. - filter_events
 * is a SPARQL-Like constraint for the event instances.
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class LogicalEventDeclaration extends ComplexEventDeclaration {

    private String dlbody;
    private int occurrence = -1;

    public LogicalEventDeclaration(String s, Node head) {
        super(head);
        this.dlbody = s;
    }

    public String toEPLSchema(Set<Var> vars) {
        CreateSchemaClause schema = new CreateSchemaClause();
        schema.setSchemaName(getUri().replace(getNamespace(), "")); //TODO
        schema.setInherits(new HashSet<>(Arrays.asList(new String[]{"TEvent"})));
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

    @Override
    public String toString() {
        return ("Class: " + getHead() + " \n\t EquivalentTo: " + getDlbody().replace(":", "")).trim();
    }
}

package sr.obep.programming.parser.delp;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Node_URI;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.parboiled.Rule;
import sr.obep.programming.parser.delp.data.*;
import sr.obep.programming.parser.sparql.Prefix;
import sr.obep.programming.parser.sparql.SPARQLParser;

/**
 * Created by Riccardo on 09/08/16.
 */
public class OBEPParser extends SPARQLParser {

    @Override
    public Rule Query() {
        return Sequence(push(new OBEPParserOutput()), WS(), Prologue(),
                VocabClause(), DataClause(),
                OneOrMore(CreateEventClause()), OutputClause(), EOI);
    }

    public Rule OutputClause() {
        return Sequence(RETURN(), push(new ResultClause()),
                FirstOf(
                        Sequence(ALL(), push(((ResultClause) pop()).setAll())),
                        Sequence(NAMED(), push(((ResultClause) pop()).setNamed())),
                        SelectedStreams()),
                AS(),
                FirstOf(Sequence(RDF(), push(((ResultClause) pop()).setRDF())),
                        Sequence(EVENT(), push(((ResultClause) pop()).setEvent()))),
                STREAM(),
                push(((OBEPParserOutput) pop(1)).addResultClause((ResultClause) pop())));
    }

    public Rule SelectedStreams() {
        return OneOrMore(IriRef(), push(((ResultClause) pop(1)).addReturnEvent((Node)pop())));
    }

    public Rule DataClause() {
        return FirstOf(Sequence(OneOrMore(DataStreamClause()), ZeroOrMore(DatasetClause())), Sequence(ZeroOrMore(DatasetClause()), OneOrMore(DataStreamClause())));
    }

    public Rule VocabClause() {
        return Sequence(FirstOf(VOCAB(), TBOX()), SourceSelector(), pushQuery(((OBEPParserOutput) pop(1)).addVocabURI((Node_URI) pop())));
    }

    public Rule DatasetClause() {
        return Sequence(FROM(), GRAPH(), DefaultGraphClause());
    }

    public Rule DataStreamClause() {
        return Sequence(FROM(), STREAM(), DefaultStreamClause());
    }

    public Rule DefaultStreamClause() {
        return Sequence(SourceSelector(), pushQuery(((OBEPParserOutput) pop(1)).addStreamURI((Node_URI) pop())));
    }

    public Rule DefaultGraphClause() {
        return Sequence(SourceSelector(), pushQuery(((OBEPParserOutput) pop(1)).addStaticGraphURI((Node_URI) pop())));
    }

    public Rule NamedGraphClause() {
        return Sequence(NAMED(), SourceSelector(), pushQuery(((OBEPParserOutput) pop(1)).addNamedGraphURI((Node_URI) pop())));
    }

    public Rule Prologue() {
        return Sequence(Optional(BaseDecl()), ZeroOrMore(PrefixDecl()));
    }

    public Rule BaseDecl() {
        return Sequence(BASE(), IRI_REF(),
                pushQuery(((OBEPParserOutput) pop(0)).setQBaseURI(trimMatch().replace(">", "").replace("<", ""))), WS());
    }

    public Rule PrefixDecl() {
        return Sequence(PrefixBuild(), pushQuery(((OBEPParserOutput) pop(1)).setPrefix((Prefix) pop())), WS());
    }

    public Rule PrefixBuild() {
        return Sequence(PREFIX(), PNAME_NS(), push(new Prefix(trimMatch())), IRI_REF(),
                push(((Prefix) pop()).setURI(URIMatch())), WS());
    }

    public Rule CreateEventClause() {
        return Sequence(Optional(NAMED()), EVENT(), IriRef(),
                FirstOf(
                        Sequence(OPEN_CURLY_BRACE(), push(new CompositeEventDeclaration((Node) pop())), EventCalculusDeclaration(), CLOSE_CURLY_BRACE()),
                        Sequence(AS(), DLEventDeclaration(), push(new LogicalEventDeclaration(match(), (Node) pop()))))
                , pushQuery(((OBEPParserOutput) popQuery(-1)).addEventDecl((ComplexEventDeclaration) pop())), Optional(DOT()));
    }

    public Rule DLEventDeclaration() {
        //TODO
        return ZeroOrMore(Sequence(TestNot(FirstOf(EVENT(), NAMED())), ANY), WS());
    }

    public Rule EventCalculusDeclaration() {
        return Sequence(MatchClause(), Optional(IfClause()));
    }

    public Rule EventConstructClause() {
        //TODO
        return FirstOf(Sequence(TriplesTemplate(), addTemplateAndPatternToQuery()),
                Sequence(OPEN_CURLY_BRACE(), ConstructTemplate(), addTemplateToQuery(), CLOSE_CURLY_BRACE()));
    }

    public Rule MatchClause() {
        return Sequence(MATCH(), PatternExpression(), addPatternExpression());
    }

    public Rule IfClause() {
        return FirstOf(
                Sequence(IF(), EventFilterDecl()),
                Sequence(IF(), OPEN_CURLY_BRACE(), ZeroOrMore(EventFilterDecl()), CLOSE_CURLY_BRACE()));
    }

    public Rule EventFilterDecl() {
        return FirstOf(Sequence(Filter(), addFilter((ElementFilter) pop()), WS()),
                Sequence(EVENT(), VarOrIRIref(), OPEN_CURLY_BRACE(), EventClause(), addEventFilter((NormalFormDeclaration) pop(), (Node) pop()), CLOSE_CURLY_BRACE(), WS())
        );
    }


    public Rule EventClause() {
        return Sequence(TriplesBlock(), push(new NormalFormDeclaration(popElement())));
    }


    public Rule PatternExpression() {
        return Sequence(FollowedByExpression(), Optional(Sequence(WITHIN(), LPAR(), TimeConstrain(),
                push(new PatternDeclaration(match(), (PatternDeclaration) pop())), RPAR())));
    }

    public Rule FollowedByExpression() {
        return Sequence(OrExpression(), ZeroOrMore(FirstOf(FOLLOWED_BY(), Sequence(NOT(), FOLLOWED_BY())),
                enclose(trimMatch()), OrExpression(), addExpression()));
    }

    public Rule OrExpression() {
        return Sequence(AndExpression(), ZeroOrMore(OR_(), enclose(trimMatch()), AndExpression(), addExpression()));
    }

    public Rule AndExpression() {
        return Sequence(QualifyExpression(),
                ZeroOrMore(AND_(), enclose(trimMatch()), QualifyExpression(), addExpression()));
    }

    public Rule QualifyExpression() {
        return FirstOf(Sequence(FirstOf(EVERY(), NOT()), push(new PatternDeclaration(trimMatch())), GuardPostFix(),
                addExpression()), GuardPostFix());
    }

    public Rule GuardPostFix() {
        return FirstOf(
                Sequence(LPAR(), PatternExpression(), RPAR(), push(new PatternDeclaration((PatternDeclaration) pop()))),
                Sequence(VarOrIRIref(), push(((OBEPParserOutput) getQuery(-1)).getEventDecl((Node) peek())),
                        push(new PatternDeclaration((ComplexEventDeclaration) pop(), (Node) pop()))));

    }

    //Utility methods

    @Override
    public boolean startSubQuery(int i) {
        return push(new OBEPParserOutput(getQuery(i).getQ().getPrologue()));
    }

    // MQL
    public boolean addEventFilter(NormalFormDeclaration pop, Node node) {
        CompositeEventDeclaration peek = (CompositeEventDeclaration) peek();
        pop.setVar(node);
        pop.setContext(peek.getUri());
        peek.addEventFilter(pop);

        return true;
    }

    public boolean addFilter(ElementFilter pop) {
        CompositeEventDeclaration peek = (CompositeEventDeclaration) peek();
        peek.addFilter(pop);
        return true;
    }

    public boolean addPatternExpression() {
        ((CompositeEventDeclaration) peek(1)).setExpr((PatternDeclaration) pop());
        return true;
    }

    public boolean setDLRule() {
        ((LogicalEventDeclaration) peek()).setDlbody(match());
        return true;
    }

    public boolean addExpression() {
        PatternDeclaration inner = (PatternDeclaration) pop();
        PatternDeclaration outer = (PatternDeclaration) pop();
        outer.addPattern(inner);
        return push(outer);
    }

    public boolean enclose(String operator) {
        PatternDeclaration inner = (PatternDeclaration) pop();

        if (inner.isBracketed() || inner.getOperator() == null || !operator.equals(inner.getOperator())) {
            PatternDeclaration outer = new PatternDeclaration(operator);
            outer.setOperator(operator);
            outer.addPattern(inner);
            return push(outer);
        }
        return push(inner);

    }

    //OBEP Syntax Extensions

    //INPUT

    public Rule STREAM() {
        return StringIgnoreCaseWS("STREAM");
    }

    public Rule RDF() {
        return StringIgnoreCaseWS("RDF");
    }

    //OUTPUT

    public Rule RETURN() {
        return StringIgnoreCaseWS("RETURN");
    }


    // DSMS
    public Rule EVENT() {
        return StringIgnoreCaseWS("EVENT");
    }

    public Rule AGGREGATE() {
        return StringIgnoreCaseWS("AGGREGATE");
    }

    public Rule PARTITION() {
        return StringIgnoreCaseWS("PARTITION");
    }

    public Rule WINDOW() {
        return StringIgnoreCaseWS("WINDOW");
    }


    //EPL
    public Rule CREATE() {
        return StringIgnoreCaseWS("CREATE");
    }

    public Rule AND_() {
        return StringIgnoreCaseWS("AND");
    }

    public Rule OR_() {
        return StringIgnoreCaseWS("OR");
    }

    public Rule FOLLOWED_BY() {
        return FirstOf(StringWS("->"), StringIgnoreCaseWS("FOLLOWED_BY"),
                Sequence(StringIgnoreCaseWS("FOLLOWED"), BY()));
    }

    public Rule MATCH() {
        return StringIgnoreCaseWS("MATCH");
    }

    public Rule EVERY() {
        return StringIgnoreCaseWS("EVERY");
    }

    public Rule WITHIN() {
        return StringIgnoreCaseWS("WITHIN");
    }

    public Rule TIME_UNIT() {
        return Sequence(FirstOf("ms", 's', 'm', 'h', 'd'), WS());
    }

    public Rule TimeConstrain() {
        return Sequence(INTEGER(), TIME_UNIT());
    }

    //DL

    public Rule SUBCLASSOF() {
        return StringIgnoreCaseWS("SubClassOf");
    }

    public Rule SOME() {
        return StringIgnoreCaseWS("some");
    }


}
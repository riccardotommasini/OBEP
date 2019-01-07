# Ontology-Based Event Processing

IDEA: using description logics to do event sourcing and extract events from streams on the web.

# Shape example

The Shape example extracts 3D shapes from a stream of 2D shapes and shape transformations.

<http://www.stream.org/shapestream> has the following VOCALS description

 

    PREFIX    : <http://www.example.org/geometry#>
    PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
    VOCAB <geometry.owl>
    FROM GRAPH <http://www.static.org>
    FROM STREAM <http://www.stream.org/shapestream>
    NAMED EVENT :ObsFig2D AS :Plane
    NAMED EVENT :ObsFig3D AS :Solid
    NAMED EVENT :ObsFigTransform AS :transforms some
    NAMED EVENT :ObsFig3D {  
    	MATCH every (:ObsFig2D -> :ObsFigTransform) WITHIN (5 s)     
      IF {  
         EVENT :ObsFig2D { ?x rdf:type :Figure  }        
         EVENT :ObsFigTransform { ?y :transforms ?x  }     }}
    
    RETURN :ObsFig3D AS RDF STREAM

**VOCAB** indicates the vocabulary to use as starting terminological box

**FROM GRAPH** indicates a set of static information to combines with the event stream

**FROM STREAM** indicates the stream of data to consume continuously

**NAMED EVENT** indicates an event definition. "Named" assigned to the event the possibility of being pushed out

**RETURN** specifies which event to push out and in which for [RDF Stream]

## The OBEP program above is translated into the following EPL program

    create schema ObsFigTransform as (event_content it.polimi.deib.sr.obep.core.data.events.Content)
    create schema ObsFig2D as (event_content it.polimi.deib.sr.obep.core.data.events.Content)
    create schema ObsFig3D as (event_content it.polimi.deib.sr.obep.core.data.events.Content)
    create schema ObsFig3D_ObsFig2D as (x string) inherits ObsFig2D
    create schema ObsFig3D_ObsFigTransform as (y string, x string) inherits ObsFigTransform
    

This part defines the new events. Notably, *ObsFig2D* corresponds to the generic definition. Their schema is bounded to an interface class: *it.polimi.deib.sr.obep.core.data.events.Content.*

The two sub-definitions present the prefix *ObsFig3D_*  that is the definition context, i.e., it included two variable projections coming from the filter definitions.

The an EPL query network is created with the help of a UDF to merge contents. 

*OutsStream* is where the event specified in the **RETURN** clause are pushed.

    
    insert into ObsFig3D select merge2(ObsFig2D0.event_content,ObsFigTransform1.event_content) as event_content from pattern [(every (ObsFig2D0=ObsFig3D_ObsFig2D -> ObsFigTransform1=ObsFig3D_ObsFigTransform(x=ObsFig2D0.x))) where timer:within(5 seconds)]
    insert into OutStream select * from ObsFig3D
    select ObsFig3D from OutStream
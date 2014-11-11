Dictionary Matcher Transformer
=========================
Dictionary Matcher is P3 transformer for SKOS based entity extraction. 

###Install and run

Clone the repository to your local machine

      git clone https://github.com/fusepoolP3/p3-dictionary-matcher-transfromer.git

Compile the application with

      mvn clean install

Start the application with

      mvn exec:java

Or start the application with parameters (`-P` sets the port, `-C` enables CORS)

      mvn exec:java -Dexec.args="-P 7100 -C"

###Usage

Example invocation with curl (setting the Content-Location header is optional)

      curl -X POST -H "Content-Location: http://example.com/document1" -d <data> "http://localhost:7100/?taxonomy=http://example.org/mytaxonomy.owl"
 
      <http://example.com/document1#annotation-body1>
            a       <http://vocab.fusepool.info/fam#LinkedEntity> ;
            <http://vocab.fusepool.info/fam#entity-label>
                    "Mobile Phone"^^<http://www.w3.org/2001/XMLSchema#string> ;
            <http://vocab.fusepool.info/fam#entity-mention>
                    "mobile phone"^^<http://www.w3.org/2001/XMLSchema#string> ;
            <http://vocab.fusepool.info/fam#entity-reference>
                    <http://example.org/productsMobile> ;
            <http://vocab.fusepool.info/fam#extracted-from>
                    <http://example.com/document1> ;
            <http://vocab.fusepool.info/fam#selector>
                    <http://example.com/document1#char=2,14> .
      			  
      <http://example.com/document1#annotation1>
            a       <http://www.w3.org/ns/oa#Annotation> ;
            <http://www.w3.org/ns/oa#annotatedAt>
                    "2014-10-30T12:54:42+0100"^^<http://www.w3.org/2001/XMLSchema#string> ;
            <http://www.w3.org/ns/oa#annotatedBy>
                    <p3-dictionary-matcher-transformer> ;
            <http://www.w3.org/ns/oa#hasBody>
                    <http://example.com/document1#annotation-body1> ;
            <http://www.w3.org/ns/oa#hasTarget>
                    <http://example.com/document1#sp-resource1> .
      
      <http://example.com/document1#sp-resource1>
            a       <http://www.w3.org/ns/oa#SpecificResource> ;
            <http://www.w3.org/ns/oa#hasSelector>
                    <http://example.com/document1#char=2,14> ;
            <http://www.w3.org/ns/oa#hasSource>
                    <http://example.com/document1#annotation-body1> .
      			  
      <http://example.com/document1#char=2,14>
            a       <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#String> , <http://vocab.fusepool.info/fam#NifSelector> ;
            <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#beginIndex>
                    "2"^^<http://www.w3.org/2001/XMLSchema#int> ;
            <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#endIndex>
                    "14"^^<http://www.w3.org/2001/XMLSchema#int> .
      
      <http://example.com/document1#annotation-body2>
            a       <http://vocab.fusepool.info/fam#LinkedEntity> ;
            <http://vocab.fusepool.info/fam#entity-label>
                    "Digital Camera"^^<http://www.w3.org/2001/XMLSchema#string> ;
            <http://vocab.fusepool.info/fam#entity-mention>
                    "digital camera"^^<http://www.w3.org/2001/XMLSchema#string> ;
            <http://vocab.fusepool.info/fam#entity-reference>
                    <http://example.org/productsCamera> ;
            <http://vocab.fusepool.info/fam#extracted-from>
                    <http://example.com/document1> ;
            <http://vocab.fusepool.info/fam#selector>
                    <http://example.com/document1#char=132,146> .
      			  
      <http://example.com/document1#annotation2>
            a       <http://www.w3.org/ns/oa#Annotation> ;
            <http://www.w3.org/ns/oa#annotatedAt>
                    "2014-10-30T12:54:42+0100"^^<http://www.w3.org/2001/XMLSchema#string> ;
            <http://www.w3.org/ns/oa#annotatedBy>
                    <p3-dictionary-matcher-transformer> ;
            <http://www.w3.org/ns/oa#hasBody>
                    <http://example.com/document1#annotation-body2> ;
            <http://www.w3.org/ns/oa#hasTarget>
                    <http://example.com/document1#sp-resource2> .
      
      <http://example.com/document1#sp-resource2>
            a       <http://www.w3.org/ns/oa#SpecificResource> ;
            <http://www.w3.org/ns/oa#hasSelector>
                    <http://example.com/document1#char=132,146> ;
            <http://www.w3.org/ns/oa#hasSource>
                    <http://example.com/document1#annotation-body2> .
      
      <http://example.com/document1#char=132,146>
            a       <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#String> , <http://vocab.fusepool.info/fam#NifSelector> ;
            <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#beginIndex>
                    "132"^^<http://www.w3.org/2001/XMLSchema#int> ;
            <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#endIndex>
                    "146"^^<http://www.w3.org/2001/XMLSchema#int> .

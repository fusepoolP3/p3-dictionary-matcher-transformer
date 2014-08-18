Dictionary Matcher Transformer
=========================
Dictionary Matcher is P3 transformer for SKOS based entity extraction. 

###Install

To clone the repository to your local machine

      git clone https://github.com/fusepoolP3/p3-dictionary-matcher-transfromer.git
      cd p3-dictionary-matcher-transfromer
      git submodule update --init

Compile and run with

      mvn clean install exec:java

Example invocation with curl

      $ curl -X POST -d @file.txt http://localhost:7100/?taxonomy=http://example.org/mytaxonomy.owl
      []    a       <http://example.org/ontology#Annotation> ;
            <http://example.org/ontology#altLabel>
                    "Mobile Phone"^^<http://www.w3.org/2001/XMLSchema#string> ;
            <http://example.org/ontology#begin>
                    "2"^^<http://www.w3.org/2001/XMLSchema#int> ;
            <http://example.org/ontology#end>
                    "14"^^<http://www.w3.org/2001/XMLSchema#int> ;
            <http://example.org/ontology#prefLabel>
                    "Mobile"^^<http://www.w3.org/2001/XMLSchema#string> ;
            <http://example.org/ontology#reference>
                    "http://example.org/productsMobile"^^<http://www.w3.org/2001/XMLSchema#anyURI> ;
            <http://example.org/ontology#textFound>
                    "mobile phone"^^<http://www.w3.org/2001/XMLSchema#string> .
      
      []    a       <http://example.org/ontology#Annotation> ;
            <http://example.org/ontology#altLabel>
                    "Hand Phone"^^<http://www.w3.org/2001/XMLSchema#string> ;
            <http://example.org/ontology#begin>
                    "66"^^<http://www.w3.org/2001/XMLSchema#int> ;
            <http://example.org/ontology#end>
                    "76"^^<http://www.w3.org/2001/XMLSchema#int> ;
            <http://example.org/ontology#prefLabel>
                    "Mobile"^^<http://www.w3.org/2001/XMLSchema#string> ;
            <http://example.org/ontology#reference>
                    "http://example.org/productsMobile"^^<http://www.w3.org/2001/XMLSchema#anyURI> ;
            <http://example.org/ontology#textFound>
                    "hand phone"^^<http://www.w3.org/2001/XMLSchema#string> .
      
      []    a       <http://example.org/ontology#Annotation> ;
            <http://example.org/ontology#altLabel>
                    "Cell Phone"^^<http://www.w3.org/2001/XMLSchema#string> ;
            <http://example.org/ontology#begin>
                    "48"^^<http://www.w3.org/2001/XMLSchema#int> ;
            <http://example.org/ontology#end>
                    "58"^^<http://www.w3.org/2001/XMLSchema#int> ;
            <http://example.org/ontology#prefLabel>
                    "Mobile"^^<http://www.w3.org/2001/XMLSchema#string> ;
            <http://example.org/ontology#reference>
                    "http://example.org/productsMobile"^^<http://www.w3.org/2001/XMLSchema#anyURI> ;
            <http://example.org/ontology#textFound>
                    "cell phone"^^<http://www.w3.org/2001/XMLSchema#string> .

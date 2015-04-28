# Dictionary Matcher Transformer [![Build Status](https://travis-ci.org/fusepoolP3/p3-dictionary-matcher-transformer.svg)](https://travis-ci.org/fusepoolP3/p3-dictionary-matcher-transformer)
The Dictionary Matcher Transformer is an information extraction tool that extracts words and phrases from text based on SKOS  taxonomies. It supports multiple languages and performs very fast keyword matching even with huge taxonomies. The transformer is based on the modified version of a string matching algorithm called Aho-Corasick.

## Try it out

To try it out, you can download the binaries of the latest release from the  [release page](https://github.com/fusepoolP3/p3-dictionary-matcher-transformer/releases).

Then start the application with
      
      java -jar p3-dictionary-matcher-transformer-v1.0.0-*-jar-with-dependencies.jar

Once it started use cURL to test it

      curl -X POST --data-binary "@test.txt" "http://localhost:8301/?taxonomy=http://example.org/example-taxonomy.rdf"

## Compiling and Running

Clone the repository to your local machine

      git clone https://github.com/fusepoolP3/p3-dictionary-matcher-transfromer.git

Compile and run the application with

      mvn clean install exec:java

Start the application with parameters (`-P` sets the port, `-C` enables CORS)

      mvn exec:java -Dexec.args="-P 8301 -C"

## Usage

The supported input and output formats of the transformer can be retrieved by the following GET request

      curl -X GET "http://localhost:8301/"
      <http://localhost:8301/>
      <http://vocab.fusepool.info/transformer#supportedInputFormat>
              "text/turtle"^^<http://www.w3.org/2001/XMLSchema#string> , "text/plain"^^<http://www.w3.org/2001/XMLSchema#string> ;
      <http://vocab.fusepool.info/transformer#supportedOutputFormat>
              "text/turtle"^^<http://www.w3.org/2001/XMLSchema#string> .

The transformer accepts the input data enclosed in the request message’s body, and expects the URI of the taxonomy (and additional options) in the query string.

      curl -X POST --data-binary <data> "http://localhost:8301/?taxonomy=<taxonomy_URI>&stemming=<stemming_language>&casesensitive=true"
      
`taxonomy` - URI of the taxonomy (it must be a valid resource location)

`stemming` - optional - if present, enables stemming (supported languages: `danish`, `dutch`, `english`, `finnish`, `french`, `german`, `hungarian`, `italian`, `norwegian`, `portuguese`, `romanian`, `russian`, `spanish`, `swedish`, `turkish`)

`casesensitive` - optional - if present, enables case sensitivity

The following curl example shows an example invocation of the Dictionary Matcher Transformer of a local running instance:

      curl -X POST --data-binary "Frauds and Swindlings cause significant concerns with regards to Ethics." "http://localhost:8301/?taxonomy=http://data.nytimes.com/descriptors.rdf&stemming=english"
 
      <http://localhost:8301/bad1b7a2-431a-4861-acd5-f01515a6d897#annotation-body1>
            a       <http://vocab.fusepool.info/fam#LinkedEntity> ;
            <http://vocab.fusepool.info/fam#entity-label>
                    "Frauds and Swindling"^^<http://www.w3.org/2001/XMLSchema#string> ;
            <http://vocab.fusepool.info/fam#entity-mention>
                    "Frauds and Swindlings"^^<http://www.w3.org/2001/XMLSchema#string> ;
            <http://vocab.fusepool.info/fam#entity-reference>
                    <http://data.nytimes.com/N38522309997148425060> ;
            <http://vocab.fusepool.info/fam#extracted-from>
                    <http://localhost:8301/bad1b7a2-431a-4861-acd5-f01515a6d897> ;
            <http://vocab.fusepool.info/fam#selector>
                    <http://localhost:8301/bad1b7a2-431a-4861-acd5-f01515a6d897#char=0,21> .
      
      <http://localhost:8301/bad1b7a2-431a-4861-acd5-f01515a6d897#annotation1>
            a       <http://www.w3.org/ns/oa#Annotation> ;
            <http://www.w3.org/ns/oa#annotatedAt>
                    "2014-10-30T14:35:26+0100"^^<http://www.w3.org/2001/XMLSchema#string> ;
            <http://www.w3.org/ns/oa#annotatedBy>
                    <p3-dictionary-matcher-transformer> ;
            <http://www.w3.org/ns/oa#hasBody>
                    <http://localhost:8301/bad1b7a2-431a-4861-acd5-f01515a6d897#annotation-body1> ;
            <http://www.w3.org/ns/oa#hasTarget>
                    <http://localhost:8301/bad1b7a2-431a-4861-acd5-f01515a6d897#sp-resource1> .			  
      		
      <http://localhost:8301/bad1b7a2-431a-4861-acd5-f01515a6d897#sp-resource1>
            a       <http://www.w3.org/ns/oa#SpecificResource> ;
            <http://www.w3.org/ns/oa#hasSelector>
                    <http://localhost:8301/bad1b7a2-431a-4861-acd5-f01515a6d897#char=0,21> ;
            <http://www.w3.org/ns/oa#hasSource>
                    <http://localhost:8301/bad1b7a2-431a-4861-acd5-f01515a6d897#annotation-body1> .
      		
      <http://localhost:8301/bad1b7a2-431a-4861-acd5-f01515a6d897#char=0,21>
            a       <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#String> , <http://vocab.fusepool.info/fam#NifSelector> ;
            <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#beginIndex>
                    "0"^^<http://www.w3.org/2001/XMLSchema#int> ;
            <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#endIndex>
                    "21"^^<http://www.w3.org/2001/XMLSchema#int> .	
      	
      <http://localhost:8301/bad1b7a2-431a-4861-acd5-f01515a6d897#annotation-body2>
            a       <http://vocab.fusepool.info/fam#LinkedEntity> ;
            <http://vocab.fusepool.info/fam#entity-label>
                    "Ethics"^^<http://www.w3.org/2001/XMLSchema#string> ;
            <http://vocab.fusepool.info/fam#entity-mention>
                    "Ethics"^^<http://www.w3.org/2001/XMLSchema#string> ;
            <http://vocab.fusepool.info/fam#entity-reference>
                    <http://data.nytimes.com/48662871776634757120> ;
            <http://vocab.fusepool.info/fam#extracted-from>
                    <http://localhost:8301/bad1b7a2-431a-4861-acd5-f01515a6d897> ;
            <http://vocab.fusepool.info/fam#selector>
                    <http://localhost:8301/bad1b7a2-431a-4861-acd5-f01515a6d897#char=65,71> .
      			  
      <http://localhost:8301/bad1b7a2-431a-4861-acd5-f01515a6d897#annotation2>
            a       <http://www.w3.org/ns/oa#Annotation> ;
            <http://www.w3.org/ns/oa#annotatedAt>
                    "2014-10-30T14:35:26+0100"^^<http://www.w3.org/2001/XMLSchema#string> ;
            <http://www.w3.org/ns/oa#annotatedBy>
                    <p3-dictionary-matcher-transformer> ;
            <http://www.w3.org/ns/oa#hasBody>
                    <http://localhost:8301/bad1b7a2-431a-4861-acd5-f01515a6d897#annotation-body2> ;
            <http://www.w3.org/ns/oa#hasTarget>
                    <http://localhost:8301/bad1b7a2-431a-4861-acd5-f01515a6d897#sp-resource2> .
      
      <http://localhost:8301/bad1b7a2-431a-4861-acd5-f01515a6d897#sp-resource2>
            a       <http://www.w3.org/ns/oa#SpecificResource> ;
            <http://www.w3.org/ns/oa#hasSelector>
                    <http://localhost:8301/bad1b7a2-431a-4861-acd5-f01515a6d897#char=65,71> ;
            <http://www.w3.org/ns/oa#hasSource>
                    <http://localhost:8301/bad1b7a2-431a-4861-acd5-f01515a6d897#annotation-body2> .
      
      <http://localhost:8301/bad1b7a2-431a-4861-acd5-f01515a6d897#char=65,71>
            a       <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#String> , <http://vocab.fusepool.info/fam#NifSelector> ;
            <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#beginIndex>
                    "65"^^<http://www.w3.org/2001/XMLSchema#int> ;
            <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#endIndex>
                    "71"^^<http://www.w3.org/2001/XMLSchema#int> .
                  <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#endIndex>
                          "146"^^<http://www.w3.org/2001/XMLSchema#int> .

## References
This application implements the requirements in [FP-39](https://fusepool.atlassian.net/browse/FP-39), [FP-105](https://fusepool.atlassian.net/browse/FP-105) and [FP-197](https://fusepool.atlassian.net/browse/FP-197).

Dictionary Matcher Transformer
=========================
Dictionary Matcher is a SKOS based text extractor. 

###Install

To clone the repository to your local machine

<pre>git clone https://github.com/fusepoolP3/dictionary-matcher-parent.git
cd dictionary-matcher-parent
git submodule update --init</pre>

Compile and run with

<pre>mvn clean install exec:java</pre>

Example invocation with curl

<pre>$ curl -X POST -d @file.txt http://localhost:7100/?taxonomy=http://example.org/mytaxonomy.owl
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
              "cell phone"^^<http://www.w3.org/2001/XMLSchema#string> .</pre>

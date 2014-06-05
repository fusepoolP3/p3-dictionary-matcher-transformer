# Java Extractor Implementation Library

Provides a library to implement 
[Extractors](https://github.com/fusepoolP3/overall-architecture/blob/master/data-extractor-importer-api.md)
in Java.

It supports both the asynchronous and the asynchronous modes of the REST API.

Implementations can either implement the `eu.fusepool.extractor.SyncExtractor` or
`eu.fusepool.extractor.AsyncExtractor` interface. The `SyncExtractor` provides a
very simple API and has also support for long-running tasks that will be exposed 
over the Asynchronous REST Interface.


Minimalistic sample extractor are contained in the package 
`eu.fusepool.extractor.sample`. The package also contains an executable main 
class.

Compile and run (the sample main class) with

    mvn clean install exec:java

Example invocation with curl:


    $ curl -X POST -d @file.txt http://localhost:7100/
    []    a       <http://example.org/ontology#TextDescription> ;
          <http://example.org/ontology#textLength>
                  "5"^^<http://www.w3.org/2001/XMLSchema#int> ;
          <http://rdfs.org/sioc/ns#content>
                  "hallo"^^<http://www.w3.org/2001/XMLSchema#string> .


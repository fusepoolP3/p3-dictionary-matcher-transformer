/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fusepoolp3.dictionarymatcher;

import eu.fusepoolp3.dmasimple.DictionaryStore;
import java.net.URI;
import org.semanticweb.skos.SKOSAnnotation;
import org.semanticweb.skos.SKOSConcept;
import org.semanticweb.skos.SKOSCreationException;
import org.semanticweb.skos.SKOSDataRelationAssertion;
import org.semanticweb.skos.SKOSDataset;
import org.semanticweb.skos.SKOSEntity;
import org.semanticweb.skos.SKOSLiteral;
import org.semanticweb.skos.SKOSObjectRelationAssertion;
import org.semanticweb.skos.SKOSTypedLiteral;
import org.semanticweb.skos.SKOSUntypedLiteral;
import org.semanticweb.skosapibinding.SKOSManager;

/**
 *
 * @author Gabor
 */
public class ReadSKOS {
    public static DictionaryStore GetDictionary(URI uri){
        // create dictionary store to store pref and alt labels
        DictionaryStore dictionary = new DictionaryStore();
        
        try {

            SKOSManager manager = new SKOSManager();
  
            SKOSDataset dataSet = manager.loadDataset(uri);

            for (SKOSConcept concept : dataSet.getSKOSConcepts()) {

                System.out.println("Concept: " + concept.getURI());

                /*
                 * SKOS entities such as Concepts, ConceptSchemes (See SKOSEntity in Javadoc for full list), are related to other
                 * entities or literal values by three different types of relationships.
                 * ObjectPropertyAssertions - These are relationships between two SKOS entities
                 * DataPropertyAssertion - These relate entities to Literal values
                 * SKOSAnnotation - These are either literal or entity annotation on a particular entity
                 */

                // print out object assertions
                concept.getSKOSAnnotationsByURI(dataSet, manager.getSKOSDataFactory().getSKOSBroaderProperty().getURI());


                System.out.println("\tObject property assertions:");
                for (SKOSObjectRelationAssertion objectAssertion : dataSet.getSKOSObjectRelationAssertions(concept)) {
                    System.out.println("\t\t" + objectAssertion.getSKOSProperty().getURI().getFragment() + " " + objectAssertion.getSKOSObject().getURI().getFragment());
                    
                }
                System.out.println("");

                // print out any data property assertions
                System.out.println("\tData property assertions:");
                for (SKOSDataRelationAssertion assertion : dataSet.getSKOSDataRelationAssertions(concept)) {

                    // the object of a data assertion can be either a typed or untyped literal
                    SKOSLiteral literal = assertion.getSKOSObject();
                    String lang = "";
                    if (literal.isTyped()) {

                        SKOSTypedLiteral typedLiteral = literal.getAsSKOSTypedLiteral();
                        System.out.println("\t\t" + assertion.getSKOSProperty().getURI().getFragment() + " " + literal.getLiteral() + " Type:" + typedLiteral.getDataType().getURI());
                    } else {

                        // if it has language
                        SKOSUntypedLiteral untypedLiteral = literal.getAsSKOSUntypedLiteral();
                        if (untypedLiteral.hasLang()) {
                            lang = untypedLiteral.getLang();
                        }
                        System.out.println("\t\t" + assertion.getSKOSProperty().getURI().getFragment() + " " + literal.getLiteral() + " Lang:" + lang);

                    }
                }
                System.out.println("");


                // finally get any OWL annotations - the object of a annotation property can be a literal or an entity
                System.out.println("\tAnnotation property assertions:");
                for (SKOSAnnotation assertion : dataSet.getSKOSAnnotations(concept)) {

                    // if the annotation is a literal annotation?
                    String lang = "";
                    String value = "";

                    if (assertion.isAnnotationByConstant()) {

                        SKOSLiteral literal = assertion.getAnnotationValueAsConstant();
                        value = literal.getLiteral();
                        if (!literal.isTyped()) {
                            // if it has language
                            SKOSUntypedLiteral untypedLiteral = literal.getAsSKOSUntypedLiteral();
                            if (untypedLiteral.hasLang()) {
                                lang = untypedLiteral.getLang();
                            }
                        }
                    } else {
                        // annotation is some resource
                        SKOSEntity entity = assertion.getAnnotationValue();
                        value = entity.getURI().getFragment();
                    }
                    
                    if(value != null){
                        dictionary.AddOriginalElement(
                                value,
                                assertion.getURI().getFragment(),
                                concept.getURI().toString());
                    }
                    
                    System.out.println("\t\t" + assertion.getURI().getFragment() + " " + value + " Lang:" + lang);
                }
//                System.out.println("");
            }
        } catch (SKOSCreationException e) {
            e.printStackTrace();
        }
        
        return dictionary;
    }
}

package eu.fusepoolp3.dmasimple;

import java.net.URI;
import java.net.URISyntaxException;
import org.apache.commons.validator.UrlValidator;
import org.semanticweb.skos.SKOSAnnotation;
import org.semanticweb.skos.SKOSConcept;
import org.semanticweb.skos.SKOSCreationException;
import org.semanticweb.skos.SKOSDataRelationAssertion;
import org.semanticweb.skos.SKOSDataset;
import org.semanticweb.skos.SKOSEntity;
import org.semanticweb.skos.SKOSLiteral;
import org.semanticweb.skos.SKOSTypedLiteral;
import org.semanticweb.skos.SKOSUntypedLiteral;
import org.semanticweb.skosapibinding.SKOSManager;

/**
 * SKOS entities such as Concepts, ConceptSchemes (See SKOSEntity in Javadoc for
 * full list), are related to other entities or literal values by three
 * different types of relationships. ObjectPropertyAssertions - These are
 * relationships between two SKOS entities DataPropertyAssertion - These relate
 * entities to Literal values SKOSAnnotation - These are either literal or
 * entity annotation on a particular entity
 *
 * @author Gabor
 */
public class Skos {

    public static DictionaryStore readDictionary(String taxonomy) {
        try {
            SKOSManager manager = new SKOSManager();

            URI uri = null;

            try {
                // see if url is valid
                if (isURLValid(taxonomy)) {
                    uri = new URI(taxonomy);
                } else {
                    // if it is not valid try to get it from resources
                    uri = Skos.class.getResource("/" + taxonomy).toURI();
                }
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }

            // create dictionary store to store pref and alt labels
            DictionaryStore dictionary = new DictionaryStore();

            // load data set from uri
            SKOSDataset dataSet = manager.loadDataset(uri);

            for (SKOSConcept concept : dataSet.getSKOSConcepts()) {

//                System.out.println("Concept: " + concept.getURI());

                // print out object assertions
                concept.getSKOSAnnotationsByURI(dataSet, manager.getSKOSDataFactory().getSKOSBroaderProperty().getURI());

//                System.out.println("\tObject property assertions:");
//                for (SKOSObjectRelationAssertion objectAssertion : dataSet.getSKOSObjectRelationAssertions(concept)) {
//                    System.out.println("\t\t" + objectAssertion.getSKOSProperty().getURI().getFragment() + " " + objectAssertion.getSKOSObject().getURI().getFragment());
//                    
//                }
//                System.out.println("");

                // print out any data property assertions
//                System.out.println("\tData property assertions:");
                for (SKOSDataRelationAssertion assertion : dataSet.getSKOSDataRelationAssertions(concept)) {
                    // the object of a data assertion can be either a typed or untyped literal
                    SKOSLiteral literal = assertion.getSKOSObject();
                    String lang = "";
                    if (literal.isTyped()) {
                        SKOSTypedLiteral typedLiteral = literal.getAsSKOSTypedLiteral();
//                        System.out.println("\t\t" + assertion.getSKOSProperty().getURI().getFragment() + " " + literal.getLiteral() + " Type:" + typedLiteral.getDataType().getURI());
                    } else {
                        // if it has language
                        SKOSUntypedLiteral untypedLiteral = literal.getAsSKOSUntypedLiteral();
                        if (untypedLiteral.hasLang()) {
                            lang = untypedLiteral.getLang();
                        }
//                        System.out.println("\t\t" + assertion.getSKOSProperty().getURI().getFragment() + " " + literal.getLiteral() + " Lang:" + lang);
                    }
                }
//                System.out.println("");

                // finally get any OWL annotations - the object of a annotation property can be a literal or an entity
//                System.out.println("\tAnnotation property assertions:");
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

                    String labelText = value;
                    String labelType = assertion.getURI().getFragment();
                    String uriText = concept.getURI().toString();

                    if (isConceptValid(labelText, labelType, uriText)) {
                        dictionary.AddOriginalElement(
                                labelText,
                                labelType,
                                uriText);
                    }
//                    System.out.println("\t\t" + assertion.getURI().getFragment() + " " + value + " Lang:" + lang);
                }
//                System.out.println("");
            }

            return dictionary;

        } catch (SKOSCreationException e) {
            throw new RuntimeException(e);
        }
    }

    private static Boolean isConceptValid(String labelText, String labelType, String uriText) {
        if (labelText != null && labelType != null && uriText != null) {
            if (LabelType.ALT.toString().equals(labelType)) {
                return true;
            }
            if (LabelType.PREF.toString().equals(labelType)) {
                return true;
            }
        }
        return false;
    }

    private static Boolean isURLValid(String uriString) {
        String[] schemes = {"http", "https"};
        UrlValidator urlValidator = new UrlValidator(schemes);
        if (urlValidator.isValid(uriString)) {
            return true;
        } else {
            return false;
        }
    }
}

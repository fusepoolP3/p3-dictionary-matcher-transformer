package eu.fusepool.p3.dictionarymatcher;

import eu.fusepool.p3.transformer.TransformerException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.validator.UrlValidator;
import org.semanticweb.skos.SKOSAnnotation;
import org.semanticweb.skos.SKOSConcept;
import org.semanticweb.skos.SKOSCreationException;
import org.semanticweb.skos.SKOSDataset;
import org.semanticweb.skos.SKOSEntity;
import org.semanticweb.skos.SKOSLiteral;
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
            } catch (URISyntaxException | NullPointerException e) {
                throw new TransformerException(HttpServletResponse.SC_BAD_REQUEST, "ERROR: Taxonomy URI is invalid! (\"" + taxonomy + "\")");
            }

            // create dictionary store to store pref and alt labels
            DictionaryStore dictionary = new DictionaryStore();

            // load data set from uri
            SKOSDataset dataSet = manager.loadDataset(uri);

            for (SKOSConcept concept : dataSet.getSKOSConcepts()) {
                concept.getSKOSAnnotationsByURI(dataSet, manager.getSKOSDataFactory().getSKOSBroaderProperty().getURI());

                // get any OWL annotations - the object of a annotation property can be a literal or an entity
                for (SKOSAnnotation assertion : dataSet.getSKOSAnnotations(concept)) {
                    String value, lang = "";
                    // if the annotation is a literal annotation?
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
                    // get the label
                    String labelText = value;
                    // get the type of the label (prefLabel or altLabel)
                    String labelType = assertion.getURI().getFragment();
                    // get the URI of the label
                    String uriText = concept.getURI().toString();
                    // if the concept is valid, add concept to the dictionary
                    if (isConceptValid(labelText, labelType, uriText)) {
                        dictionary.AddOriginalElement(labelText, labelType, uriText);
                    }
                }
            }

            return dictionary;

        } catch (SKOSCreationException e) {
            throw new RuntimeException(e);
        }
    }

    public static Boolean isConceptValid(String labelText, String labelType, String uriText) {
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
        return urlValidator.isValid(uriString);
    }
}

package org.opengis.cite.kml22.util;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.logging.Level;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;

import org.apache.xerces.util.XMLCatalogResolver;
import org.opengis.cite.kml22.ErrorMessage;
import org.opengis.cite.validation.ErrorLocator;
import org.opengis.cite.validation.ErrorSeverity;
import org.opengis.cite.validation.SchematronValidator;
import org.opengis.cite.validation.ValidationErrorHandler;
import org.opengis.cite.validation.XmlSchemaCompiler;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * A utility class that provides convenience methods to support schema
 * validation.
 */
public class ValidationUtils {

    private static final String ROOT_PKG = "/org/opengis/cite/kml22/";
    private static final XMLCatalogResolver SCH_RESOLVER = initCatalogResolver();

    private static XMLCatalogResolver initCatalogResolver() {
        URL catalogURL = ValidationUtils.class
                .getResource("/org/opengis/cite/kml22/schematron-catalog.xml");
        XMLCatalogResolver resolver = new XMLCatalogResolver();
        resolver.setCatalogList(new String[] { catalogURL.toString() });
        return resolver;
    }

    /**
     * Constructs a SchematronValidator that will check an XML resource against
     * the rules defined in a Schematron schema. An attempt is made to resolve
     * the schema reference using an entity catalog; if this fails the reference
     * is used as given.
     * 
     * @param schemaRef
     *            A reference to a Schematron schema; this is expected to be a
     *            relative or absolute URI value, possibly matching the system
     *            identifier for some entry in an entity catalog.
     * @param phase
     *            The name of the phase to invoke.
     * @return A SchematronValidator instance, or {@code null} if the validator
     *         cannot be constructed (e.g. invalid schema reference or phase
     *         name).
     */
    public static SchematronValidator buildSchematronValidator(
            String schemaRef, String phase) {
        Source source = null;
        try {
            String catalogRef = SCH_RESOLVER
                    .resolveSystem(schemaRef.toString());
            if (null != catalogRef) {
                source = new StreamSource(URI.create(catalogRef).toString());
            } else {
                source = new StreamSource(schemaRef);
            }
        } catch (IOException x) {
            TestSuiteLogger.log(Level.WARNING,
                    "Error reading Schematron schema catalog.", x);
        }
        SchematronValidator validator = null;
        try {
            validator = new SchematronValidator(source, phase);
        } catch (Exception e) {
            TestSuiteLogger.log(Level.WARNING,
                    "Error creating Schematron validator.", e);
        }
        return validator;
    }

    /**
     * Extracts an XML Schema reference from a source XML document. The
     * resulting URI value refers to the schema whose target namespace matches
     * the namespace of the document element.
     * 
     * @param source
     *            The source instance to read from; its base URI (systemId)
     *            should be set. The document element is expected to include the
     *            standard xsi:schemaLocation attribute.
     * @param baseURI
     *            An alternative base URI to use if the source does not have a
     *            system identifier set or if its system id is a {@code file}
     *            URI. This will usually be the URI used to retrieve the
     *            resource; it may be null.
     * @return An absolute URI reference specifying the location of an XML
     *         Schema resource, or {@code null} if no reference is found.
     * @throws XMLStreamException
     *             If an error occurs while reading the source instance.
     */
    public static URI extractSchemaReference(Source source, String baseURI)
            throws XMLStreamException {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLEventReader reader = factory.createXMLEventReader(source);
        // advance to document element
        StartElement docElem = reader.nextTag().asStartElement();
        QName qName = docElem.getName();
        String namespace = qName.getNamespaceURI();
        Attribute schemaLoc = docElem.getAttributeByName(new QName(
                XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "schemaLocation"));
        String[] uriValues = new String[] {};
        if (null != schemaLoc) {
            uriValues = schemaLoc.getValue().split("\\s");
        }
        URI schemaURI = null;
        // one or more pairs of [namespace name] [schema location]
        for (int i = 0; i < uriValues.length; i += 2) {
            if (uriValues[i].equals(namespace)) {
                if (!URI.create(uriValues[i + 1]).isAbsolute()
                        && (null != source.getSystemId())) {
                    String schemaRef = URIUtils.resolveRelativeURI(
                            source.getSystemId(), uriValues[i + 1]).toString();
                    if (schemaRef.startsWith("file")
                            && !new File(schemaRef).exists()
                            && (null != baseURI)) {
                        schemaRef = URIUtils.resolveRelativeURI(baseURI,
                                uriValues[i + 1]).toString();
                    }
                    schemaURI = URI.create(schemaRef);
                } else {
                    schemaURI = URI.create(uriValues[i + 1]);
                }
                break;
            }
        }
        return schemaURI;
    }

    /**
     * Creates a single Schema object representing the complete set of XML
     * Schema constraints that apply to KML 2.2 documents. It incorporates
     * schema components from all relevant namespaces.
     * 
     * @return An immutable Schema object, or <code>null</code> if one cannot be
     *         constructed.
     * 
     * @see <a href="http://schemas.opengis.net/kml/2.2.0/ogckml22.xsd"
     *      target="_blank">XML Schema for OGC KML version 2.2</a>
     */
    public static Schema createKMLSchema() {
        URL entityCatalog = ValidationUtils.class.getResource(ROOT_PKG
                + "schema-catalog.xml");
        XmlSchemaCompiler xsdCompiler = new XmlSchemaCompiler(entityCatalog);
        Schema kmlSchema = null;
        try {
            URL schemaURL = ValidationUtils.class.getResource(ROOT_PKG
                    + "xsd/ogc/kml/2.2.0/ogckml22.xsd");
            Source xsdSource = new StreamSource(schemaURL.toString());
            kmlSchema = xsdCompiler
                    .compileXmlSchema(new Source[] { xsdSource });
        } catch (SAXException e) {
            TestSuiteLogger.log(Level.WARNING,
                    "Failed to create KML Schema object.", e);
        }
        return kmlSchema;
    }

    /**
     * Validates coordinate tuples by checking for (1) the occurrence of
     * non-float values, (2) inconsistent tuple dimension, and (3) coordinates
     * lying outside the valid lat/lon extent of the CRS.
     * 
     * The KML 2.2 specification defines a compound geographic 3D coordinate
     * reference system in Annex B ("urn:ogc:def:crs:OGC:LonLat84_5773").
     * 
     * @param coordinates
     *            A kml:coordinates element; it's expected to contain a
     *            whitespace-separated (SP,CR,LF,HT) sequence of coordinate
     *            tuples.
     * @param minDim
     *            The expected minimum tuple dimension (2 or 3).
     * @param errHandler
     *            An error handler to collect any detected constraint
     *            violations.
     */
    public static void validateCoordinateTuples(Node coordinates, int minDim,
            ValidationErrorHandler errHandler) {
        if (!coordinates.getLocalName().equals("coordinates"))
            return;
        int minTupleDim = (minDim < 2 || minDim > 3) ? 2 : minDim;
        // use XPointer to locate detected errors
        String xpointer = XMLUtils.getXPointer(coordinates);
        String coords = coordinates.getTextContent();
        String[] tuples = coords.trim().split("[ \\t\\n\\r]{1,}");
        for (int i = 0; i < tuples.length; i++) {
            String[] tuple = tuples[i].trim().split(",");
            if (tuple.length < minTupleDim || tuple.length > 3) {
                errHandler.addError(ErrorSeverity.ERROR, ErrorMessage.format(
                        "level1.GeometryCoordinates.dim", minTupleDim, i + 1),
                        new ErrorLocator(-1, -1, xpointer));
                continue;
            }
            try {
                Float.valueOf(tuple[0]);
                Float.valueOf(tuple[1]);
                if (tuple.length == 3) {
                    Float.valueOf(tuple[2]);
                }
            } catch (NumberFormatException e) {
                errHandler.addError(ErrorSeverity.ERROR, ErrorMessage.format(
                        "level1.GeometryCoordinates.float", i + 1),
                        new ErrorLocator(-1, -1, xpointer));
                // Skip range checks if non-numeric value
                continue;
            }
            if ((Float.valueOf(tuple[0]) < -180.0 || Float.valueOf(tuple[0]) > 180.0)) {
                errHandler.addError(ErrorSeverity.ERROR, ErrorMessage.format(
                        "level1.GeometryCoordinates.lon", i + 1),
                        new ErrorLocator(-1, -1, xpointer));
            }
            if ((Float.valueOf(tuple[1]) < -90.0 || Float.valueOf(tuple[1]) > 90.0)) {
                errHandler.addError(ErrorSeverity.ERROR, ErrorMessage.format(
                        "level1.GeometryCoordinates.lat", i + 1),
                        new ErrorLocator(-1, -1, xpointer));
            }
        }
    }
}

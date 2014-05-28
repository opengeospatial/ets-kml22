package org.opengis.cite.kml22.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opengis.cite.kml22.Namespaces;
import org.opengis.cite.validation.SchematronValidator;
import org.opengis.cite.validation.ValidationError;
import org.opengis.cite.validation.ValidationErrorHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Verifies the behavior of the ValidationUtils class.
 */
public class VerifyValidationUtils {

    private static DocumentBuilder docBuilder;

    public VerifyValidationUtils() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        docBuilder = dbf.newDocumentBuilder();
    }

    @Test
    public void testBuildSchematronValidator() {
        // matches entry in schematron-catalog.xml
        String schemaRef = "gml-3.2.1.sch";
        String phase = "";
        SchematronValidator result = ValidationUtils.buildSchematronValidator(
                schemaRef, phase);
        Assert.assertNotNull(result);
    }

    @Test
    public void extractRelativeSchemaReference() throws FileNotFoundException,
            XMLStreamException {
        File xmlFile = new File("src/test/resources/Alpha-1.xml");
        URI xsdRef = ValidationUtils.extractSchemaReference(new StreamSource(
                xmlFile), null);
        Assert.assertTrue("Expected schema reference */xsd/alpha.xsd", xsdRef
                .toString().endsWith("/xsd/alpha.xsd"));
    }

    @Test
    public void testCompileKMLSchema() {
        Schema kmlSchema = ValidationUtils.createKMLSchema();
        Assert.assertNotNull(kmlSchema);
    }

    @Test
    public void coordinatesWith1DTuple() throws SAXException, IOException {
        Document doc = docBuilder.parse(this.getClass().getResourceAsStream(
                "/kml/LinearRingWith1DTuple.kml"));
        Node coords = doc.getElementsByTagNameNS(Namespaces.KML22,
                "coordinates").item(0);
        ValidationErrorHandler errHandler = new ValidationErrorHandler();
        ValidationUtils.validateCoordinateTuples(coords, 2, errHandler);
        Assert.assertEquals("Unexpected number of errors detected.", 1,
                errHandler.getErrorCount());
        ValidationError err = errHandler.iterator().next();
        Assert.assertTrue("Unexpected error message.", err.getMessage()
                .contains("tuple not of minimum length 2 [position()=3]"));
    }

    @Test
    public void coordinatesWithLatOutOfRange() throws SAXException, IOException {
        Document doc = docBuilder.parse(this.getClass().getResourceAsStream(
                "/kml/LinearRingWithInvalidLat.kml"));
        Node coords = doc.getElementsByTagNameNS(Namespaces.KML22,
                "coordinates").item(0);
        ValidationErrorHandler errHandler = new ValidationErrorHandler();
        ValidationUtils.validateCoordinateTuples(coords, 2, errHandler);
        Assert.assertEquals("Unexpected number of errors detected.", 1,
                errHandler.getErrorCount());
        ValidationError err = errHandler.iterator().next();
        Assert.assertTrue(
                "Unexpected error message.",
                err.getMessage().contains(
                        "latitude outside valid range of +/-90 [position()=4]"));
    }
}

package org.opengis.cite.kml22.level1;

import java.io.IOException;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathExpressionException;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opengis.cite.kml22.Namespaces;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.testng.ISuite;
import org.testng.ITestContext;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Verifies the behavior of the test class UserDefinedDataTests.
 */
public class VerifyUserDefinedDataTests {

    private static DocumentBuilder docBuilder;
    private static ITestContext testContext;
    private static ISuite suite;
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    public VerifyUserDefinedDataTests() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        testContext = mock(ITestContext.class);
        suite = mock(ISuite.class);
        when(testContext.getSuite()).thenReturn(suite);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        docBuilder = dbf.newDocumentBuilder();
    }

    @Test
    public void simpleFieldOk() throws SAXException, IOException {
        URL url = this.getClass().getResource("/kml/custom/SimpleField-Ok.xml");
        Document doc = docBuilder.parse(url.toString());
        UserDefinedDataTests iut = new UserDefinedDataTests();
        iut.setTestSubject(doc);
        iut.verifySimpleField();
    }

    @Test
    public void simpleFieldHasUnsupportedType() throws SAXException,
            IOException {
        thrown.expect(AssertionError.class);
        thrown.expectMessage("kml:SimpleField[@name=\"date\"] has an unsupported @type attribute value.");
        URL url = this.getClass().getResource(
                "/kml/custom/SimpleField-UnsupportedType.xml");
        Document doc = docBuilder.parse(url.toString());
        UserDefinedDataTests iut = new UserDefinedDataTests();
        iut.setTestSubject(doc);
        iut.verifySimpleField();
    }

    @Test
    public void schemaDataWithValidSchemaRef() throws SAXException, IOException {
        URL url = this.getClass().getResource("/kml/custom/SchemaData-Ok.xml");
        Document doc = docBuilder.parse(url.toString());
        UserDefinedDataTests iut = new UserDefinedDataTests();
        iut.setTestSubject(doc);
        iut.verifySchemaData();
    }

    @Test
    @Ignore("Passes, but skip to avoid network connection.")
    public void schemaDataWithValidRemoteSchemaRef() throws SAXException,
            IOException {
        URL url = this.getClass().getResource(
                "/kml/custom/SchemaData-RemoteSchema.xml");
        Document doc = docBuilder.parse(url.toString());
        UserDefinedDataTests iut = new UserDefinedDataTests();
        iut.setTestSubject(doc);
        iut.verifySchemaData();
    }

    @Test
    public void schemaDataWithInvalidSchemaRef() throws SAXException,
            IOException {
        thrown.expect(AssertionError.class);
        thrown.expectMessage("kml:SchemaData references a kml:Schema element that cannot be found");
        URL url = this.getClass().getResource(
                "/kml/custom/SchemaData-NoSchema.xml");
        Document doc = docBuilder.parse(url.toString());
        UserDefinedDataTests iut = new UserDefinedDataTests();
        iut.setTestSubject(doc);
        iut.verifySchemaData();
    }

    @Test
    public void floatFromNonNumericValueShouldFail() {
        QName dataType = new QName(Namespaces.XSD, "float");
        UserDefinedDataTests iut = new UserDefinedDataTests();
        assertFalse(iut.valueConformsToType("abcd", dataType));
    }

    @Test
    public void booleanFromTRUE() {
        QName dataType = new QName(Namespaces.XSD, "boolean");
        UserDefinedDataTests iut = new UserDefinedDataTests();
        assertTrue(iut.valueConformsToType("TRUE", dataType));
    }

    @Test
    public void schemaDataWithInvalidDatum() throws SAXException, IOException {
        thrown.expect(AssertionError.class);
        thrown.expectMessage("kml:SchemaData contains a kml:SimpleData value that does not correspond to the declared type");
        URL url = this.getClass().getResource(
                "/kml/custom/SchemaData-InvalidDatum.xml");
        Document doc = docBuilder.parse(url.toString());
        UserDefinedDataTests iut = new UserDefinedDataTests();
        iut.setTestSubject(doc);
        iut.verifySchemaData();
    }

    @Test
    public void untypedDataOk() throws SAXException, IOException,
            XPathExpressionException {
        URL url = this.getClass().getResource("/kml/custom/Data-Ok.xml");
        Document doc = docBuilder.parse(url.toString());
        UserDefinedDataTests iut = new UserDefinedDataTests();
        iut.setTestSubject(doc);
        iut.verifyUntypedDataAreUnique();
    }

    @Test
    public void untypedDataNotUnique() throws SAXException, IOException,
            XPathExpressionException {
        thrown.expect(AssertionError.class);
        thrown.expectMessage("kml:ExtendedData contains a kml:Data element with a non-unique @name");
        URL url = this.getClass().getResource("/kml/custom/Data-Duplicate.xml");
        Document doc = docBuilder.parse(url.toString());
        UserDefinedDataTests iut = new UserDefinedDataTests();
        iut.setTestSubject(doc);
        iut.verifyUntypedDataAreUnique();
    }
}

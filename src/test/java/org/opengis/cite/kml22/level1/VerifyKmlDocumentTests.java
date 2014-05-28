package org.opengis.cite.kml22.level1;

import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.validation.Schema;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opengis.cite.kml22.SuiteAttribute;
import org.opengis.cite.kml22.util.ValidationUtils;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.testng.ISuite;
import org.testng.ITestContext;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Verifies the behavior of the test class KmlDocumentTests. Test stubs replace
 * fixture constituents where appropriate.
 */
public class VerifyKmlDocumentTests {

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    private static final String SUBJ = SuiteAttribute.TEST_SUBJECT.getName();
    private static DocumentBuilder docBuilder;
    private static ITestContext testContext;
    private static ISuite suite;

    private static Schema kmlSchema;

    public VerifyKmlDocumentTests() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        testContext = mock(ITestContext.class);
        suite = mock(ISuite.class);
        when(testContext.getSuite()).thenReturn(suite);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        docBuilder = dbf.newDocumentBuilder();
        kmlSchema = ValidationUtils.createKMLSchema();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test(expected = NullPointerException.class)
    public void supplyNullTestSubject() {
        KmlDocumentTests iut = new KmlDocumentTests();
        iut.setTestSubject(null);
        iut.verifyDocumentElement();
    }

    @Test
    public void testNonKMLDocIsInvalid() throws SAXException, IOException {
        thrown.expect(AssertionError.class);
        thrown.expectMessage("schema validation error(s) detected");
        Document doc = docBuilder.parse(this.getClass().getResourceAsStream(
                "/atom-feed.xml"));
        when(suite.getAttribute(SUBJ)).thenReturn(doc);
        when(suite.getAttribute(SuiteAttribute.KML_SCHEMA.getName()))
                .thenReturn(kmlSchema);
        KmlDocumentTests iut = new KmlDocumentTests();
        iut.obtainTestSubject(testContext);
        iut.verifyXmlSchemaConstraints(testContext);
    }

    @Test
    public void testValidDoc_KML_Samples() throws SAXException, IOException {
        Document doc = docBuilder.parse(this.getClass().getResourceAsStream(
                "/kml/KML_Samples.kml"));
        when(suite.getAttribute(SUBJ)).thenReturn(doc);
        when(suite.getAttribute(SuiteAttribute.KML_SCHEMA.getName()))
                .thenReturn(kmlSchema);
        KmlDocumentTests iut = new KmlDocumentTests();
        iut.setTestSubject(doc);
        iut.verifyXmlSchemaConstraints(testContext);
    }

    @Test
    public void emptyObjectWithoutIdShouldFail() throws SAXException,
            IOException {
        thrown.expect(AssertionError.class);
        thrown.expectMessage("is empty and does not have an @id attribute");
        Document doc = docBuilder.parse(this.getClass().getResourceAsStream(
                "/kml/emptyPlacemarkWithoutId.xml"));
        when(suite.getAttribute(SUBJ)).thenReturn(doc);
        when(suite.getAttribute(SuiteAttribute.KML_SCHEMA.getName()))
                .thenReturn(kmlSchema);
        KmlDocumentTests iut = new KmlDocumentTests();
        iut.setTestSubject(doc);
        iut.verifyEmptyObjectHasId(testContext);
    }
}

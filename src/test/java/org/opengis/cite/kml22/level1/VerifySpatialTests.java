package org.opengis.cite.kml22.level1;

import java.io.IOException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opengis.cite.kml22.Namespaces;
import org.opengis.cite.kml22.SuiteAttribute;
import org.opengis.cite.validation.ValidationErrorHandler;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.testng.ISuite;
import org.testng.ITestContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Verifies the behavior of the test class SpatialTests.
 */
public class VerifySpatialTests {

    private static final String SUBJ = SuiteAttribute.TEST_SUBJECT.getName();
    private static DocumentBuilder docBuilder;
    private static ITestContext testContext;
    private static ISuite suite;
    private ValidationErrorHandler errHandler;
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    public VerifySpatialTests() {
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

    @Before
    public void initErrorHandler() {
        this.errHandler = new ValidationErrorHandler();
    }

    @After
    public void resetErrorHandler() {
        this.errHandler.reset();
    }

    @Test
    public void checkLatRange_SouthGreaterThanNorth() throws SAXException,
            IOException {
        Document doc = docBuilder.parse(this.getClass().getResourceAsStream(
                "/kml/spatial/LatLonAltBox-SgtN.xml"));
        when(suite.getAttribute(SUBJ)).thenReturn(doc);
        SpatialTests iut = new SpatialTests();
        iut.setTestSubject(doc);
        Element box = doc.getDocumentElement();
        iut.checkLatValues(box, this.errHandler);
        assertEquals("Unexpected number of errors.", 1,
                errHandler.getErrorCount());
        assertTrue(
                "Unexpected error message.",
                errHandler.toString().contains(
                        "kml:north must be greater than kml:south"));
    }

    @Test
    public void lineStringHasTruncatedTuple() throws SAXException, IOException {
        thrown.expect(AssertionError.class);
        thrown.expectMessage("kml:LineString contains less than the required two or more coordinate tuples");
        Document doc = docBuilder.parse(this.getClass().getResourceAsStream(
                "/kml/spatial/Placemark-TruncatedLineString.xml"));
        when(suite.getAttribute(SUBJ)).thenReturn(doc);
        SpatialTests iut = new SpatialTests();
        iut.setTestSubject(doc);
        iut.verifyLineStringCoordinates();
    }

    @Test
    public void invalidPolygonBoundary() throws SAXException, IOException {
        thrown.expect(AssertionError.class);
        thrown.expectMessage("Inner boundary of Polygon is not within outer boundary");
        Document doc = docBuilder.parse(this.getClass().getResourceAsStream(
                "/kml/spatial/invalidPolygonBoundary.xml"));
        when(suite.getAttribute(SUBJ)).thenReturn(doc);
        SpatialTests iut = new SpatialTests();
        iut.setTestSubject(doc);
        iut.verifyPolygonBoundary();
    }

    @Test
    public void resourceMapIsValid() throws SAXException, IOException {
        URL url = this.getClass().getResource(
                "/kml/models/Model-ResourceMap.xml");
        Document doc = docBuilder.parse(url.toString());
        SpatialTests iut = new SpatialTests();
        iut.setTestSubject(doc);
        Element mapping = (Element) doc.getDocumentElement()
                .getElementsByTagNameNS(Namespaces.KML22, "ResourceMap")
                .item(0);
        iut.checkModelResourceMap(mapping, this.errHandler);
        assertEquals("Unexpected number of errors.", 0,
                this.errHandler.getErrorCount());
    }

    @Test
    public void resourceMapIsInvalid() throws SAXException, IOException {
        URL url = this.getClass().getResource(
                "/kml/models/Model-ResourceMap-Invalid.xml");
        Document doc = docBuilder.parse(url.toString());
        SpatialTests iut = new SpatialTests();
        iut.setTestSubject(doc);
        Element mapping = (Element) doc.getDocumentElement()
                .getElementsByTagNameNS(Namespaces.KML22, "ResourceMap")
                .item(0);
        iut.checkModelResourceMap(mapping, this.errHandler);
        assertEquals("Unexpected number of errors.", 2,
                this.errHandler.getErrorCount());
    }

    @Test
    public void textureAliasHasMissingTarget() throws SAXException, IOException {
        thrown.expect(AssertionError.class);
        thrown.expectMessage("Link referent not found");
        thrown.expectMessage("03.jpg");
        URL url = this.getClass().getResource(
                "/kml/models/Model-AliasTargetMissing.xml");
        Document doc = docBuilder.parse(url.toString());
        SpatialTests iut = new SpatialTests();
        iut.setTestSubject(doc);
        iut.verifyTextureFileAliasInModel();
    }

    @Test
    public void tessellatedLineStringHasModeClampToGround()
            throws SAXException, IOException {
        URL url = this.getClass().getResource(
                "/kml/spatial/LineString-ClampToGround.xml");
        Document doc = docBuilder.parse(url.toString());
        SpatialTests iut = new SpatialTests();
        iut.setTestSubject(doc);
        iut.verifyGeometryTesselate();
    }

    @Test
    public void tessellatedLineStringWithoutAltitudeMode() throws SAXException,
            IOException {
        URL url = this.getClass().getResource(
                "/kml/spatial/LineString-Tessellate.xml");
        Document doc = docBuilder.parse(url.toString());
        SpatialTests iut = new SpatialTests();
        iut.setTestSubject(doc);
        iut.verifyGeometryTesselate();
    }

    @Test
    public void tessellatedPolygonHasModeAbsolute() throws SAXException,
            IOException {
        thrown.expect(AssertionError.class);
        thrown.expectMessage("has kml:altitudeMode not equal to \"clampToGround\"");
        URL url = this.getClass().getResource(
                "/kml/spatial/Polygon-Absolute.xml");
        Document doc = docBuilder.parse(url.toString());
        SpatialTests iut = new SpatialTests();
        iut.setTestSubject(doc);
        iut.verifyGeometryTesselate();
    }

    @Test
    public void textureAliasInModel() throws SAXException, IOException {
        URL url = this.getClass().getResource(
                "/kml/models/model_relative_target.kml");
        Document doc = docBuilder.parse(url.toString());
        SpatialTests iut = new SpatialTests();
        iut.setTestSubject(doc);
        iut.verifyTextureFileAliasInModel();
    }
}

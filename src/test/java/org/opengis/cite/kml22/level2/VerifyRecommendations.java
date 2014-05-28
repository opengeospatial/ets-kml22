package org.opengis.cite.kml22.level2;

import java.io.IOException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.testng.ISuite;
import org.testng.ITestContext;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Verifies the behavior of the test class Recommendations.
 */
public class VerifyRecommendations {

    private static DocumentBuilder docBuilder;
    private static ITestContext testContext;
    private static ISuite suite;
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    public VerifyRecommendations() {
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
    public void httpQueryOk() throws SAXException, IOException {
        URL url = this.getClass().getResource("/kml/links/Link.xml");
        Document doc = docBuilder.parse(url.toString());
        Recommendations iut = new Recommendations();
        iut.setTestSubject(doc);
        iut.verifyHttpQuery();
    }

    @Test
    public void httpQueryIsMissingSupportedParam() throws SAXException,
            IOException {
        thrown.expect(AssertionError.class);
        thrown.expectMessage("kml:httpQuery element does not contain a supported parameter");
        URL url = this.getClass().getResource(
                "/kml/links/Link-BadHttpQuery.xml");
        Document doc = docBuilder.parse(url.toString());
        Recommendations iut = new Recommendations();
        iut.setTestSubject(doc);
        iut.verifyHttpQuery();
    }

    @Test
    public void linkRefreshModeError() throws SAXException, IOException {
        thrown.expect(AssertionError.class);
        thrown.expectMessage("invalid kml:refreshMode (not \"onInterval\")");
        URL url = this.getClass().getResource("/kml/links/Link.xml");
        Document doc = docBuilder.parse(url.toString());
        Recommendations iut = new Recommendations();
        iut.setTestSubject(doc);
        iut.verifyLinkRefresh();
    }

    @Test
    public void imagePyramidWithoutLinkHref() throws SAXException, IOException {
        thrown.expect(AssertionError.class);
        thrown.expectMessage("kml:ImagePyramid and a kml:Icon/kml:href without the required parameters");
        URL url = this.getClass().getResource(
                "/kml/features/PhotoOverlay-PyramidWithoutHref.xml");
        Document doc = docBuilder.parse(url.toString());
        Recommendations iut = new Recommendations();
        iut.setTestSubject(doc);
        iut.verifyImagePyramidInPhotoOverlay();
    }

    @Test
    public void overlayLinkParamsWithoutImagePyramid() throws SAXException,
            IOException {
        thrown.expect(AssertionError.class);
        thrown.expectMessage("contains kml:Icon/kml:href with parameters but kml:ImagePyramid not found");
        URL url = this.getClass().getResource(
                "/kml/features/PhotoOverlay-HrefParamsWithoutImagePyramid.xml");
        Document doc = docBuilder.parse(url.toString());
        Recommendations iut = new Recommendations();
        iut.setTestSubject(doc);
        iut.verifyImagePyramidInPhotoOverlay();
    }

    @Test
    public void photoOverlayOk() throws SAXException, IOException {
        URL url = this.getClass().getResource("/kml/features/PhotoOverlay.xml");
        Document doc = docBuilder.parse(url.toString());
        Recommendations iut = new Recommendations();
        iut.setTestSubject(doc);
        iut.verifyImagePyramidInPhotoOverlay();
    }

    @Test
    public void emptyFolder() throws SAXException, IOException {
        thrown.expect(AssertionError.class);
        thrown.expectMessage("KML element is empty: Folder");
        URL url = this.getClass().getResource("/kml/features/Folder-Empty.xml");
        Document doc = docBuilder.parse(url.toString());
        Recommendations iut = new Recommendations();
        iut.setTestSubject(doc);
        iut.verifyFolderNotEmpty();
    }
}

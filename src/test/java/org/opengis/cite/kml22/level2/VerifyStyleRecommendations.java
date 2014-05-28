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
 * Verifies the behavior of the test class StyleRecommendations.
 */
public class VerifyStyleRecommendations {

    private static DocumentBuilder docBuilder;
    private static ITestContext testContext;
    private static ISuite suite;
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    public VerifyStyleRecommendations() {
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
    public void styleMapPairsOk() throws SAXException, IOException {
        URL url = this.getClass().getResource("/kml/styles/StyleMap-Ok.xml");
        Document doc = docBuilder.parse(url.toString());
        StyleRecommendations iut = new StyleRecommendations();
        iut.setTestSubject(doc);
        iut.verifyStyleMapPairs();
    }

    @Test
    public void styleMapPairsHaveDuplicateKeys() throws SAXException,
            IOException {
        thrown.expect(AssertionError.class);
        thrown.expectMessage("missing one or more required kml:Pair elements (with kml:key of \"normal\" and \"highlight\")");
        URL url = this.getClass().getResource(
                "/kml/styles/StyleMap-DuplicateKeys.xml");
        Document doc = docBuilder.parse(url.toString());
        StyleRecommendations iut = new StyleRecommendations();
        iut.setTestSubject(doc);
        iut.verifyStyleMapPairs();
    }
}

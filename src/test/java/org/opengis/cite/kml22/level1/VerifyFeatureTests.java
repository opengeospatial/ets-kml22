package org.opengis.cite.kml22.level1;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opengis.cite.kml22.util.KMLUtils;
import org.testng.ISuite;
import org.testng.ITestContext;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Verifies the behavior of the test class FeatureTests.
 */
public class VerifyFeatureTests {

	private static DocumentBuilder docBuilder;

	private static ITestContext testContext;

	private static ISuite suite;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	public VerifyFeatureTests() {
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
	public void placemarkWithAuthorOk() throws SAXException, IOException {
		URL url = this.getClass().getResource("/kml/features/Placemark-Author.xml");
		Document doc = docBuilder.parse(url.toString());
		FeatureTests iut = new FeatureTests();
		iut.setTestSubject(doc);
		iut.verifyAtomAuthor();
	}

	@Test
	public void regionInMainDocOk() throws URISyntaxException, IOException, SAXException {
		URL kmzUrl = this.getClass().getResource("/kmz/small_world.kmz");
		File kmzFile = new File(kmzUrl.toURI());
		Document kmlDoc = KMLUtils.extractKMLFromArchive(kmzFile);
		FeatureTests iut = new FeatureTests();
		iut.setTestSubject(kmlDoc);
		iut.verifyRegion();
	}

}

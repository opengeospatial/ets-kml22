package org.opengis.cite.kml22.level1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opengis.cite.kml22.KML22;
import org.opengis.cite.kml22.Namespaces;
import org.opengis.cite.kml22.SuiteAttribute;
import org.opengis.cite.validation.ValidationErrorHandler;
import org.testng.ISuite;
import org.testng.ITestContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Verifies the behavior of the test class LinkTests.
 */
public class VerifyLinkTests {

	private static final String SUBJ = SuiteAttribute.TEST_SUBJECT.getName();
	private static DocumentBuilder docBuilder;
	private static ITestContext testContext;
	private static ISuite suite;
	private ValidationErrorHandler errHandler;
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	public VerifyLinkTests() {
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
	public void checkNetworkLinkTargetAsRelativeUri_NoErrors()
			throws SAXException, IOException {
		URL url = this.getClass().getResource(
				"/kml/links/NetworkLink-relativeRef.xml");
		Document doc = docBuilder.parse(url.toString());
		when(suite.getAttribute(SUBJ)).thenReturn(doc);
		LinkTests iut = new LinkTests();
		iut.setTestSubject(doc);
		Node linkNode = doc.getDocumentElement()
				.getElementsByTagNameNS(Namespaces.KML22, "Link").item(0);
		URI uriRef = URI.create(iut.getLinkHref((Element) linkNode,
				this.errHandler));
		iut.checkNetworkLinkReferent(linkNode, uriRef, this.errHandler);
		assertFalse("Expected no errors\n" + this.errHandler.toString(),
				this.errHandler.errorsDetected());
	}

	@Test
	@Ignore("Passes, but skip to avoid network connection.")
	public void verifyLinkReferent_NotFound() throws SAXException, IOException {
		thrown.expect(AssertionError.class);
		thrown.expectMessage("Link element does not refer to a KML or KMZ resource");
		URL url = this.getClass().getResource(
				"/kml/links/NetworkLink-NotFound.xml");
		Document doc = docBuilder.parse(url.toString());
		when(suite.getAttribute(SUBJ)).thenReturn(doc);
		LinkTests iut = new LinkTests();
		iut.setTestSubject(doc);
		iut.verifyLinkReferent();
	}

	@Test
	@Ignore("Passes, but skip to avoid network connection.")
	public void verifyGroundOverlayWithHttpLink() throws SAXException,
			IOException {
		URL url = this.getClass().getResource(
				"/kml/links/GroundOverlay-httpRef.xml");
		Document doc = docBuilder.parse(url.toString());
		when(suite.getAttribute(SUBJ)).thenReturn(doc);
		LinkTests iut = new LinkTests();
		iut.setTestSubject(doc);
		Element icon = (Element) doc.getDocumentElement()
				.getElementsByTagNameNS(Namespaces.KML22, "Icon").item(0);
		String uriRef = iut.getLinkHref((Element) icon, this.errHandler);
		iut.checkOverlayIconReferent(icon, uriRef, this.errHandler);
		assertEquals("Unexpected number of errors.", 0,
				this.errHandler.getErrorCount());
	}

	@Test
	public void readJPEGImageFromFile() throws IOException, URISyntaxException {
		URL url = this.getClass().getResource("/img/photo-1.jpg");
		LinkTests iut = new LinkTests();
		BufferedImage img = iut.readImageDataFromURI(url.toURI());
		assertNotNull(
				"Failed to create BufferedImage with data from " + url.toURI(),
				img);
		assertEquals("Unexpected image height.", 93, img.getHeight());
	}

	@Test
	@Ignore("Passes, but skip to avoid network connection.")
	public void readPNGImageFromURL() throws IOException, URISyntaxException {
		URL url = new URL("http://www.w3.org/Icons/valid-html401");
		LinkTests iut = new LinkTests();
		iut.initHttpClient();
		BufferedImage img = iut.readImageDataFromURI(url.toURI());
		assertNotNull(
				"Failed to create BufferedImage with data from " + url.toURI(),
				img);
		assertEquals("Unexpected image height.", 31, img.getHeight());
	}

	@Test
	@Ignore("Passes, but skip to avoid network connection.")
	public void readImageFromURLWithRedirect() throws IOException,
			URISyntaxException {
		URL url = new URL(
				"http://developers.google.com/kml/documentation/images/etna.jpg");
		LinkTests iut = new LinkTests();
		iut.initHttpClient();
		BufferedImage img = iut.readImageDataFromURI(url.toURI());
		assertNotNull(
				"Failed to create BufferedImage with data from " + url.toURI(),
				img);
		assertEquals("Unexpected image height.", 418, img.getHeight());
	}

	@Test
	public void changePlacemark() throws SAXException, IOException {
		URL url = this.getClass()
				.getResource("/kml/links/Update-Placemark.xml");
		Document doc = docBuilder.parse(url.toString());
		LinkTests iut = new LinkTests();
		iut.setTestSubject(doc);
		iut.verifyUpdateTargetExists();
	}

	@Test
	public void updateTargetDoesNotExist() throws SAXException, IOException {
		thrown.expect(AssertionError.class);
		thrown.expectMessage("Failed to parse KML resource");
		thrown.expectMessage("does-not-exist.kml");
		URL url = this.getClass().getResource("/kml/links/Update-NoTarget.xml");
		Document doc = docBuilder.parse(url.toString());
		LinkTests iut = new LinkTests();
		iut.setTestSubject(doc);
		iut.verifyUpdateTargetExists();
	}

	@Test
	public void checkOverlayIconReferentWithImagePyramid() throws SAXException,
			IOException {
		URL url = this.getClass().getResource("/kml/features/PhotoOverlay.xml");
		Document doc = docBuilder.parse(url.toString());
		Element iconElem = (Element) doc.getElementsByTagNameNS(KML22.NS_NAME,
				"Icon").item(0);
		LinkTests iut = new LinkTests();
		iut.setTestSubject(doc);
		String uriRef = iut.getLinkHref((Element) iconElem, this.errHandler);
		iut.checkOverlayIconReferent(iconElem, uriRef, errHandler);
		assertEquals("Unexpected number of errors detected.", 0,
				this.errHandler.getErrorCount());
	}

	@Test
	public void checkOverlayIconReferentIsNotImage() throws SAXException,
			IOException {
		URL url = this.getClass().getResource(
				"/kml/features/PhotoOverlay-NotImage.xml");
		Document doc = docBuilder.parse(url.toString());
		Element iconElem = (Element) doc.getElementsByTagNameNS(KML22.NS_NAME,
				"Icon").item(0);
		LinkTests iut = new LinkTests();
		iut.setTestSubject(doc);
		String uriRef = iut.getLinkHref((Element) iconElem, this.errHandler);
		iut.checkOverlayIconReferent(iconElem, uriRef, errHandler);
		assertEquals("Unexpected number of errors detected.", 1,
				this.errHandler.getErrorCount());
		assertThat(this.errHandler.toString(),
				CoreMatchers.containsString("Failed to read image data"));
	}

	@Test
	public void verifyParameterizedLinkInPhotoOverlay() throws SAXException,
			IOException {
		URL url = this.getClass().getResource("/kml/features/PhotoOverlay.xml");
		Document doc = docBuilder.parse(url.toString());
		LinkTests iut = new LinkTests();
		iut.setTestSubject(doc);
		iut.verifyLinkReferent();
	}
}

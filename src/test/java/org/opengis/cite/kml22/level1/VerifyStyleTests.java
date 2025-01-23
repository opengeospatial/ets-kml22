package org.opengis.cite.kml22.level1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opengis.cite.kml22.Namespaces;
import org.opengis.cite.kml22.SuiteAttribute;
import org.opengis.cite.validation.ValidationErrorHandler;
import org.testng.ISuite;
import org.testng.ITestContext;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Verifies the behavior of the test class StyleTests. Test stubs replace fixture
 * constituents where appropriate.
 */
public class VerifyStyleTests {

	private static final String SUBJ = SuiteAttribute.TEST_SUBJECT.getName();

	private static DocumentBuilder docBuilder;

	private static ITestContext testContext;

	private static ISuite suite;

	private ValidationErrorHandler errHandler;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	public VerifyStyleTests() {
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
	public void checkStyleTargetAsSameDocFragment_NoErrors() throws SAXException, IOException {
		Document doc = docBuilder.parse(this.getClass().getResourceAsStream("/kml/KML_Samples.kml"));
		when(suite.getAttribute(SUBJ)).thenReturn(doc);
		assertNoErrors(doc);
	}

	@Test
	@Ignore("Passes, but skip to avoid network connection.")
	public void checkStyleTargetAsHttpRef_NoErrors() throws SAXException, IOException {
		Document doc = docBuilder.parse("https://developers.google.com/kml/documentation/KML_Samples.kml");
		when(suite.getAttribute(SUBJ)).thenReturn(doc);
		assertNoErrors(doc);
	}

	@Test
	public void checkStyleTargetAsFileRef_NoErrors() throws SAXException, IOException {
		URL url = this.getClass().getResource("/kml/styles/styleUrl-fileRef.xml");
		Document doc = docBuilder.parse(url.toString());
		when(suite.getAttribute(SUBJ)).thenReturn(doc);
		assertNoErrors(doc);
	}

	@Test
	public void checkStyleTargetAsFileRef_OneError() throws SAXException, IOException {
		URL url = this.getClass().getResource("/kml/styles/styleUrl-fileRef.xml");
		Document doc = docBuilder.parse(url.toString());
		when(suite.getAttribute(SUBJ)).thenReturn(doc);
		StyleTests iut = new StyleTests();
		iut.setTestSubject(doc);
		Node styleUrl = doc.getDocumentElement().getElementsByTagNameNS(Namespaces.KML22, "styleUrl").item(1);
		iut.checkStyleTarget(styleUrl, this.errHandler);
		assertEquals("Unexpected number of errors.", 1, errHandler.getErrorCount());
	}

	@Test
	public void styleMapOk() throws SAXException, IOException {
		URL url = this.getClass().getResource("/kml/styles/StyleMap-Ok.xml");
		Document doc = docBuilder.parse(url.toString());
		StyleTests iut = new StyleTests();
		iut.setTestSubject(doc);
		iut.verifyStyleMap();
		assertFalse("Expected no errors\n" + this.errHandler.toString(), this.errHandler.errorsDetected());
	}

	@Test
	public void styleMapPairIsMissingStyle() throws SAXException, IOException {
		thrown.expect(AssertionError.class);
		thrown.expectMessage("kml:Pair is missing one or more required elements");
		URL url = this.getClass().getResource("/kml/styles/StyleMap-Error.xml");
		Document doc = docBuilder.parse(url.toString());
		StyleTests iut = new StyleTests();
		iut.setTestSubject(doc);
		iut.verifyStyleMap();
	}

	private void assertNoErrors(Document doc) {
		StyleTests iut = new StyleTests();
		iut.setTestSubject(doc);
		Node styleUrl = doc.getDocumentElement().getElementsByTagNameNS(Namespaces.KML22, "styleUrl").item(0);
		iut.checkStyleTarget(styleUrl, this.errHandler);
		assertFalse("Expected no errors\n" + this.errHandler.toString(), this.errHandler.errorsDetected());
	}

}

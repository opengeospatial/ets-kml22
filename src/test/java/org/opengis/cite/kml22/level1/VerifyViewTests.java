package org.opengis.cite.kml22.level1;

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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opengis.cite.validation.ValidationErrorHandler;
import org.testng.ISuite;
import org.testng.ITestContext;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Verifies the behavior of the test class ViewTests.
 */
public class VerifyViewTests {

	private static DocumentBuilder docBuilder;

	private static ITestContext testContext;

	private static ISuite suite;

	private ValidationErrorHandler errHandler;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	public VerifyViewTests() {
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
	public void validLookAt() throws SAXException, IOException {
		URL url = this.getClass().getResource("/kml/views/LookAt-Ok.xml");
		Document doc = docBuilder.parse(url.toString());
		ViewTests iut = new ViewTests();
		iut.setTestSubject(doc);
		iut.verifyLookAt();
		assertFalse("Expected no errors\n" + this.errHandler.toString(), this.errHandler.errorsDetected());
	}

	@Test
	public void lookAtIsMissingAltitude() throws SAXException, IOException {
		thrown.expect(AssertionError.class);
		thrown.expectMessage(
				"kml:LookAt is missing required kml:altitude element when kml:altitudeMode != \"clampToGround\"");
		URL url = this.getClass().getResource("/kml/views/LookAt-Error.xml");
		Document doc = docBuilder.parse(url.toString());
		ViewTests iut = new ViewTests();
		iut.setTestSubject(doc);
		iut.verifyLookAt();
	}

	@Test
	public void lookAtIsMissingAltitudeMode() throws SAXException, IOException {
		URL url = this.getClass().getResource("/kml/views/LookAt-MissingAltitudeMode.xml");
		Document doc = docBuilder.parse(url.toString());
		ViewTests iut = new ViewTests();
		iut.setTestSubject(doc);
		iut.verifyLookAt();
	}

	@Test
	public void lookAtIsMissingAltitudeModeAndAltitude() throws SAXException, IOException {
		URL url = this.getClass().getResource("/kml/views/LookAt-MissingAltitudeModeAndAltitude.xml");
		Document doc = docBuilder.parse(url.toString());
		ViewTests iut = new ViewTests();
		iut.setTestSubject(doc);
		iut.verifyLookAt();
	}

	@Test
	public void lookAt_clampToGroundNoAltitude() throws SAXException, IOException {
		URL url = this.getClass().getResource("/kml/views/LookAt-ClampToGround.xml");
		Document doc = docBuilder.parse(url.toString());
		ViewTests iut = new ViewTests();
		iut.setTestSubject(doc);
		iut.verifyLookAt();
	}

}

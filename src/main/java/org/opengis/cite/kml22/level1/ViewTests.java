package org.opengis.cite.kml22.level1;

import java.util.logging.Level;

import javax.xml.xpath.XPathExpressionException;

import org.opengis.cite.kml22.BaseFixture;
import org.opengis.cite.kml22.ETSAssert;
import org.opengis.cite.kml22.ErrorMessage;
import org.opengis.cite.kml22.Namespaces;
import org.opengis.cite.kml22.util.TestSuiteLogger;
import org.opengis.cite.kml22.util.XMLUtils;
import org.opengis.cite.validation.ErrorLocator;
import org.opengis.cite.validation.ErrorSeverity;
import org.opengis.cite.validation.ValidationErrorHandler;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * <p>
 * Contains test methods that apply to view elements (kml:Camera, kml:LookAt).
 * </p>
 *
 * <h2 style="margin-bottom: 0.5em">Sources</h2>
 * <ul>
 * <li>OGC 07-134r2: OGC KML 2.2 - Abstract Test Suite</li>
 * <li><a href= "https://developers.google.com/kml/documentation/kmlreference#camera"
 * target="_blank">KML Reference - Camera</a></li>
 * <li><a href= "https://developers.google.com/kml/documentation/kmlreference#lookat"
 * target="_blank">KML Reference - LookAt</a></li>
 * </ul>
 */
public class ViewTests extends BaseFixture {

	/**
	 * [{@code Test}] Verifies that a kml:LookAt element satisfies all of the following
	 * constraints:
	 *
	 * <ol>
	 * <li>if it is not a descendant of kml:Update, it contains all of the following child
	 * elements: kml:longitude, kml:latitude, and kml:range;</li>
	 * <li>0 &lt;= kml:tilt &lt;= 90;</li>
	 * <li>if kml:altitudeMode does not have the value "clampToGround", then the
	 * kml:altitude element is present.</li>
	 * </ol>
	 *
	 * @see "OGC 07-134r2, ATC 38: LookAt"
	 */
	@Test(description = "Implements ATC 38")
	public void verifyLookAt() {
		NodeList lookAtNodes = null;
		try {
			lookAtNodes = XMLUtils.evaluateXPath(this.testSubject, "//kml:LookAt[not(ancestor::kml:Update)]", NS_MAP);
		}
		catch (XPathExpressionException xpe) {
			TestSuiteLogger.log(Level.WARNING, "Error evaluating XPath expression", xpe);
		}
		ValidationErrorHandler errHandler = new ValidationErrorHandler();
		for (int i = 0; i < lookAtNodes.getLength(); i++) {
			Element lookAt = (Element) lookAtNodes.item(i);
			try {
				ETSAssert.assertXPath("kml:longitude and kml:latitude and kml:range", lookAt, NS_MAP);
			}
			catch (AssertionError e) {
				errHandler.addError(ErrorSeverity.ERROR, ErrorMessage.format("level1.LookAt.err1"),
						new ErrorLocator(-1, -1, XMLUtils.getXPointer(lookAt)));
			}
			try {
				ETSAssert.assertXPath("not(kml:altitudeMode) or (kml:altitudeMode = 'clampToGround') or kml:altitude",
						lookAt, NS_MAP);
			}
			catch (AssertionError e) {
				errHandler.addError(ErrorSeverity.ERROR, ErrorMessage.format("level1.LookAt.err4"),
						new ErrorLocator(-1, -1, XMLUtils.getXPointer(lookAt)));
			}
			checkTiltRange(lookAt, errHandler);
		}
		Assert.assertFalse(errHandler.errorsDetected(), errHandler.toString());
	}

	/**
	 * An error is reported if the value of the kml:tilt element (of type
	 * kml:anglepos180Type) is outside the valid range 0-90.
	 * @param lookAt A kml:LookAt element.
	 * @param errHandler The error handler that accepts a reported constraint violation.
	 */
	void checkTiltRange(Element lookAt, ValidationErrorHandler errHandler) {
		Node tiltNode = lookAt.getElementsByTagNameNS(Namespaces.KML22, "tilt").item(0);
		if (null != tiltNode) {
			double tilt = Double.parseDouble(tiltNode.getTextContent());
			if (tilt < 0 || tilt > 90) {
				errHandler.addError(ErrorSeverity.ERROR, ErrorMessage.format("level1.LookAt.err3"),
						new ErrorLocator(-1, -1, XMLUtils.getXPointer(lookAt)));
			}
		}
	}

}

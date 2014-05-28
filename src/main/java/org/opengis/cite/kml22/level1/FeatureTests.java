package org.opengis.cite.kml22.level1;

import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPathExpressionException;

import org.apache.jena.iri.IRI;
import org.apache.jena.iri.IRIException;
import org.apache.jena.iri.IRIFactory;
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
 * Contains test methods that verify constraints applicable to KML features,
 * including:
 * 
 * <ul>
 * <li>Document</li>
 * <li>Folder</li>
 * <li>NetworkLink</li>
 * <li>Placemark</li>
 * <li>GroundOverlay</li>
 * <li>PhotoOverlay</li>
 * <li>ScreenOverlay</li>
 * </ul>
 * 
 * <h3 style="margin-bottom: 0.5em">Sources</h3>
 * <ul>
 * <li>OGC 07-134r2: OGC KML 2.2 - Abstract Test Suite</li>
 * </ul>
 */
public class FeatureTests extends BaseFixture {

    /**
     * [{@code Test}] Verifies that a kml:ViewVolume element (in
     * kml:GroundOverlay) includes at least the following child elements:
     * kml:leftFov, kml:rightFov, kml:bottomFov, kml:topFov, and kml:near
     * (non-negative value).
     * 
     * @see "OGC 07-134r2, ATC 19: ViewVolume - minimal content"
     * @see <a href=
     *      "https://developers.google.com/kml/documentation/kmlreference#photooverlay"
     *      target="_blank">KML Reference - PhotoOverlay</a>
     */
    @Test(description = "Implements ATC 19")
    public void verifyViewVolumeContent() {
        NodeList viewVolNodes = this.testSubject.getElementsByTagNameNS(
                Namespaces.KML22, "ViewVolume");
        String[] requiredElems = { "leftFov", "rightFov", "bottomFov",
                "topFov", "near" };
        ValidationErrorHandler errHandler = new ValidationErrorHandler();
        for (int i = 0; i < viewVolNodes.getLength(); i++) {
            Element viewVolume = (Element) viewVolNodes.item(i);
            for (String elemName : requiredElems) {
                if (viewVolume.getElementsByTagNameNS(Namespaces.KML22,
                        elemName).getLength() == 0) {
                    errHandler.addError(
                            ErrorSeverity.ERROR,
                            ErrorMessage.format("level1.ViewVolume.err1",
                                    elemName),
                            new ErrorLocator(-1, -1, XMLUtils
                                    .getXPointer(viewVolume)));
                }
            }
            if (Double.valueOf(viewVolume
                    .getElementsByTagNameNS(Namespaces.KML22, "near").item(0)
                    .getTextContent().trim()) < 0) {
                errHandler.addError(ErrorSeverity.ERROR, ErrorMessage
                        .format("level1.ViewVolume.err3"), new ErrorLocator(-1,
                        -1, XMLUtils.getXPointer(viewVolume)));
            }
        }
        Assert.assertFalse(errHandler.errorsDetected(), errHandler.toString());
    }

    /**
     * [{@code Test}] Verifies that the value of the kml:phoneNumber element is
     * a 'tel' URI that conforms to RFC 3966.
     * 
     * @see "OGC 07-134r2, ATC 24: PhoneNumber"
     * @see <a href= "http://tools.ietf.org/html/rfc3966" target="_blank">RFC
     *      3966: The tel URI for Telephone Numbers</a>
     */
    @Test(description = "Implements ATC 24")
    public void verifyPhoneNumberSyntax() {
        NodeList phoneNums = this.testSubject.getElementsByTagNameNS(
                Namespaces.KML22, "phoneNumber");
        ValidationErrorHandler errHandler = new ValidationErrorHandler();
        for (int i = 0; i < phoneNums.getLength(); i++) {
            Node phoneNumber = phoneNums.item(i);
            String phoneNumberTxt = phoneNumber.getTextContent();
            Pattern phoneNumberPattern = Pattern
                    .compile("tel:(\\+)?(\\d*([-.()])?)+(\\d{3}?[-.()])?(\\d{3}[-.()])?(\\d{4,10})?([;].*)?");
            Matcher matcher = phoneNumberPattern.matcher(phoneNumberTxt);
            if (!matcher.matches()) {
                errHandler.addError(ErrorSeverity.ERROR, ErrorMessage
                        .format("level1.PhoneNumber.err"), new ErrorLocator(-1,
                        -1, XMLUtils.getXPointer(phoneNumber)));
            }
        }
        Assert.assertFalse(errHandler.errorsDetected(), errHandler.toString());
    }

    /**
     * [{@code Test}] Verifies that that an atom:author element satisfies all of
     * the following constraints:
     * 
     * <ol>
     * <li>the content of the atom:uri element is an IRI reference;</li>
     * <li>the content of the atom:email element conforms to the "addr-spec"
     * production in RFC 2822.</li>
     * </ol>
     * 
     * <h3 style="margin-bottom: 0.5em">Sources</h3>
     * <ul>
     * <li>OGC 07-134r2, ATC 30: atom:author</li>
     * <li><a href= "http://tools.ietf.org/html/rfc4287#section-3.2"
     * target="_blank">RFC 4287: Person Constructs</a></li>
     * <li><a href= "http://tools.ietf.org/html/rfc3987" target="_blank">RFC
     * 3987: Internationalized Resource Identifiers (IRIs)</a></li>
     * <li><a href= "http://tools.ietf.org/html/rfc2822#section-3.4.1"
     * target="_blank">RFC 2822: Addr-spec specification</a></li>
     * </ul>
     */
    @Test(description = "Implements ATC 30")
    public void verifyAtomAuthor() {
        NodeList authorNodes = this.testSubject.getElementsByTagNameNS(
                Namespaces.ATOM, "author");
        ValidationErrorHandler errHandler = new ValidationErrorHandler();
        for (int i = 0; i < authorNodes.getLength(); i++) {
            Element author = (Element) authorNodes.item(i);
            Node uri = author.getElementsByTagNameNS(Namespaces.ATOM, "uri")
                    .item(0);
            try {
                if (null != uri) {
                    assertValidIRI(uri.getTextContent().trim());
                }
            } catch (AssertionError e) {
                errHandler.addError(ErrorSeverity.ERROR, ErrorMessage.format(
                        "level1.AtomAuthor.err1", e.getMessage()),
                        new ErrorLocator(-1, -1, XMLUtils.getXPointer(author)));
            }
            Node email = author
                    .getElementsByTagNameNS(Namespaces.ATOM, "email").item(0);
            try {
                if (null != email) {
                    assertValidEmailAddr(email.getTextContent().trim());
                }
            } catch (AssertionError e) {
                errHandler.addError(ErrorSeverity.ERROR, ErrorMessage.format(
                        "level1.AtomAuthor.err2", e.getMessage()),
                        new ErrorLocator(-1, -1, XMLUtils.getXPointer(author)));
            }
        }
        Assert.assertFalse(errHandler.errorsDetected(), errHandler.toString());
    }

    /**
     * [{@code Test}] Verifies that the value of the atom:link/@rel attribute is
     * "related". If the attribute is not present, the link must be interpreted
     * as if {@code @rel="alternate"}; that is, the referent is an alternate
     * version of the resource. The value "related" simply signifies a generic
     * relationship.
     * 
     * @see "OGC 07-134r2, ATC 31: atom:link"
     */
    @Test(description = "Implements ATC 31")
    public void verifyAtomLink() {
        NodeList linkNodes = this.testSubject.getElementsByTagNameNS(
                Namespaces.ATOM, "link");
        ValidationErrorHandler errHandler = new ValidationErrorHandler();
        for (int i = 0; i < linkNodes.getLength(); i++) {
            Element link = (Element) linkNodes.item(i);
            String rel = link.getAttribute("rel");
            if (!rel.equals("related")) {
                errHandler.addError(ErrorSeverity.ERROR,
                        ErrorMessage.format("level1.AtomLink.err"),
                        new ErrorLocator(-1, -1, XMLUtils.getXPointer(link)));
            }
        }
        Assert.assertFalse(errHandler.errorsDetected(), errHandler.toString());
    }

    /**
     * [{@code Test}] Verifies that a kml:GroundOverlay feature with an
     * kml:altitudeMode value of "absolute" includes a kml:altitude element.
     * 
     * @see "OGC 07-134r2, ATC 33: GroundOverlay"
     */
    @Test(description = "Implements ATC 33")
    public void verifyAltitudeInGroundOverlay() {
        NodeList grndOverlayAltModeNodes = null;
        try {
            grndOverlayAltModeNodes = XMLUtils.evaluateXPath(this.testSubject,
                    "//kml:GroundOverlay/kml:altitudeMode", NS_MAP);
        } catch (XPathExpressionException xpe) {
            TestSuiteLogger.log(Level.WARNING,
                    "Error evaluating XPath expression", xpe);
        }
        ValidationErrorHandler errHandler = new ValidationErrorHandler();
        for (int i = 0; i < grndOverlayAltModeNodes.getLength(); i++) {
            Node altitudeMode = grndOverlayAltModeNodes.item(i);
            // previous sibling is kml:altitude if present
            Node prevSibling = altitudeMode.getPreviousSibling();
            if (altitudeMode.getTextContent().trim().equals("absolute")
                    && !prevSibling.getLocalName().equals("altitude")) {
                errHandler.addError(
                        ErrorSeverity.ERROR,
                        ErrorMessage.format("level1.GroundOverlay.err"),
                        new ErrorLocator(-1, -1, XMLUtils
                                .getXPointer(altitudeMode.getParentNode())));
            }
        }
        Assert.assertFalse(errHandler.errorsDetected(), errHandler.toString());
    }

    /**
     * [{@code Test}] Verifies that if a kml:PhotoOverlay element is not a
     * descendant of kml:Update, then it includes all of the following child
     * elements: kml:Icon, kml:ViewVolume, kml:Point, and kml:Camera.
     * 
     * @see "OGC 07-134r2, ATC 35: PhotoOverlay - minimal content"
     */
    @Test(description = "Implements ATC 35")
    public void verifyPhotoOverlay() {
        NodeList photoOverlays = null;
        try {
            photoOverlays = XMLUtils.evaluateXPath(this.testSubject,
                    "//kml:PhotoOverlay[not(ancestor::kml:Update)]", NS_MAP);
        } catch (XPathExpressionException xpe) {
            TestSuiteLogger.log(Level.WARNING,
                    "Error evaluating XPath expression", xpe);
        }
        ValidationErrorHandler errHandler = new ValidationErrorHandler();
        String[] requiredElems = { "Icon", "ViewVolume", "Point", "Camera" };
        for (int i = 0; i < photoOverlays.getLength(); i++) {
            Element photoOverlay = (Element) photoOverlays.item(i);
            for (String elemName : requiredElems) {
                if (photoOverlay.getElementsByTagNameNS(Namespaces.KML22,
                        elemName).getLength() == 0) {
                    errHandler
                            .addError(ErrorSeverity.ERROR,
                                    ErrorMessage.format(
                                            "level1.PhotoOverlayMinimal.err",
                                            elemName), new ErrorLocator(-1, -1,
                                            XMLUtils.getXPointer(photoOverlay)));
                }
            }
        }
        Assert.assertFalse(errHandler.errorsDetected(), errHandler.toString());
    }

    /**
     * [{@code Test}] Verifies that if a kml:Region element is not a descendant
     * of kml:Update, it contains the kml:LatLonAltBox and kml:Lod elements.
     * 
     * @see "OGC 07-134r2, ATC 41: Region"
     */
    @Test(description = "Implements ATC 41")
    public void verifyRegion() {
        NodeList regionNodes = null;
        try {
            regionNodes = XMLUtils.evaluateXPath(this.testSubject,
                    "//kml:Region[not(ancestor::kml:Update)]", NS_MAP);
        } catch (XPathExpressionException xpe) {
            TestSuiteLogger.log(Level.WARNING,
                    "Error evaluating XPath expression", xpe);
        }
        ValidationErrorHandler errHandler = new ValidationErrorHandler();
        for (int i = 0; i < regionNodes.getLength(); i++) {
            Node region = regionNodes.item(i);
            try {
                ETSAssert.assertXPath("kml:LatLonAltBox and kml:Lod", region,
                        NS_MAP);
            } catch (AssertionError e) {
                errHandler.addError(ErrorSeverity.ERROR, ErrorMessage.format(
                        "level1.Region.err", e.getMessage()), new ErrorLocator(
                        -1, -1, XMLUtils.getXPointer(region)));
            }
        }
        Assert.assertFalse(errHandler.errorsDetected(), errHandler.toString());
    }

    /**
     * [{@code Test}] Verifies that a kml:Lod element (with parent kml:Region)
     * satisfies all of the following constraints:
     * 
     * <ol>
     * <li>if it is not a descendant of kml:Update, it contains the
     * kml:minLodPixels element;</li>
     * <li>kml:minLodPixels &lt; kml:maxLodPixels (where a value of -1 denotes
     * positive infinity).</li>
     * </ol>
     * 
     * @see "OGC 07-134r2, ATC 39: Lod"
     */
    @Test(description = "Implements ATC 39")
    public void verifyRegionLod() {
        NodeList lodNodes = null;
        try {
            lodNodes = XMLUtils.evaluateXPath(this.testSubject,
                    "//kml:Lod[not(ancestor::kml:Update)]", NS_MAP);
        } catch (XPathExpressionException xpe) {
            TestSuiteLogger.log(Level.WARNING,
                    "Error evaluating XPath expression", xpe);
        }
        ValidationErrorHandler errHandler = new ValidationErrorHandler();
        for (int i = 0; i < lodNodes.getLength(); i++) {
            Element lod = (Element) lodNodes.item(i);
            Node minLodPixelsNode = lod.getElementsByTagNameNS(
                    Namespaces.KML22, "minLodPixels").item(0);
            if (null == minLodPixelsNode) {
                errHandler.addError(ErrorSeverity.ERROR,
                        ErrorMessage.format("level1.Lod.err1"),
                        new ErrorLocator(-1, -1, XMLUtils.getXPointer(lod)));
                continue;
            }
            double minLodPixels = Double.parseDouble(minLodPixelsNode
                    .getTextContent());
            double maxLodPixels = Double.POSITIVE_INFINITY;
            Node maxLodPixelsNode = lod.getElementsByTagNameNS(
                    Namespaces.KML22, "maxLodPixels").item(0);
            if (null != maxLodPixelsNode) {
                Double value = Double.parseDouble(maxLodPixelsNode
                        .getTextContent().trim());
                maxLodPixels = (value < 0) ? Double.POSITIVE_INFINITY : value;
            }
            if (minLodPixels >= maxLodPixels) {
                errHandler.addError(ErrorSeverity.ERROR,
                        ErrorMessage.format("level1.Lod.err3"),
                        new ErrorLocator(-1, -1, XMLUtils.getXPointer(lod)));
            }
        }
        Assert.assertFalse(errHandler.errorsDetected(), errHandler.toString());
    }

    /**
     * Asserts that a given email address conforms to the "addr-spec" production
     * in RFC 5322.
     * 
     * @param emailAddr
     *            A String denoting an email address.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc5322#section-3.4.1"
     *      target="_blank">RFC 5322: Internet Message Format - Addr-Spec
     *      Specification</a>
     */
    void assertValidEmailAddr(String emailAddr) {
        Pattern emailPattern = Pattern.compile("(.+)@(.+)\\.(.+)");
        Matcher matcher = emailPattern.matcher(emailAddr);
        if (!matcher.matches()) {
            throw new AssertionError("Not a valid address: " + emailAddr);
        }
    }

    /**
     * Asserts that a resource identifier is a valid internationalized resource
     * identifier (IRI). An IRI is a sequence of characters from the Universal
     * Character Set (Unicode/ISO 10646).
     * 
     * @param id
     *            A String representing a resource identifier.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc3987" target="_blank">RFC
     *      3987: Internationalized Resource Identifiers (IRIs)</a>
     */
    void assertValidIRI(String id) {
        try {
            @SuppressWarnings("unused")
            IRI iri = IRIFactory.iriImplementation().construct(id);
        } catch (IRIException e) {
            throw new AssertionError("Not a valid IRI reference:" + id);
        }
    }
}

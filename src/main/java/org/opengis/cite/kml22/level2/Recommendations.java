package org.opengis.cite.kml22.level2;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.opengis.cite.kml22.BaseFixture;
import org.opengis.cite.kml22.ETSAssert;
import org.opengis.cite.kml22.ErrorMessage;
import org.opengis.cite.kml22.Namespaces;
import org.opengis.cite.kml22.util.TestSuiteLogger;
import org.opengis.cite.kml22.util.ValidationUtils;
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
 * Contains test methods for various recommended constraints that should be
 * satisfied by a KML instance.
 * 
 * <h3 style="margin-bottom: 0.5em">Sources</h3>
 * <ul>
 * <li>OGC 07-134r2: OGC KML 2.2 - Abstract Test Suite</li>
 * </ul>
 */
public class Recommendations extends BaseFixture {

    static final String ALT_MODE_CLAMP = "clampToGround";
    /** List of supported httpQuery parameters. */
    static final Set<String> HTTP_QUERY_PARAMS;
    static {
        final Set<String> types = new HashSet<String>();
        types.add("[clientVersion]");
        types.add("[kmlVersion]");
        types.add("[clientName]");
        types.add("[language]");
        HTTP_QUERY_PARAMS = Collections.unmodifiableSet(types);
    }

    /**
     * [{@code Test}] Verifies that a kml:coordinates or kml:Model/kml:Location
     * element includes an altitude value if its sibling kml:altitudeMode
     * element does NOT have the value "clampToGround".
     * 
     * @see "OGC 07-134r2, ATC 43: Coordinates - altitudeMode"
     */
    @Test(description = "Implements ATC 43")
    public void verifyAltitudeIfNotClampToGround() {
        NodeList nodeList = null;
        try {
            nodeList = XMLUtils.evaluateXPath(this.testSubject,
                    "//kml:coordinates | //kml:Location", NS_MAP);
        } catch (XPathExpressionException xpe) {
            TestSuiteLogger.log(Level.WARNING,
                    "Error evaluating XPath expression", xpe);
        }
        ValidationErrorHandler errHandler = new ValidationErrorHandler();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Element elem = (Element) nodeList.item(i);
            Node altModeNode = null;
            try {
                altModeNode = (Node) XMLUtils.evaluateXPath(elem,
                        "../kml:altitudeMode", NS_MAP, XPathConstants.NODE);
            } catch (XPathExpressionException xpe) {
                throw new RuntimeException(xpe);
            }
            String altitudeMode = (null != altModeNode) ? altModeNode
                    .getTextContent().trim() : ALT_MODE_CLAMP;
            if (altitudeMode.equals(ALT_MODE_CLAMP)) {
                continue;
            }
            String localName = elem.getLocalName();
            if (localName.equals("coordinates")) {
                ValidationUtils.validateCoordinateTuples(elem, 3, errHandler);
            } else { // kml:Location
                if (elem.getElementsByTagNameNS(Namespaces.KML22, "altitude")
                        .getLength() == 0) {
                    errHandler
                            .addError(
                                    ErrorSeverity.ERROR,
                                    ErrorMessage
                                            .format("level2.CoordinatesAltitudeMode.err"),
                                    new ErrorLocator(-1, -1, XMLUtils
                                            .getXPointer(elem)));
                }
            }
        }
        Assert.assertFalse(errHandler.errorsDetected(), errHandler.toString());
    }

    /**
     * [{@code Test}] Verifies that if a kml:Scale element is not a descendant
     * of kml:Update, it contains at least one of the following elements: kml:x,
     * kml:y, or kml:z.
     * 
     * @see "OGC 07-134r2, ATC 44: Scale - minimal content"
     */
    @Test(description = "Implements ATC 44")
    public void verifyScale() {
        NodeList scaleNodes = null;
        try {
            scaleNodes = XMLUtils.evaluateXPath(this.testSubject,
                    "//kml:Scale[not(ancestor::kml:Update)]", NS_MAP);
        } catch (XPathExpressionException xpe) {
            throw new RuntimeException(xpe);
        }
        ValidationErrorHandler errHandler = new ValidationErrorHandler();
        for (int i = 0; i < scaleNodes.getLength(); i++) {
            Node scale = scaleNodes.item(i);
            try {
                ETSAssert.assertXPath("kml:x or kml:y or kml:z", scale, NS_MAP);
            } catch (AssertionError e) {
                errHandler.addError(ErrorSeverity.ERROR,
                        ErrorMessage.format("level2.ScaleMinimal.err"),
                        new ErrorLocator(-1, -1, XMLUtils.getXPointer(scale)));
            }
        }
        Assert.assertFalse(errHandler.errorsDetected(), errHandler.toString());
    }

    /**
     * [{@code Test}] Verifies that the document element (kml:kml) contains at
     * least one child element: kml:NetworkLinkControl or any element that
     * substitutes for kml:AbstractFeatureType.
     * 
     * This test checks for the occurrence of child elements in the standard
     * namespace. It is possible that extension elements in some foreign
     * namespace may be present (e.g. gx:Tour), but these are currently ignored.
     * 
     * @see "OGC 07-134r2, ATC 45: KML - minimal content"
     */
    @Test(description = "Implements ATC 45")
    public void verifyKmlDocumentIsNotEmpty() {
        Element docElement = this.testSubject.getDocumentElement();
        ETSAssert.assertXPath("kml:*", docElement, NS_MAP);
    }

    /**
     * [{@code Test}] Verifies that a kml:viewFormat element (appearing within a
     * parent link element) contains at least one parameter.
     * 
     * @see "OGC 07-134r2, ATC 46: ViewFormat"
     */
    @Test(description = "Implements ATC 46")
    public void verifyViewFormat() {
        NodeList viewFormatNodes = this.testSubject.getElementsByTagNameNS(
                Namespaces.KML22, "viewFormat");
        ValidationErrorHandler errHandler = new ValidationErrorHandler();
        for (int i = 0; i < viewFormatNodes.getLength(); i++) {
            Node viewFormat = viewFormatNodes.item(i);
            if (viewFormat.getTextContent().trim().isEmpty()) {
                errHandler.addError(ErrorSeverity.ERROR, ErrorMessage
                        .format("level2.ViewFormat.err"), new ErrorLocator(-1,
                        -1, XMLUtils.getXPointer(viewFormat)));
            }
        }
        Assert.assertFalse(errHandler.errorsDetected(), errHandler.toString());
    }

    /**
     * [{@code Test}] Verifies that a kml:httpQuery element (appearing within a
     * parent link element) contains at least one supported parameter (these are
     * listed below).
     * 
     * <ul>
     * <li>[clientVersion]</li>
     * <li>[kmlVersion]</li>
     * <li>[clientName]</li>
     * <li>[language]</li>
     * </ul>
     * 
     * @see "OGC 07-134r2, ATC 47: httpQuery"
     */
    @Test(description = "Implements ATC 47")
    public void verifyHttpQuery() {
        NodeList queryNodes = this.testSubject.getElementsByTagNameNS(
                Namespaces.KML22, "httpQuery");
        ValidationErrorHandler errHandler = new ValidationErrorHandler();
        Pattern paramsPattern = Pattern.compile("\\[[a-zA-Z]+\\]");
        Set<String> paramSet = new HashSet<String>();
        for (int i = 0; i < queryNodes.getLength(); i++) {
            paramSet.clear();
            Node queryNode = queryNodes.item(i);
            String queryParams = queryNode.getTextContent().trim();
            if (queryParams.isEmpty()) {
                errHandler.addError(ErrorSeverity.ERROR, ErrorMessage
                        .format("level2.HttpQuery.err"), new ErrorLocator(-1,
                        -1, XMLUtils.getXPointer(queryNode)));
                continue;
            }
            Matcher paramMatcher = paramsPattern.matcher(queryParams);
            while (paramMatcher.find()) {
                paramSet.add(paramMatcher.group());
            }
            // Remove all unsupported parameters
            paramSet.retainAll(HTTP_QUERY_PARAMS);
            if (paramSet.isEmpty()) {
                errHandler.addError(ErrorSeverity.ERROR, ErrorMessage
                        .format("level2.HttpQuery.err2"), new ErrorLocator(-1,
                        -1, XMLUtils.getXPointer(queryNode)));
            }
        }
        Assert.assertFalse(errHandler.errorsDetected(), errHandler.toString());
    }

    /**
     * [{@code Test}] Verifies that a kml:LinearRing element (composing the
     * boundary of a polygon) does not contain any of the following elements:
     * kml:extrude, kml:tessellate, or kml:altitudeMode.
     * 
     * @see "OGC 07-134r2, ATC 48: LinearRing in Polygon"
     */
    @Test(description = "Implements ATC 48")
    public void verifyLinearRing() {
        NodeList ringNodes = null;
        try {
            ringNodes = XMLUtils.evaluateXPath(this.testSubject,
                    "//kml:LinearRing[ancestor::kml:Polygon]", NS_MAP);
        } catch (XPathExpressionException xpe) {
            throw new RuntimeException(xpe);
        }
        ValidationErrorHandler errHandler = new ValidationErrorHandler();
        for (int i = 0; i < ringNodes.getLength(); i++) {
            Node ring = ringNodes.item(i);
            try {
                ETSAssert
                        .assertXPath(
                                "not(kml:extrude or kml:tessellate or kml:altitudeMode)",
                                ring, NS_MAP);
            } catch (AssertionError e) {
                errHandler.addError(ErrorSeverity.ERROR,
                        ErrorMessage.format("level2.LinearRingInPolygon.err"),
                        new ErrorLocator(-1, -1, XMLUtils.getXPointer(ring)));
            }
        }
        Assert.assertFalse(errHandler.errorsDetected(), errHandler.toString());
    }

    /**
     * [{@code Test}] Verifies that a kml:Data element has both a 'name'
     * attribute and a kml:value child element.
     * 
     * @see "OGC 07-134r2, ATC 49: Data"
     */
    @Test(description = "Implements ATC 49")
    public void verifyUntypedData() {
        NodeList dataNodes = this.testSubject.getElementsByTagNameNS(
                Namespaces.KML22, "Data");
        ValidationErrorHandler errHandler = new ValidationErrorHandler();
        for (int i = 0; i < dataNodes.getLength(); i++) {
            Element dataElem = (Element) dataNodes.item(i);
            NodeList valueNodes = dataElem.getElementsByTagNameNS(
                    Namespaces.KML22, "value");
            if (dataElem.getAttribute("name").isEmpty()
                    || valueNodes.getLength() == 0) {
                errHandler
                        .addError(ErrorSeverity.ERROR, ErrorMessage
                                .format("level2.Data.err"), new ErrorLocator(
                                -1, -1, XMLUtils.getXPointer(dataElem)));
            }
        }
        Assert.assertFalse(errHandler.errorsDetected(), errHandler.toString());
    }

    /**
     * [{@code Test}] Verifies that a kml:ResourceMap element contains at least
     * one kml:Alias child element, and that each Alias element has a unique
     * kml:sourceHref value.
     * 
     * @see "OGC 07-134r2, ATC 50: ResourceMap - Alias"
     */
    @Test(description = "Implements ATC 50")
    public void verifyResourceAliasIsUnique() {
        NodeList resourceMaps = this.testSubject.getElementsByTagNameNS(
                Namespaces.KML22, "ResourceMap");
        ValidationErrorHandler errHandler = new ValidationErrorHandler();
        Set<String> aliasSourceSet = new HashSet<String>();
        for (int i = 0; i < resourceMaps.getLength(); i++) {
            aliasSourceSet.clear();
            Element resourceMap = (Element) resourceMaps.item(i);
            NodeList aliasNodes = resourceMap.getElementsByTagNameNS(
                    Namespaces.KML22, "Alias");
            if (aliasNodes.getLength() == 0) {
                errHandler.addError(ErrorSeverity.ERROR, ErrorMessage
                        .format("level2.ResourceMap.err1"), new ErrorLocator(
                        -1, -1, XMLUtils.getXPointer(resourceMap)));
                continue;
            }
            for (int j = 0; j < aliasNodes.getLength(); j++) {
                Element alias = (Element) aliasNodes.item(j);
                Node sourceRef = alias.getElementsByTagNameNS(Namespaces.KML22,
                        "sourceHref").item(0);
                String href = sourceRef.getTextContent().trim();
                if (!aliasSourceSet.add(href)) {
                    errHandler.addError(
                            ErrorSeverity.ERROR,
                            ErrorMessage
                                    .format("level2.ResourceMap.err2", href),
                            new ErrorLocator(-1, -1, XMLUtils
                                    .getXPointer(alias)));
                }
            }
        }
        Assert.assertFalse(errHandler.errorsDetected(), errHandler.toString());
    }

    /**
     * [{@code Test}] Verifies that a kml:Link or a kml:Icon element (both of
     * type kml:LinkType) satisfies all of the following constraints:
     * 
     * <ol>
     * <li>if the kml:refreshInterval element is present, the kml:refreshMode
     * value must be "onInterval";</li>
     * <li>if the kml:viewRefreshTime element is present, the
     * kml:viewRefreshMode value must be "onStop".</li>
     * </ol>
     * 
     * @see "OGC 07-134r2, ATC 51: Link refresh values"
     */
    @Test(description = "Implements ATC 51")
    public void verifyLinkRefresh() {
        NodeList linkNodes = null;
        try {
            linkNodes = XMLUtils.evaluateXPath(this.testSubject,
                    "//kml:Link | //kml:Icon", NS_MAP);
        } catch (XPathExpressionException xpe) {
            throw new RuntimeException(xpe);
        }
        ValidationErrorHandler errHandler = new ValidationErrorHandler();
        for (int i = 0; i < linkNodes.getLength(); i++) {
            Node link = linkNodes.item(i);
            try {
                if ((Boolean) XMLUtils
                        .evaluateXPath(
                                link,
                                "kml:refreshInterval and kml:refreshMode != 'onInterval'",
                                NS_MAP, XPathConstants.BOOLEAN)) {
                    errHandler
                            .addError(
                                    ErrorSeverity.ERROR,
                                    ErrorMessage
                                            .format("level2.LinkRefresh.err1"),
                                    new ErrorLocator(-1, -1, XMLUtils
                                            .getXPointer(link)));
                }
                if ((Boolean) XMLUtils
                        .evaluateXPath(
                                link,
                                "kml:viewRefreshTime and kml:viewRefreshMode != 'onStop'",
                                NS_MAP, XPathConstants.BOOLEAN)) {
                    errHandler
                            .addError(
                                    ErrorSeverity.ERROR,
                                    ErrorMessage
                                            .format("level2.LinkRefresh.err2"),
                                    new ErrorLocator(-1, -1, XMLUtils
                                            .getXPointer(link)));
                }
            } catch (XPathExpressionException xpe) {
                throw new RuntimeException(xpe);
            }
        }
        Assert.assertFalse(errHandler.errorsDetected(), errHandler.toString());
    }

    /**
     * [{@code Test}] Verifies that if a kml:PhotoOverlay element includes a
     * kml:Icon/kml:href element containing [x], [y], and [level] parameters (to
     * accommodate large images), then it also includes a child kml:ImagePyramid
     * element. The converse must also be true.
     * 
     * The parameters are embedded within the URL. For example:
     * 
     * <pre>
     * http://example.org/bigphoto/$[level]/row_$[x]_column_$[y].jpg.
     * </pre>
     * 
     * <p>
     * Check for the kml:ImagePyramid when the x, y, level parameters are
     * present, or if the kml:ImagePyramid is present check for the x, y, level
     * parameters.
     * </p>
     * 
     * @see "OGC 07-134r2, ATC 52: PhotoOverlay"
     */
    @Test(description = "Implements ATC 52")
    public void verifyImagePyramidInPhotoOverlay() {
        NodeList overlayNodes = null;
        try {
            overlayNodes = XMLUtils.evaluateXPath(this.testSubject,
                    "//kml:PhotoOverlay[kml:Icon or kml:ImagePyramid]", NS_MAP);
        } catch (XPathExpressionException xpe) {
            throw new RuntimeException(xpe);
        }
        ValidationErrorHandler errHandler = new ValidationErrorHandler();
        for (int i = 0; i < overlayNodes.getLength(); i++) {
            Element photoOverlay = (Element) overlayNodes.item(i);
            String href;
            try {
                href = (String) XMLUtils.evaluateXPath(photoOverlay,
                        "kml:Icon/kml:href", NS_MAP, XPathConstants.STRING);
            } catch (XPathExpressionException e) {
                throw new RuntimeException(e);
            }
            Element pyramid = (Element) photoOverlay.getElementsByTagNameNS(
                    Namespaces.KML22, "ImagePyramid").item(0);
            boolean hrefParams = (null != href && href.contains("[level]"));
            if ((null != pyramid) && !hrefParams) {
                errHandler.addError(ErrorSeverity.ERROR, ErrorMessage
                        .format("level2.PhotoOverlay.err"), new ErrorLocator(
                        -1, -1, XMLUtils.getXPointer(photoOverlay)));
            }
            if ((null == pyramid) && hrefParams) {
                errHandler.addError(ErrorSeverity.ERROR, ErrorMessage
                        .format("level2.PhotoOverlay.err2"), new ErrorLocator(
                        -1, -1, XMLUtils.getXPointer(photoOverlay)));
            }

        }
        Assert.assertFalse(errHandler.errorsDetected(), errHandler.toString());
    }

    /**
     * [{@code Test}] Verifies that if a kml:GroundOverlay element is not a
     * descendant of kml:Update, it contains a kml:LatLonBox element (with
     * kml:north, kml:south, kml:east, kml:west).
     * 
     * @see "OGC 07-134r2, ATC 53: GroundOverlay - minimal content"
     */
    @Test(description = "Implements ATC 53")
    public void verifyGroundOverlayHasLatLonBox() {
        NodeList overlayNodes = null;
        try {
            overlayNodes = XMLUtils.evaluateXPath(this.testSubject,
                    "//kml:GroundOverlay[not(ancestor::kml:Update)]", NS_MAP);
        } catch (XPathExpressionException xpe) {
            throw new RuntimeException(xpe);
        }
        ValidationErrorHandler errHandler = new ValidationErrorHandler();
        for (int i = 0; i < overlayNodes.getLength(); i++) {
            Node groundOverlay = overlayNodes.item(i);
            try {
                ETSAssert
                        .assertXPath(
                                "kml:LatLonBox[kml:north and kml:south and kml:east and kml:west]",
                                groundOverlay, NS_MAP);
            } catch (AssertionError e) {
                errHandler
                        .addError(
                                ErrorSeverity.ERROR,
                                ErrorMessage
                                        .format("level2.GroundOverlayMinimal.err1"),
                                new ErrorLocator(-1, -1, XMLUtils
                                        .getXPointer(groundOverlay)));
            }
        }
        Assert.assertFalse(errHandler.errorsDetected(), errHandler.toString());
    }

    /**
     * [{@code Test}] Verifies that a kml:Camera element satisfies all of the
     * following constraints:
     * 
     * <ol>
     * <li>if it is not a descendant of kml:Update, then the following child
     * elements are present: kml:latitude, kml:longitude, and kml:altitude;</li>
     * <li>the value of kml:altitudeMode is not "clampToGround".</li>
     * </ol>
     * 
     * @see "OGC 07-134r2, ATC 54: Camera"
     */
    @Test(description = "Implements ATC 54")
    public void verifyCamera() {
        NodeList cameraNodes = null;
        try {
            cameraNodes = XMLUtils.evaluateXPath(this.testSubject,
                    "//kml:Camera[not(ancestor::kml:Update)]", NS_MAP);
        } catch (XPathExpressionException xpe) {
            throw new RuntimeException(xpe);
        }
        ValidationErrorHandler errHandler = new ValidationErrorHandler();
        for (int i = 0; i < cameraNodes.getLength(); i++) {
            Element camera = (Element) cameraNodes.item(i);
            try {
                ETSAssert.assertXPath(
                        "kml:latitude and kml:longitude and kml:altitude",
                        camera, NS_MAP);
            } catch (AssertionError e) {
                errHandler.addError(ErrorSeverity.ERROR,
                        ErrorMessage.format("level2.Camera.err1"),
                        new ErrorLocator(-1, -1, XMLUtils.getXPointer(camera)));
            }
            String altitudeMode = ALT_MODE_CLAMP;
            Node altMode = camera.getElementsByTagNameNS(Namespaces.KML22,
                    "altitudeMode").item(0);
            if (null != altMode) {
                altitudeMode = altMode.getTextContent().trim();
            }
            if (altitudeMode.equals("clampToGround")) {
                errHandler.addError(ErrorSeverity.ERROR,
                        ErrorMessage.format("level2.Camera.err2"),
                        new ErrorLocator(-1, -1, XMLUtils.getXPointer(camera)));
            }
        }
        Assert.assertFalse(errHandler.errorsDetected(), errHandler.toString());
    }

    /**
     * [{@code Test}] Verifies that a kml:Location element satisfies all of the
     * following constraints:
     * 
     * <ol>
     * <li>it contains the kml:longitude and kml:latitude elements;</li>
     * <li>if the parent kml:Model element has a kml:altitudeMode value that is
     * not "clampToGround", then the kml:altitude element is also present.</li>
     * </ol>
     * 
     * @see "OGC 07-134r2, ATC 55: Location"
     */
    @Test(description = "Implements ATC 55")
    public void verifyModelLocation() {
        NodeList locationNodes = this.testSubject.getElementsByTagNameNS(
                Namespaces.KML22, "Location");
        ValidationErrorHandler errHandler = new ValidationErrorHandler();
        for (int i = 0; i < locationNodes.getLength(); i++) {
            Element location = (Element) locationNodes.item(i);
            try {
                ETSAssert.assertXPath("kml:latitude and kml:longitude",
                        location, NS_MAP);
            } catch (AssertionError e) {
                errHandler
                        .addError(
                                ErrorSeverity.ERROR,
                                ErrorMessage.format("level2.Location.err1"),
                                new ErrorLocator(-1, -1, XMLUtils
                                        .getXPointer(location)));
            }
            String altitudeMode = ALT_MODE_CLAMP;
            Node modelAltMode;
            try {
                modelAltMode = (Node) XMLUtils.evaluateXPath(location,
                        "../kml:altitudeMode", NS_MAP, XPathConstants.NODE);
            } catch (XPathExpressionException xpe) {
                throw new RuntimeException(xpe);
            }
            if (null != modelAltMode) {
                altitudeMode = modelAltMode.getTextContent().trim();
            }
            if (!altitudeMode.equals("clampToGround")
                    && location.getElementsByTagNameNS(Namespaces.KML22,
                            "altitude").getLength() == 0) {
                errHandler
                        .addError(
                                ErrorSeverity.ERROR,
                                ErrorMessage.format("level2.Location.err2"),
                                new ErrorLocator(-1, -1, XMLUtils
                                        .getXPointer(location)));
            }
        }
        Assert.assertFalse(errHandler.errorsDetected(), errHandler.toString());
    }

    /**
     * [{@code Test}] Verifies that if any element that substitutes for
     * kml:AbstractOverlayType (kml:PhotoOverlay, kml:GroundOverlay,
     * kml:ScreenOverlay) is not a descendant of kml:Update, then it contains a
     * kml:Icon child element.
     * 
     * @see "OGC 07-134r2, ATC 56: Overlay"
     */
    @Test(description = "Implements ATC 56")
    public void verifyOverlayHasIcon() {
        NodeList overlayNodes = null;
        try {
            overlayNodes = XMLUtils
                    .evaluateXPath(
                            this.testSubject,
                            "//kml:ScreenOverlay[not(ancestor::kml:Update)] | //kml:GroundOverlay[not(ancestor::kml:Update)] | //kml:PhotoOverlay[not(ancestor::kml:Update)]",
                            NS_MAP);
        } catch (XPathExpressionException xpe) {
            throw new RuntimeException(xpe);
        }
        ValidationErrorHandler errHandler = new ValidationErrorHandler();
        for (int i = 0; i < overlayNodes.getLength(); i++) {
            Element overlay = (Element) overlayNodes.item(i);
            if (overlay.getElementsByTagNameNS(Namespaces.KML22, "Icon")
                    .getLength() == 0) {
                errHandler
                        .addError(
                                ErrorSeverity.ERROR,
                                ErrorMessage.format("level2.Overlay.err"),
                                new ErrorLocator(-1, -1, XMLUtils
                                        .getXPointer(overlay)));
            }
        }
        Assert.assertFalse(errHandler.errorsDetected(), errHandler.toString());
    }

    /**
     * [{@code Test}] Verifies that if a kml:ScreenOverlay element is not a
     * descendant of kml:Update, then it has a kml:screenXY child element.
     * 
     * @see "OGC 07-134r2, ATC 57: ScreenOverlay"
     */
    @Test(description = "Implements ATC 57")
    public void verifyScreenOverlay() {
        NodeList overlayNodes = null;
        try {
            overlayNodes = XMLUtils.evaluateXPath(this.testSubject,
                    "//kml:ScreenOverlay[not(ancestor::kml:Update)]", NS_MAP);
        } catch (XPathExpressionException xpe) {
            throw new RuntimeException(xpe);
        }
        ValidationErrorHandler errHandler = new ValidationErrorHandler();
        for (int i = 0; i < overlayNodes.getLength(); i++) {
            Element screenOverlay = (Element) overlayNodes.item(i);
            if (screenOverlay.getElementsByTagNameNS(Namespaces.KML22,
                    "screenXY").getLength() == 0) {
                errHandler.addError(ErrorSeverity.ERROR, ErrorMessage
                        .format("level2.ScreenOverlay.err"), new ErrorLocator(
                        -1, -1, XMLUtils.getXPointer(screenOverlay)));
            }
        }
        Assert.assertFalse(errHandler.errorsDetected(), errHandler.toString());
    }

    /**
     * [{@code Test}] Verifies that a kml:ExtendedData element is not empty.
     * 
     * @see "OGC 07-134r2, ATC 59: ExtendedData"
     */
    @Test(description = "Implements ATC 59")
    public void verifyExtendedDataNotEmpty() {
        NodeList extDataNodes = this.testSubject.getElementsByTagNameNS(
                Namespaces.KML22, "ExtendedData");
        ValidationErrorHandler errHandler = new ValidationErrorHandler();
        for (int i = 0; i < extDataNodes.getLength(); i++) {
            Node extData = extDataNodes.item(i);
            if (extData.getChildNodes().getLength() == 0) {
                errHandler
                        .addError(
                                ErrorSeverity.ERROR,
                                ErrorMessage.format("level2.ExtendedData.err"),
                                new ErrorLocator(-1, -1, XMLUtils
                                        .getXPointer(extData)));
            }
        }
        Assert.assertFalse(errHandler.errorsDetected(), errHandler.toString());
    }

    /**
     * [{@code Test}] Verifies that if a kml:Folder element is not a descendant
     * of kml:Update, it is not empty.
     * 
     * @see "OGC 07-134r2, ATC 60: Folder"
     */
    @Test(description = "Implements ATC 60")
    public void verifyFolderNotEmpty() {
        ValidationErrorHandler errHandler = new ValidationErrorHandler();
        verifyElementNotEmpty("Folder", errHandler);
        Assert.assertFalse(errHandler.errorsDetected(), errHandler.toString());
    }

    /**
     * [{@code Test}] Verifies that if a kml:ImagePyramid element is not a
     * descendant of kml:Update, it satisfies all of the following constraints:
     * 
     * <ol>
     * <li>it has the kml:maxWidth and kml:maxHeight child elements;</li>
     * <li>the kml:tileSize value is a power of 2.</li>
     * </ol>
     * 
     * @see "OGC 07-134r2, ATC 62: ImagePyramid"
     */
    @Test(description = "Implements ATC 62")
    public void verifyImagePyramid() {
        NodeList imgPyramidNodes = null;
        try {
            imgPyramidNodes = XMLUtils.evaluateXPath(this.testSubject,
                    "//kml:ImagePyramid[not(ancestor::kml:Update)]", NS_MAP);
        } catch (XPathExpressionException xpe) {
            throw new RuntimeException(xpe);
        }
        ValidationErrorHandler errHandler = new ValidationErrorHandler();
        for (int i = 0; i < imgPyramidNodes.getLength(); i++) {
            Element imgPyramid = (Element) imgPyramidNodes.item(i);
            try {
                ETSAssert.assertXPath("kml:maxWidth and kml:maxHeight",
                        imgPyramid, NS_MAP);
            } catch (AssertionError e) {
                errHandler.addError(ErrorSeverity.ERROR, ErrorMessage
                        .format("level2.ImagePyramid.err1"), new ErrorLocator(
                        -1, -1, XMLUtils.getXPointer(imgPyramid)));
            }
            Node tileSizeNode = imgPyramid.getElementsByTagNameNS(
                    Namespaces.KML22, "tileSize").item(0);
            if (null != tileSizeNode) {
                int tileSize = Integer.parseInt(tileSizeNode.getTextContent());
                // power of 2 has a single 1 in bit representation
                if ((tileSize & (tileSize - 1)) != 0) {
                    errHandler.addError(
                            ErrorSeverity.ERROR,
                            ErrorMessage.format("level2.ImagePyramid.err2"),
                            new ErrorLocator(-1, -1, XMLUtils
                                    .getXPointer(imgPyramid)));
                }
            }
        }
        Assert.assertFalse(errHandler.errorsDetected(), errHandler.toString());
    }

    /**
     * [{@code Test}] Verifies that if a kml:MultiGeometry element is not a
     * descendant of kml:Update, it contains two or more geometry elements. In
     * effect this just means there are two or more child KML elements.
     * 
     * @see "OGC 07-134r2, ATC 66: MultiGeometry"
     */
    @Test(description = "Implements ATC 66")
    public void verifyMultiGeometry() {
        NodeList multiGeomNodes = null;
        try {
            multiGeomNodes = XMLUtils.evaluateXPath(this.testSubject,
                    "//kml:MultiGeometry[not(ancestor::kml:Update)]", NS_MAP);
        } catch (XPathExpressionException xpe) {
            throw new RuntimeException(xpe);
        }
        ValidationErrorHandler errHandler = new ValidationErrorHandler();
        for (int i = 0; i < multiGeomNodes.getLength(); i++) {
            Node multiGeom = multiGeomNodes.item(i);
            try {
                ETSAssert.assertXPath("count(kml:*) > 1", multiGeom, NS_MAP);
            } catch (AssertionError e) {
                errHandler.addError(ErrorSeverity.ERROR, ErrorMessage
                        .format("level2.MultiGeometry.err"), new ErrorLocator(
                        -1, -1, XMLUtils.getXPointer(multiGeom)));
            }
        }
        Assert.assertFalse(errHandler.errorsDetected(), errHandler.toString());
    }

    /**
     * [{@code Test}] Verifies that if a kml:Placemark element is not a
     * descendant of kml:Update, it includes a geometry element (any element
     * that substitutes for <em>kml:AbstractGeometryGroup</em>). Known KML
     * geometry elements include Point, LineString, LinearRing, Polygon, Model,
     * and MultiGeometry.
     * 
     * @see "OGC 07-134r2, ATC 67: Placemark"
     */
    @Test(description = "Implements ATC 67")
    public void verifyPlacemarkHasGeometry() {
        NodeList placemarkNodes = null;
        ValidationErrorHandler errHandler = new ValidationErrorHandler();
        try {
            placemarkNodes = XMLUtils.evaluateXPath(this.testSubject,
                    "//kml:Placemark[not(ancestor::kml:Update)]", NS_MAP);
            for (int i = 0; i < placemarkNodes.getLength(); i++) {
                Node placemark = placemarkNodes.item(i);
                NodeList geomList = XMLUtils
                        .evaluateXPath(
                                placemark,
                                "kml:Point | kml:LineString | kml:LinearRing | kml:Polygon | kml:Model | kml:MultiGeometry",
                                NS_MAP);
                if (geomList.getLength() == 0) {
                    errHandler.addError(ErrorSeverity.ERROR, ErrorMessage
                            .format("level2.Placemark.err"), new ErrorLocator(
                            -1, -1, XMLUtils.getXPointer(placemark)));
                }
            }
        } catch (XPathExpressionException xpe) {
            throw new RuntimeException(xpe);
        }
        Assert.assertFalse(errHandler.errorsDetected(), errHandler.toString());
    }
}

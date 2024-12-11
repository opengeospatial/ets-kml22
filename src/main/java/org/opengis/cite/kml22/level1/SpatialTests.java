package org.opengis.cite.kml22.level1;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.opengis.cite.kml22.BaseFixture;
import org.opengis.cite.kml22.ETSAssert;
import org.opengis.cite.kml22.ErrorMessage;
import org.opengis.cite.kml22.Namespaces;
import org.opengis.cite.kml22.util.KmlGeometryUnmarshaller;
import org.opengis.cite.kml22.util.TestSuiteLogger;
import org.opengis.cite.kml22.util.URIUtils;
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
 * Contains test methods that apply to spatial items in a KML resource such as
 * geometry and bounding box elements. Geometry objects include the following:
 * 
 * <ul>
 * <li>Point</li>
 * <li>LineString</li>
 * <li>LinearRing</li>
 * <li>Polygon</li>
 * <li>MultiGeometry</li>
 * <li>Model</li>
 * </ul>
 * 
 * @see "OGC 07-134r2: OGC KML 2.2 - Abstract Test Suite"
 */
public class SpatialTests extends BaseFixture {

    /**
     * [{@code Test}] Verify that a kml:coordinates element contains a list of
     * 2D or 3D tuples (separated by white space) that contain comma-separated
     * decimal values (lon,lat[,hgt]).
     * 
     * <p>
     * Pass if all kml:coordinates elements contain 2D/3D tuples containing
     * decimal values conforming to the xsd:decimal type; fail otherwise. White
     * space consists of one or more of the following characters: space
     * (U+0020), carriage return (U+000D), line feed (U+000A), or tab (U+0009).
     * The relevant 3D coordinate reference system (CRS) is defined in Annex B
     * of the OGC KML 2.2 specification (its identifier is
     * {@code urn:ogc:def:crs:OGC:LonLat84_5773}).
     * </p>
     * 
     * @see "OGC 07-134r2, ATC 3: Geometry coordinates"
     */
    @Test(description = "Implements ATC 3")
    public void verifyGeometryCoordinates() {
        NodeList coordinatesNodes = this.testSubject.getElementsByTagNameNS(
                Namespaces.KML22, "coordinates");
        ValidationErrorHandler errHandler = new ValidationErrorHandler();
        for (int i = 0; i < coordinatesNodes.getLength(); i++) {
            Node coordinates = coordinatesNodes.item(i);
            ValidationUtils
                    .validateCoordinateTuples(coordinates, 2, errHandler);
        }
        Assert.assertFalse(errHandler.errorsDetected(), errHandler.toString());
    }

    /**
     * [{@code Test}] Verifies that the content of a kml:LatLonAltBox element
     * satisfies all of the following constraints:
     * 
     * <ol>
     * <li>kml:north &gt; kml:south;</li>
     * <li>kml:east &gt; kml:west;</li>
     * <li>kml:minAltitude &lt;= kml:maxAltitude;</li>
     * <li>if kml:minAltitude and kml:maxAltitude are both present, then
     * kml:altitudeMode does not have the value "clampToGround".</li>
     * </ol>
     * 
     * The default envelope for a region of interest is the entire surface of
     * the EGM96 geoid. By testing north &gt; south and east &gt; west, we are testing
     * for a non-zero area.
     * 
     * @see "OGC 07-134r2, ATC 8: Region - LatLonAltBox"
     */
    @Test(description = "Implements ATC 8")
    public void verifyLatLonAltBox() {
        NodeList latLonAltBoxNodes = this.testSubject.getElementsByTagNameNS(
                Namespaces.KML22, "LatLonAltBox");
        ValidationErrorHandler errHandler = new ValidationErrorHandler();
        for (int i = 0; i < latLonAltBoxNodes.getLength(); i++) {
            Element latLonAltBox = (Element) latLonAltBoxNodes.item(i);
            checkLonValues(latLonAltBox, errHandler);
            checkLatValues(latLonAltBox, errHandler);
            checkAltValues(latLonAltBox, errHandler);
        }
        Assert.assertFalse(errHandler.errorsDetected(), errHandler.toString());
    }

    /**
     * [{@code Test}] Verifies that the content of a kml:LatLonBox element
     * satisfies all of the following constraints:
     * 
     * <ol>
     * <li>it contains the kml:north, kml:south, kml:east, and kml:west
     * elements;</li>
     * <li>kml:north &gt; kml:south;</li>
     * <li>kml:east &gt; kml:west.</li>
     * </ol>
     * 
     * The default envelope for a kml:GroundOverlay is the entire surface of the
     * WGS 84 ellipsoid. By testing north &gt; south and east &gt; west, we are
     * testing for a non-zero area.
     * 
     * @see "OGC 07-134r2, ATC 11: LatLonBox"
     */
    @Test(description = "Implements ATC 11")
    public void verifyLatLonBox() {
        NodeList boxNodes = this.testSubject.getElementsByTagNameNS(
                Namespaces.KML22, "LatLonBox");
        ValidationErrorHandler errHandler = new ValidationErrorHandler();
        for (int i = 0; i < boxNodes.getLength(); i++) {
            Element box = (Element) boxNodes.item(i);
            try {
                ETSAssert.assertXPath(
                        "kml:north and kml:south and kml:east and kml:west",
                        box, NS_MAP);
            } catch (AssertionError e) {
                errHandler.addError(ErrorSeverity.ERROR,
                        ErrorMessage.format("level1.LatLonBox.err1"),
                        new ErrorLocator(-1, -1, XMLUtils.getXPointer(box)));
                continue;
            }
            checkLonValues(box, errHandler);
            checkLatValues(box, errHandler);
        }
        Assert.assertFalse(errHandler.errorsDetected(), errHandler.toString());
    }

    /**
     * [{@code Test}] Verifies that if the kml:extrude element has the value
     * "true", then the value of kml:altitudeMode is <strong>not</strong>
     * "clampToGround". This constraint applies to the following elements:
     * kml:Point, kml:LineString, kml:LinearRing (but NOT if it occurs within a
     * Polygon), and kml:Polygon.
     * 
     * @see "OGC 07-134r2, ATC 12:  Geometry - extrude"
     */
    @Test(description = "Implements ATC 12")
    public void verifyGeometryExtrude() {
        NodeList extrudeNodes = this.testSubject.getElementsByTagNameNS(
                Namespaces.KML22, "extrude");
        Set<Node> allExtrudedGeometries = new LinkedHashSet<Node>();
        for (int i = 0; i < extrudeNodes.getLength(); i++) {
            Node extrude = extrudeNodes.item(i);
            String value = extrude.getTextContent().trim();
            if (value.equals("true") || value.equals("1")) {
                allExtrudedGeometries.add(extrude.getParentNode());
            }
        }
        ValidationErrorHandler errHandler = new ValidationErrorHandler();
        for (Node extrudeGeometry : allExtrudedGeometries) {
            checkAltitudeModeNotClampToGround((Element) extrudeGeometry,
                    errHandler);
        }
        Assert.assertFalse(errHandler.errorsDetected(), errHandler.toString());
    }

    /**
     * [{@code Test}] Verifies that if the kml:tessellate element has the value
     * "true", then the value of kml:altitudeMode is "clampToGround" (default
     * value). This applies to the following elements: kml:LineString,
     * kml:LinearRing (but NOT if it occurs within a Polygon), and kml:Polygon.
     * 
     * Any KML feature with no altitude mode specified will default to
     * 'clampToGround'.
     * 
     * @see "OGC 07-134r2, ATC 13:  Geometry - tessellate"
     */
    @Test(description = "Implements ATC 13")
    public void verifyGeometryTesselate() {
        NodeList tessellateNodes = this.testSubject.getElementsByTagNameNS(
                Namespaces.KML22, "tessellate");
        Set<Node> allTessellatedGeometries = new LinkedHashSet<Node>();
        for (int i = 0; i < tessellateNodes.getLength(); i++) {
            Node tessellate = tessellateNodes.item(i);
            String value = tessellate.getTextContent().trim();
            if (value.equals("true") || value.equals("1")) {
                allTessellatedGeometries.add(tessellate.getParentNode());
            }
        }
        ValidationErrorHandler errHandler = new ValidationErrorHandler();
        for (Node tessellatedGeom : allTessellatedGeometries) {
            try {
                ETSAssert
                        .assertXPath(
                                "not(kml:altitudeMode) or (kml:altitudeMode = 'clampToGround')",
                                tessellatedGeom, NS_MAP);
            } catch (AssertionError e) {
                errHandler.addError(
                        ErrorSeverity.ERROR,
                        ErrorMessage.format("level1.GeometryTessellate.err"),
                        new ErrorLocator(-1, -1, XMLUtils
                                .getXPointer(tessellatedGeom)));
                continue;
            }
        }
        Assert.assertFalse(errHandler.errorsDetected(), errHandler.toString());
    }

    /**
     * [{@code Test}] Verifies that the kml:coordinates element in a kml:Point
     * geometry contains exactly one coordinate tuple.
     * 
     * @see "OGC 07-134r2, ATC 14: Point"
     */
    @Test(description = "Implements ATC 14")
    public void verifyPointCoordinates() {
        NodeList pointCoords = null;
        try {
            pointCoords = XMLUtils.evaluateXPath(this.testSubject,
                    "//kml:Point/kml:coordinates", NS_MAP);
        } catch (XPathExpressionException xpe) {
            TestSuiteLogger.log(Level.WARNING,
                    "Error evaluating XPath expression", xpe);
        }
        ValidationErrorHandler errHandler = new ValidationErrorHandler();
        for (int i = 0; i < pointCoords.getLength(); i++) {
            Node pointCoordNode = pointCoords.item(i);
            String coordinates = pointCoordNode.getTextContent();
            // Split tuples on space (U+0020), carriage return (U+000D),
            // line feed (U+000A), or tab (U+0009) characters
            String[] tuples = coordinates.trim().split("[ \\t\\n\\r]{1,}");
            if (tuples.length > 1) {
                errHandler.addError(ErrorSeverity.ERROR, ErrorMessage
                        .format("level1.Point.err"), new ErrorLocator(-1, -1,
                        XMLUtils.getXPointer(pointCoordNode.getParentNode())));
            }
        }
        Assert.assertFalse(errHandler.errorsDetected(), errHandler.toString());
    }

    /**
     * [{@code Test}] Verifies that the kml:coordinates element in a
     * kml:LineString geometry contains at least two coordinate tuples.
     * 
     * @see "OGC 07-134r2, ATC 15: LineString"
     */
    @Test(description = "Implements ATC 15")
    public void verifyLineStringCoordinates() {
        NodeList lineCoords = null;
        try {
            lineCoords = XMLUtils.evaluateXPath(this.testSubject,
                    "//kml:LineString/kml:coordinates", NS_MAP);
        } catch (XPathExpressionException xpe) {
            TestSuiteLogger.log(Level.WARNING,
                    "Error evaluating XPath expression", xpe);
        }
        ValidationErrorHandler errHandler = new ValidationErrorHandler();
        for (int i = 0; i < lineCoords.getLength(); i++) {
            Node lineCoordNode = lineCoords.item(i);
            String coordinates = lineCoordNode.getTextContent().trim();
            // Split tuples on space (U+0020), carriage return (U+000D),
            // line feed (U+000A), or tab (U+0009) characters
            String[] tuples = coordinates.split("[ \\t\\n\\r]{1,}");
            if (tuples.length < 2) {
                errHandler.addError(
                        ErrorSeverity.ERROR,
                        ErrorMessage.format("level1.LineString.err"),
                        new ErrorLocator(-1, -1, XMLUtils
                                .getXPointer(lineCoordNode.getParentNode())));
            }
        }
        Assert.assertFalse(errHandler.errorsDetected(), errHandler.toString());
    }

    /**
     * [{@code Test}] Verifies that the kml:coordinates element in a
     * kml:LinearRing geometry contains at least 4 coordinate tuples and that
     * the first and last are identical (i.e. they constitute a closed figure).
     * 
     * @see "OGC 07-134r2, ATC 16: LinearRing - control points"
     */
    @Test(description = "Implements ATC 16")
    public void verifyLinearRingIsClosed() {
        NodeList ringCoords = null;
        try {
            ringCoords = XMLUtils.evaluateXPath(this.testSubject,
                    "//kml:LinearRing/kml:coordinates", NS_MAP);
        } catch (XPathExpressionException xpe) {
            TestSuiteLogger.log(Level.WARNING,
                    "Error evaluating XPath expression", xpe);
        }
        ValidationErrorHandler errHandler = new ValidationErrorHandler();
        for (int i = 0; i < ringCoords.getLength(); i++) {
            Node ringCoordNode = ringCoords.item(i);
            String coordinates = ringCoordNode.getTextContent().trim();
            // Split tuples on space (U+0020), carriage return (U+000D),
            // line feed (U+000A), or tab (U+0009) characters
            String[] tuples = coordinates.split("[ \\t\\n\\r]{1,}");
            if (tuples.length < 4) {
                errHandler.addError(
                        ErrorSeverity.ERROR,
                        ErrorMessage
                                .format("level1.LinearRingControlPoints.err1"),
                        new ErrorLocator(-1, -1, XMLUtils
                                .getXPointer(ringCoordNode.getParentNode())));
                continue;
            }
            String[] startPoint = tuples[0].split(",");
            String[] endPoint = tuples[tuples.length - 1].split(",");
            // WARNING: Should compare numeric values rather than strings
            if (!Arrays.equals(startPoint, endPoint)) {
                errHandler.addError(
                        ErrorSeverity.ERROR,
                        ErrorMessage
                                .format("level1.LinearRingControlPoints.err2"),
                        new ErrorLocator(-1, -1, XMLUtils
                                .getXPointer(ringCoordNode.getParentNode())));
            }
        }
        Assert.assertFalse(errHandler.errorsDetected(), errHandler.toString());
    }

    /**
     * [{@code Test}] Verifies that the boundary of a kml:Polygon element
     * satisfies all of the following constraints.
     * 
     * <ol>
     * <li>if it is not a descendant of kml:Update, then the kml:Polygon has a
     * child kml:outerBoundaryIs element;</li>
     * <li>each interior boundary defines a hole in the Polygon (that is, each
     * inner ring lies within the exterior boundary).</li>
     * </ol>
     * 
     * This test case reflects the essential definition of a polygon. No
     * particular line orientations are assumed. The relevant polygons can be
     * identified using this XPath expression:
     * <code>//kml:Polygon[not(ancestor::kml:Update)]</code>.
     * 
     * @see "OGC 07-134r2, ATC 17: Polygon boundary"
     */
    @Test(description = "Implements ATC 17")
    public void verifyPolygonBoundary() {
        NodeList polygons = null;
        try {
            polygons = XMLUtils.evaluateXPath(this.testSubject,
                    "//kml:Polygon[not(ancestor::kml:Update)]", NS_MAP);
        } catch (XPathExpressionException xpe) {
            TestSuiteLogger.log(Level.WARNING,
                    "Error evaluating XPath expression", xpe);
        }
        ValidationErrorHandler errHandler = new ValidationErrorHandler();
        for (int i = 0; i < polygons.getLength(); i++) {
            Element polygon = (Element) polygons.item(i);
            if (polygon.getElementsByTagNameNS(Namespaces.KML22,
                    "outerBoundaryIs").getLength() == 0) {
                errHandler
                        .addError(
                                ErrorSeverity.ERROR,
                                ErrorMessage
                                        .format("level1.PolygonBoundary.err1"),
                                new ErrorLocator(-1, -1, XMLUtils
                                        .getXPointer(polygon)));
                continue;
            }
            checkInnerBoundaries(polygon, errHandler);
        }
        Assert.assertFalse(errHandler.errorsDetected(), errHandler.toString());
    }

    /**
     * [{@code Test}] Verifies that a kml:Alias element (that renames texture
     * file paths found within a source COLLADA file) referenced in a kml:Model)
     * satisfies the following constraints:
     * 
     * <ol>
     * <li>the value of the child kml:targetHref element is a URI that refers to
     * an image (texture) resource;</li>
     * <li>the value of the child kml:sourceHref element corresponds to a file
     * reference appearing within the 3D object resource referenced in the
     * preceding sibling kml:Link element.</li>
     * </ol>
     * 
     * @see "OGC 07-134r2, ATC 29: Alias"
     */
    @Test(description = "Implements ATC 29")
    public void verifyTextureFileAliasInModel() {
        NodeList aliasNodes = this.testSubject.getElementsByTagNameNS(
                Namespaces.KML22, "Alias");
        ValidationErrorHandler errHandler = new ValidationErrorHandler();
        Element resourceMap = null;
        for (int i = 0; i < aliasNodes.getLength(); i++) {
            Element alias = (Element) aliasNodes.item(i);
            if (null == resourceMap) {
                resourceMap = (Element) alias.getParentNode();
            }
            try {
                ETSAssert.assertReferentExists("kml:targetHref", alias, null,
                        "image/*");
            } catch (AssertionError e) {
                errHandler.addError(ErrorSeverity.ERROR, ErrorMessage.format(
                        "level1.LinkReferents.err1", e.getMessage()),
                        new ErrorLocator(-1, -1, XMLUtils.getXPointer(alias)));
                continue;
            }
        }
        if (null != resourceMap) {
            checkModelResourceMap(resourceMap, errHandler);
        }
        Assert.assertFalse(errHandler.errorsDetected(), errHandler.toString());
    }

    /**
     * [{@code Test}] Verifies that if a kml:Orientation element is not a
     * descendant of kml:Update, then it contains at least one of the following
     * KML elements: kml:heading, kml:tilt, or kml:roll.
     * 
     * @see "OGC 07-134r2, ATC 32: Orientation - minimal content"
     */
    @Test(description = "Implements ATC 32")
    public void verifyModelOrientationNotEmpty() {
        NodeList nodeList = null;
        try {
            nodeList = XMLUtils
                    .evaluateXPath(
                            this.testSubject,
                            "//kml:Orientation[not(ancestor::kml:Update) and not(kml:*)]",
                            NS_MAP);
        } catch (XPathExpressionException xpe) {
            TestSuiteLogger.log(Level.WARNING,
                    "Error evaluating XPath expression", xpe);
        }
        ValidationErrorHandler errHandler = new ValidationErrorHandler();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node emptyOrientation = nodeList.item(i);
            errHandler.addError(ErrorSeverity.ERROR, ErrorMessage
                    .format("level1.OrientationMinimal.err"), new ErrorLocator(
                    -1, -1, XMLUtils.getXPointer(emptyOrientation)));
        }
        Assert.assertFalse(errHandler.errorsDetected(), errHandler.toString());
    }

    /**
     * {@code Test}] Verifies that the content of a kml:Model element satisfies
     * all of the following constraints:
     * 
     * <ol>
     * <li>it contains the kml:Link and kml:Location elements;</li>
     * <li>if it is not a descendant of kml:Update and the target resource
     * refers to any texture files, then there must be a
     * kml:ResourceMap/kml:Alias for each related texture file.</li>
     * </ol>
     * 
     * <p>
     * Note: This test doesn't first examine the model resource to find all
     * texture file references, since the content type is not known for certain
     * (although a COLLADA file is probably the most common one in use,
     * "model/vnd.collada+xml", *.dae).
     * </p>
     * 
     * @see "OGC 07-134r2, ATC 34: Model"
     */
    @Test(description = "Implements ATC 34")
    public void verifyModelContent() {
        NodeList modelNodes = null;
        try {
            modelNodes = XMLUtils.evaluateXPath(this.testSubject,
                    "//kml:Model[not(ancestor::kml:Update)]", NS_MAP);
        } catch (XPathExpressionException xpe) {
            TestSuiteLogger.log(Level.WARNING,
                    "Error evaluating XPath expression", xpe);
        }
        ValidationErrorHandler errHandler = new ValidationErrorHandler();
        for (int i = 0; i < modelNodes.getLength(); i++) {
            Element model = (Element) modelNodes.item(i);
            try {
                ETSAssert.assertXPath("kml:Link and kml:Location", model,
                        NS_MAP);
            } catch (AssertionError e) {
                errHandler.addError(ErrorSeverity.ERROR,
                        ErrorMessage.format("level1.Model.err1"),
                        new ErrorLocator(-1, -1, XMLUtils.getXPointer(model)));
            }
            NodeList resourceMaps = model.getElementsByTagNameNS(
                    Namespaces.KML22, "ResourceMap");
            if (resourceMaps.getLength() > 0) {
                checkModelResourceMap((Element) resourceMaps.item(0),
                        errHandler);
            }
        }
        Assert.assertFalse(errHandler.errorsDetected(), errHandler.toString());
    }

    /**
     * Checks the content of a model resource for the occurrence of the expected
     * source file names given by aliases contained in a kml:ResourceMap. The
     * model resource (e.g. a COLLADA file) is referenced by a
     * kml:Model/kml:Link element.
     * 
     * <pre>
     * {@code 
     * <Model id="model-id">
     *   <Link>
     *     <href>...</href>
     *   </Link>
     *   <ResourceMap>
     *     <Alias>
     *       <targetHref>...</targetHref>
     *       <sourceHref>...</sourceHref>
     *     </Alias>
     *   </ResourceMap>
     * </Model>
     * }
     * </pre>
     * 
     * @param resourceMap
     *            A kml:ResourceMap element.
     * @param errHandler
     *            The error handler that receives reports of any constraint
     *            violations.
     */
    void checkModelResourceMap(Element resourceMap,
            ValidationErrorHandler errHandler) {
        File sourceModel = null;
        try {
            Node modelHref = (Node) XMLUtils.evaluateXPath(resourceMap,
                    "../kml:Link/kml:href", NS_MAP, XPathConstants.NODE);
            URI modelUri = URI.create(modelHref.getTextContent().trim());
            if (!modelUri.isAbsolute()) {
                String base = resourceMap.getOwnerDocument().getDocumentURI();
                modelUri = URI.create(base).resolve(modelUri);
            }
            sourceModel = URIUtils.dereferenceURI(modelUri);
        } catch (Exception e) {
            TestSuiteLogger.log(Level.WARNING,
                    "Unable to locate Model referent. ", e);
        }
        if (null == sourceModel) {
            errHandler.addError(ErrorSeverity.ERROR, ErrorMessage
                    .format("level1.Alias.err4"), new ErrorLocator(-1, -1,
                    XMLUtils.getXPointer(resourceMap.getParentNode())));
            return;
        }
        NodeList aliases = resourceMap.getElementsByTagNameNS(Namespaces.KML22,
                "Alias");
        for (int i = 0; i < aliases.getLength(); i++) {
            Element alias = (Element) aliases.item(i);
            Element sourceHref = (Element) alias.getElementsByTagNameNS(
                    Namespaces.KML22, "sourceHref").item(0);
            URI sourceURI = URI.create(sourceHref.getTextContent().trim());
            int lastSlash = sourceURI.toString().lastIndexOf("/");
            String sourceFileName = (lastSlash > -1) ? sourceURI.toString()
                    .substring(lastSlash + 1) : sourceURI.toString();
            // scan model for source file name
            boolean foundFileName = false;
            Scanner scanner = null;
            try {
                scanner = new Scanner(sourceModel);
                while (scanner.hasNextLine()) {
                    if (scanner.nextLine().indexOf(sourceFileName) > -1) {
                        foundFileName = true;
                        break;
                    }
                }
            } catch (FileNotFoundException e) {
                TestSuiteLogger.log(Level.FINE, "Model not found.", e);
            } finally {
                if (null != scanner) {
                    scanner.close();
                }
            }
            if (!foundFileName) {
                errHandler.addError(ErrorSeverity.ERROR, ErrorMessage.format(
                        "level1.Alias.err5", sourceFileName), new ErrorLocator(
                        -1, -1, XMLUtils.getXPointer(alias)));
            }
        }
    }

    /**
     * An error is reported if any inner boundary of a polygon is not within the
     * outer boundary.
     * 
     * @param polygonElem
     *            A DOM Element representing a kml:Polygon element.
     * @param errHandler
     *            The error handler that receives any errors that were detected.
     */
    void checkInnerBoundaries(Element polygonElem,
            ValidationErrorHandler errHandler) {
        NodeList ringNodes = polygonElem.getElementsByTagNameNS(
                Namespaces.KML22, "LinearRing");
        // rings are in document order, so outer ring is first.
        if (ringNodes.getLength() > 1) {
            Element outerRingElem = (Element) ringNodes.item(0);
            KmlGeometryUnmarshaller unmarshaller = new KmlGeometryUnmarshaller();
            LinearRing outerRing = (LinearRing) unmarshaller
                    .unmarshalKmlGeometry(outerRingElem);
            GeometryFactory geoFactory = new GeometryFactory();
            Polygon polygon = geoFactory.createPolygon(outerRing, null);
            for (int i = 1; i < ringNodes.getLength(); i++) {
                Element ringElem = (Element) ringNodes.item(i);
                LinearRing innerRing = (LinearRing) unmarshaller
                        .unmarshalKmlGeometry(ringElem);
                if (!innerRing.within(polygon)) {
                    errHandler.addError(
                            ErrorSeverity.ERROR,
                            ErrorMessage.format("level1.PolygonBoundary.err2"),
                            new ErrorLocator(-1, -1, XMLUtils
                                    .getXPointer(ringNodes.item(i))));
                }
            }
        }
    }

    /**
     * An error is reported if the geometry element has a kml:altitudeMode of
     * "clampToGround" (default value) and the kml:extrude value is "true".
     * 
     * @param geometry
     *            A geometry element where kml:extrude is "true".
     * @param errHandler
     *            The error handler that receives any errors that were detected.
     */
    void checkAltitudeModeNotClampToGround(Element geometry,
            ValidationErrorHandler errHandler) {
        NodeList altitudeModeNodes = geometry.getElementsByTagNameNS(
                Namespaces.KML22, "altitudeMode");
        // default mode is clampToGround
        if (altitudeModeNodes.getLength() == 0) {
            errHandler.addError(ErrorSeverity.ERROR,
                    ErrorMessage.format("level1.GeometryExtrude.err1"),
                    new ErrorLocator(-1, -1, XMLUtils.getXPointer(geometry)));
            return;
        }
        String altitudeMode = altitudeModeNodes.item(0).getTextContent().trim();
        if (altitudeMode.equals("clampToGround")) {
            errHandler.addError(ErrorSeverity.ERROR,
                    ErrorMessage.format("level1.GeometryExtrude.err2"),
                    new ErrorLocator(-1, -1, XMLUtils.getXPointer(geometry)));
        }
    }

    /**
     * An error is reported if any box elements having longitude children are
     * invalid; kml:east must be greater than kml:west.
     * 
     * @param box
     *            A box element (kml:LatLonAltBox, kml:LatLonBox)
     * @param errHandler
     *            The error handler that receives any errors that were detected.
     */
    void checkLonValues(Element box, ValidationErrorHandler errHandler) {
        NodeList eastNodes = box.getElementsByTagNameNS(Namespaces.KML22,
                "east");
        NodeList westNodes = box.getElementsByTagNameNS(Namespaces.KML22,
                "west");
        Float east = Float.valueOf(180);
        Float west = Float.valueOf(-180);
        if (eastNodes.getLength() != 0) {
            east = Float.valueOf(eastNodes.item(0).getTextContent());
        }
        if (westNodes.getLength() != 0) {
            west = Float.valueOf(westNodes.item(0).getTextContent());
        }
        if (east == null || west == null) {
            errHandler.addError(ErrorSeverity.ERROR,
                    ErrorMessage.format("level1.LatLonAltBox.err1"),
                    new ErrorLocator(-1, -1, XMLUtils.getXPointer(box)));
            return;
        }
        if (east <= west) {
            errHandler.addError(ErrorSeverity.ERROR,
                    ErrorMessage.format("level1.LatLonAltBox.err2"),
                    new ErrorLocator(-1, -1, XMLUtils.getXPointer(box)));
        }
    }

    /**
     * An error is reported if any box elements having latitude children are
     * invalid; kml:north must be greater than kml:south.
     * 
     * @param box
     *            A box element (kml:LatLonAltBox, kml:LatLonBox)
     * @param errHandler
     *            The error handler that receives any errors that were detected.
     */
    void checkLatValues(Element box, ValidationErrorHandler errHandler) {
        NodeList northNodes = box.getElementsByTagNameNS(Namespaces.KML22,
                "north");
        NodeList southNodes = box.getElementsByTagNameNS(Namespaces.KML22,
                "south");
        Float north = Float.valueOf(180);
        Float south = Float.valueOf(-180);
        if (northNodes.getLength() != 0) {
            north = Float.valueOf(northNodes.item(0).getTextContent());
        }
        if (southNodes.getLength() != 0) {
            south = Float.valueOf(southNodes.item(0).getTextContent());
        }
        if (north == null || south == null) {
            errHandler.addError(ErrorSeverity.ERROR,
                    ErrorMessage.format("level1.LatLonAltBox.err3"),
                    new ErrorLocator(-1, -1, XMLUtils.getXPointer(box)));
            return;
        }
        if (north <= south) {
            errHandler.addError(ErrorSeverity.ERROR,
                    ErrorMessage.format("level1.LatLonAltBox.err4"),
                    new ErrorLocator(-1, -1, XMLUtils.getXPointer(box)));
        }
    }

    /**
     * An error is reported if any kml:LatLonAltBox elements altitude children
     * are invalid; kml:maxAltitude must be greater than or equal to
     * kml:minAltitude.
     * 
     * @param latLonAltBox
     *            A kml:LatLonAltBox element.
     * @param errHandler
     *            The error handler that receives any errors that were detected.
     */
    void checkAltValues(Element latLonAltBox, ValidationErrorHandler errHandler) {
        NodeList minAltitudeNodes = latLonAltBox.getElementsByTagNameNS(
                Namespaces.KML22, "minAltitude");
        NodeList maxAltitudeNodes = latLonAltBox.getElementsByTagNameNS(
                Namespaces.KML22, "maxAltitude");
        if (minAltitudeNodes.getLength() != 0
                && maxAltitudeNodes.getLength() != 0) {
            checkAltMode(latLonAltBox, errHandler);
        }
        Float minAltitude = Float.valueOf(0);
        Float maxAltitude = Float.valueOf(0);
        if (minAltitudeNodes.getLength() != 0) {
            minAltitude = Float.valueOf(minAltitudeNodes.item(0)
                    .getTextContent());
        }
        if (maxAltitudeNodes.getLength() != 0) {
            maxAltitude = Float.valueOf(maxAltitudeNodes.item(0)
                    .getTextContent());
        }
        if (minAltitude == null || maxAltitude == null) {
            errHandler
                    .addError(
                            ErrorSeverity.ERROR,
                            ErrorMessage.format("level1.LatLonAltBox.err5"),
                            new ErrorLocator(-1, -1, XMLUtils
                                    .getXPointer(latLonAltBox)));
            return;
        }
        if (minAltitude > maxAltitude) {
            errHandler
                    .addError(
                            ErrorSeverity.ERROR,
                            ErrorMessage.format("level1.LatLonAltBox.err6"),
                            new ErrorLocator(-1, -1, XMLUtils
                                    .getXPointer(latLonAltBox)));
        }
    }

    /**
     * An error is reported if any kml:LatLonAltBox element's altitude children
     * are present but the kml:altitudeMode == clampToGround (also the default
     * value).
     * 
     * @param latLonAltBox
     *            A kml:LatLonAltBox element.
     * @param errHandler
     *            The error handler that receives any errors that were detected.
     */
    void checkAltMode(Element latLonAltBox, ValidationErrorHandler errHandler) {
        // When both kml:minAltitude and kml:maxAltitude are present
        NodeList altitudeModeNodes = latLonAltBox.getElementsByTagNameNS(
                Namespaces.KML22, "altitudeMode");
        boolean altitudeModeExists = (altitudeModeNodes.getLength() > 0) ? true
                : false;
        if (!altitudeModeExists) {
            errHandler
                    .addError(
                            ErrorSeverity.ERROR,
                            ErrorMessage.format("level1.LatLonAltBox.err7"),
                            new ErrorLocator(-1, -1, XMLUtils
                                    .getXPointer(latLonAltBox)));
            return;
        }
        if (altitudeModeExists) {
            String altitudeModeStr = altitudeModeNodes.item(0).getTextContent();
            if (altitudeModeStr.equals("clampToGround")) {
                errHandler.addError(ErrorSeverity.ERROR, ErrorMessage
                        .format("level1.LatLonAltBox.err8"), new ErrorLocator(
                        -1, -1, XMLUtils.getXPointer(latLonAltBox)));
            }
        }
    }
}

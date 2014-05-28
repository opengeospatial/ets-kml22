package org.opengis.cite.kml22.util;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.opengis.cite.kml22.Namespaces;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;

/**
 * Unmarshals KML 2.x geometry representations (except
 * <code>&lt;Model&gt;</code>) from DOM sources to JTS Geometry objects. The
 * following geometry elements are supported: Point, LineString, LinearRing,
 * Polygon, and MultiGeometry.
 * 
 */
public class KmlGeometryUnmarshaller {

    private static final Logger LOGR = Logger
            .getLogger(KmlGeometryUnmarshaller.class.getPackage().getName());
    private String namespaceURI;
    private GeometryFactory geoFactory;

    /**
     * Creates an unmarshaller for KML 2.2 geometry elements.
     */
    public KmlGeometryUnmarshaller() {
        this(Namespaces.KML22);
    }

    /**
     * Constructs an unmarshaller for KML geometry elements in the given target
     * namespace.
     * 
     * @param namespaceURI
     *            The name of the target namespace (an absolute URI value).
     */
    public KmlGeometryUnmarshaller(String namespaceURI) {
        this.namespaceURI = namespaceURI;
        this.geoFactory = new GeometryFactory();
    }

    /**
     * Creates a JTS geometry object from a KML 2.x geometry representation. The
     * provided node must be either an Element or a Document node; if the
     * latter, the geometry must be the document element.
     * 
     * @param node
     *            a DOM Document or Element node representation of a KML 2
     *            geometry element.
     * @return the corresponding JTS Geometry object.
     */
    public Geometry unmarshalKmlGeometry(Node node) {

        short nodeType = node.getNodeType();
        if ((nodeType != Node.DOCUMENT_NODE) && (nodeType != Node.ELEMENT_NODE)) {
            throw new IllegalArgumentException(
                    "Expect Document or Element node as input to unmarshalKmlGeometry(Node node)");
        }
        Element geoElem = null;
        if (nodeType == Node.DOCUMENT_NODE) {
            Document root = (Document) node;
            geoElem = root.getDocumentElement();
        } else {
            geoElem = (Element) node;
        }
        String localName = geoElem.getLocalName();
        Geometry geom = null;
        if (localName.equals("Point")) {
            geom = unmarshalPoint(geoElem);
        } else if (localName.equals("LineString")) {
            geom = unmarshalLineString(geoElem);
        } else if (localName.equals("LinearRing")) {
            geom = unmarshalLinearRing(geoElem);
        } else if (localName.equals("Polygon")) {
            geom = unmarshalPolygon(geoElem);
        } else if (localName.equals("MultiGeometry")) {
            geom = unmarshalMultiGeometry(geoElem);
        } else {
            throw new IllegalArgumentException("Unsupported KML geometry type");
        }
        return geom;
    }

    /**
     * Creates a Point geometry object from a source KML2 Point representation.
     * The ID of the spatial reference system is set to 0 (undefined).
     * 
     * @param geoElem
     *            A DOM Element representing a kml:Point element.
     * @return the corresponding JTS Point object
     */
    Point unmarshalPoint(Element geoElem) {

        assert geoElem.getLocalName().equals("Point") : "Expected Point element as input";
        Node coords = geoElem.getElementsByTagNameNS(this.namespaceURI,
                "coordinates").item(0);
        CoordinateArraySequence cas = buildCoordinateArraySequence(coords);
        Point pt = geoFactory.createPoint(cas);
        return pt;
    }

    /**
     * Creates a LineString object from a KML2 LineString representation. The ID
     * of the spatial reference system is set to 0 (undefined).
     * 
     * @param geoElem
     *            a DOM Element representing a gml:LineString element.
     * @return the corresponding JTS LineString object
     */
    LineString unmarshalLineString(Element geoElem) {
        assert geoElem.getLocalName().startsWith("LineString") : "Expected LineString as input";
        Node coords = geoElem.getElementsByTagNameNS(this.namespaceURI,
                "coordinates").item(0);
        CoordinateArraySequence cas = buildCoordinateArraySequence(coords);
        LineString line = geoFactory.createLineString(cas);
        return line;
    }

    /**
     * Creates a LinearRing geometry object from a source KML2 LinearRing
     * representation. The ID of the spatial reference system is set to 0
     * (undefined).
     * 
     * @param geoElem
     *            a DOM Element representing a kml:LinearRing element.
     * @return the corresponding JTS LinearRing object
     */
    LinearRing unmarshalLinearRing(Element geoElem) {
        assert geoElem.getLocalName().equals("LinearRing") : "Expected LinearRing element as input";
        LinearRing ring = null;
        Node coords = geoElem.getElementsByTagNameNS(this.namespaceURI,
                "coordinates").item(0);
        CoordinateArraySequence cas = buildCoordinateArraySequence(coords);
        ring = geoFactory.createLinearRing(cas);
        return ring;
    }

    /**
     * Creates a Polygon geometry object from a source KML2 Polygon
     * representation. The ID of the spatial reference system is set to 0
     * (undefined).
     * 
     * @param geoElem
     *            a DOM Element representing a kml:Polygon element.
     * @return the corresponding JTS Polygon object.
     */
    Polygon unmarshalPolygon(Element geoElem) {
        assert geoElem.getLocalName().equals("Polygon") : "Expected Polygon element as input";
        NodeList boundary = geoElem.getElementsByTagNameNS(this.namespaceURI,
                "LinearRing");
        int nRings = boundary.getLength();
        ArrayList<LinearRing> rings = new ArrayList<LinearRing>(nRings);
        for (int i = 0; i < nRings; i++) {
            Element ringElem = (Element) boundary.item(i);
            LinearRing ring = unmarshalLinearRing(ringElem);
            rings.add(ring);
        }
        Polygon poly = geoFactory.createPolygon(rings.remove(0),
                rings.toArray(new LinearRing[nRings - 1]));
        return poly;
    }

    /**
     * Creates a GeometryCollection geometry object from a source KML2
     * MultiGeometry representation with one or more geometry members.
     * 
     * @param geoElem
     *            a DOM Element representing a kml:MultiGeometry element.
     * @return the corresponding JTS GeometryCollection object
     */
    GeometryCollection unmarshalMultiGeometry(Element geoElem) {
        assert geoElem.getLocalName().equals("MultiGeometry") : "Expected MultiGeometry element as input";
        NodeList members = geoElem.getChildNodes();
        int nMembers = members.getLength();
        ArrayList<Geometry> geometries = new ArrayList<Geometry>(nMembers);
        for (int i = 0; i < nMembers; i++) {
            Element geomElem = (Element) members.item(i);
            Geometry geom = unmarshalKmlGeometry(geomElem);
            geometries.add(geom);
        }
        GeometryCollection geomColl = geoFactory
                .createGeometryCollection(geometries
                        .toArray(new Geometry[nMembers]));
        return geomColl;
    }

    /**
     * Builds a raw coordinate sequence from the tuples included in a given
     * coordinates element. According to the KML reference, coordinates must be
     * specified as a space-separated list of 2D or 3D tuples: lon,lat[,alt]. An
     * <code>IllegalArgumentException</code> is thrown if a coordinate tuple is
     * is not 2D or 3D.
     * 
     * @param coords
     *            a DOM Node representing a kml:coordinates element
     * @return a CoordinateArraySequence that can be used to create primitive
     *         geometry objects
     */
    CoordinateArraySequence buildCoordinateArraySequence(Node coords) {
        assert coords.getLocalName().equals("coordinates") : "Expected coordinates element as input";
        String[] tuples = coords.getTextContent().trim().split("\\s+");
        int nTuples = tuples.length;
        if (LOGR.isLoggable(Level.FINER)) {
            LOGR.finer("nTuples: " + nTuples);
        }
        ArrayList<Coordinate> coordinates = new ArrayList<Coordinate>(nTuples);
        for (int i = 0; i < nTuples; i++) {
            Coordinate coord = null;
            String[] values = tuples[i].split(",");
            int crsDim = values.length;
            if (crsDim < 2 || crsDim > 3) {
                throw new IllegalArgumentException(
                        "Not a 2D or 3D coordinate tuple: " + tuples[i]);
            }
            if (crsDim == 2) {
                coord = new Coordinate(Double.parseDouble(values[0]),
                        Double.parseDouble(values[1]));
            } else {
                coord = new Coordinate(Double.parseDouble(values[0]),
                        Double.parseDouble(values[1]),
                        Double.parseDouble(values[2]));
            }
            coordinates.add(coord);
        }
        Coordinate[] tupleArray = coordinates.toArray(new Coordinate[nTuples]);
        CoordinateArraySequence cas = new CoordinateArraySequence(tupleArray);
        return cas;
    }
}

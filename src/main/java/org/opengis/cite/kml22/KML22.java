package org.opengis.cite.kml22;

/**
 * Contains various constants pertaining to KML 2.2 documents.
 * 
 * <h3 style="margin-bottom: 0.5em">Sources</h3>
 * <ul>
 * <li><a href="http://portal.opengeospatial.org/files/?artifact_id=27810"
 * target="_blank">OGC KML 2.2.0</a> (OGC 07-147r2)</li>
 * <li><a href="https://developers.google.com/kml/documentation/kmlreference"
 * target="_blank">KML Reference</a></li>
 * </ul>
 * 
 */
public class KML22 {

    private KML22() {
    }

    /**
     * The namespace name for the KML 2.2 schema.
     */
    public static final String NS_NAME = "http://www.opengis.net/kml/2.2";

    /** Local name of the root element in a KML document. */
    public static final String DOC_ELEMENT = "kml";

    /** KML coordinate reference system (see OGC 07-147r2, Annex B). */
    public static final String EPSG_4326 = "urn:ogc:def:crs:OGC:LonLat84_5773";

    /**
     * KML media type (see
     * http://www.iana.org/assignments/media-types/application).
     */
    public static final String KML_MEDIA_TYPE = "application/vnd.google-earth.kml+xml";

    /** KMZ media type. */
    public static final String KMZ_MEDIA_TYPE = "application/vnd.google-earth.kmz";

    /**
     * Local name of kml:Update element (specifies an addition, change, or
     * deletion to KML data).
     */
    public static final String UPDATE = "Update";
}

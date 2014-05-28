package org.opengis.cite.kml22.level1;

import java.util.LinkedHashSet;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.opengis.cite.kml22.BaseFixture;
import org.opengis.cite.kml22.ErrorMessage;
import org.opengis.cite.kml22.Namespaces;
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
 * Contains test methods that apply to temporal elements (TimeSpan and
 * TimeStamp).
 * 
 * @see "OGC 07-134r2: OGC KML 2.2 - Abstract Test Suite"
 */
public class TemporalElementTests extends BaseFixture {

    /**
     * [{@code Test}] Verifies that a TimeSpan element satisfies all of the
     * following constraints:
     * 
     * <ol>
     * <li>it includes at least one child element (kml:begin or kml:end);</li>
     * <li>if it is a definite interval (both kml:begin and kml:end are
     * present), then the begin value is earlier than the end value.</li>
     * </ol>
     * 
     * @see "OGC 07-134r2, ATC 4: TimeSpan"
     */
    @Test(description = "Implements ATC 4")
    public void verifyTimeSpan() {
        NodeList allTimeSpanNodes = this.testSubject.getElementsByTagNameNS(
                Namespaces.KML22, "TimeSpan");
        // use LinkedHashSet to preserve document order
        Set<Node> indefiniteTimeSpans = new LinkedHashSet<Node>();
        Set<Node> definiteTimeSpans = new LinkedHashSet<Node>();
        for (int i = 0; i < allTimeSpanNodes.getLength(); i++) {
            Element timeSpan = (Element) allTimeSpanNodes.item(i);
            NodeList beginNodes = timeSpan.getElementsByTagNameNS(
                    Namespaces.KML22, "begin");
            NodeList endNodes = timeSpan.getElementsByTagNameNS(
                    Namespaces.KML22, "end");
            if (beginNodes.getLength() > 0 && endNodes.getLength() > 0) {
                definiteTimeSpans.add(timeSpan);
            } else {
                indefiniteTimeSpans.add(timeSpan);
            }
        }
        ValidationErrorHandler errHandler = new ValidationErrorHandler();
        for (Node indefiniteTimeSpan : indefiniteTimeSpans) {
            checkIndefiniteInterval((Element) indefiniteTimeSpan, errHandler);
        }
        for (Node definiteTimeSpan : definiteTimeSpans) {
            checkInterval((Element) definiteTimeSpan, errHandler);
        }
        Assert.assertFalse(errHandler.errorsDetected(), errHandler.toXml());
    }

    /**
     * [{@code Test}] Verifies that a kml:TimeStamp element has a child kml:when
     * element.
     * 
     * @see "OGC 07-134r2, ATC 5: TimeStamp"
     */
    @Test(description = "Implements ATC 5")
    public void verifyTimeStamp() {
        NodeList timeStampNodes = this.testSubject.getElementsByTagNameNS(
                Namespaces.KML22, "TimeStamp");
        ValidationErrorHandler errHandler = new ValidationErrorHandler();
        for (int i = 0; i < timeStampNodes.getLength(); i++) {
            Element timeStamp = (Element) timeStampNodes.item(i);
            NodeList whenNodes = timeStamp.getElementsByTagNameNS(
                    Namespaces.KML22, "when");
            if (whenNodes.getLength() == 0) {
                errHandler.addError(ErrorSeverity.ERROR, ErrorMessage
                        .format("level1.TimeStamp.err"), new ErrorLocator(-1,
                        -1, XMLUtils.getXPointer(timeStamp)));
            }
        }
        Assert.assertFalse(errHandler.errorsDetected(), errHandler.toXml());
    }

    /**
     * An error is produced if any kml:TimeSpan elements does not have at least
     * one of the required children, kml:begin or kml:end.
     * 
     * @param timeSpan
     *            A kml:TimeSpan element.
     * @param errHandler
     *            The error handler that receives any errors that were detected.
     */
    void checkIndefiniteInterval(Element timeSpan,
            ValidationErrorHandler errHandler) {
        NodeList beginNodes = timeSpan.getElementsByTagNameNS(Namespaces.KML22,
                "begin");
        NodeList endNodes = timeSpan.getElementsByTagNameNS(Namespaces.KML22,
                "end");
        if (beginNodes.getLength() == 0 && endNodes.getLength() == 0) {
            errHandler.addError(ErrorSeverity.ERROR,
                    ErrorMessage.format("level1.TimeSpan.err1"),
                    new ErrorLocator(-1, -1, XMLUtils.getXPointer(timeSpan)));
        }
    }

    /**
     * An error is produced if any kml:TimeSpan elements, which specify both a
     * kml:begin and kml:end, specify an invalid interval (kml:begin is before
     * kml:end).
     * 
     * @param timeSpan
     *            A kml:TimeSpan element.
     * @param errHandler
     *            The error handler that receives any errors that were detected.
     */
    void checkInterval(Element timeSpan, ValidationErrorHandler errHandler) {
        NodeList beginNodes = timeSpan.getElementsByTagNameNS(Namespaces.KML22,
                "begin");
        NodeList endNodes = timeSpan.getElementsByTagNameNS(Namespaces.KML22,
                "end");
        String begin = beginNodes.item(0).getTextContent();
        String end = endNodes.item(0).getTextContent();
        DateTime beginDate = parseDateTime(begin);
        DateTime endDate = parseDateTime(end);
        if (beginDate == null || endDate == null) {
            errHandler.addError(ErrorSeverity.ERROR,
                    ErrorMessage.format("level1.TimeSpan.err2"),
                    new ErrorLocator(-1, -1, XMLUtils.getXPointer(timeSpan)));
            return;
        }
        if (beginDate.getMillis() >= endDate.getMillis()) {
            errHandler.addError(ErrorSeverity.ERROR,
                    ErrorMessage.format("level1.TimeSpan.err3"),
                    new ErrorLocator(-1, -1, XMLUtils.getXPointer(timeSpan)));
        }
    }

    /**
     * Parses a string representing an instant in time as an ISO 8601 DateTime
     * object.
     * 
     * @param str
     *            The string to parse.
     * @return The corresponding DateTime object, or null if the value could not
     *         be parsed.
     */
    DateTime parseDateTime(String str) {
        DateTimeFormatter formatter = ISODateTimeFormat
                .dateOptionalTimeParser();
        DateTime dateTime = null;
        try {
            dateTime = formatter.parseDateTime(str);
        } catch (Exception e) {
            return null;
        }
        return dateTime;
    }
}

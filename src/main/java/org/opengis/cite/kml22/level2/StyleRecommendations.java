package org.opengis.cite.kml22.level2;

import javax.xml.xpath.XPathExpressionException;

import org.opengis.cite.kml22.BaseFixture;
import org.opengis.cite.kml22.ETSAssert;
import org.opengis.cite.kml22.ErrorMessage;
import org.opengis.cite.kml22.util.XMLUtils;
import org.opengis.cite.validation.ErrorLocator;
import org.opengis.cite.validation.ErrorSeverity;
import org.opengis.cite.validation.ValidationErrorHandler;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Contains test methods for checking recommended constraints that apply to style
 * selectors (kml: Style and kml:StyleMap elements).
 * 
 * <h3 style="margin-bottom: 0.5em">Sources</h3>
 * <ul>
 * <li>OGC 07-134r2: OGC KML 2.2 - Abstract Test Suite</li>
 * </ul>
 */
public class StyleRecommendations extends BaseFixture {

    /**
     * [{@code Test}] Verifies that if a kml:PolyStyle element is not a
     * descendant of kml:Update, it contains at least one of the following
     * elements: kml:color, kml:colorMode, kml:fill, or kml:outline.
     * 
     * @see "OGC 07-134r2, ATC 42: PolyStyle"
     */
    @Test(description = "Implements ATC 42")
    public void verifyPolyStyle() {
        NodeList polyStyleNodes = null;
        try {
            polyStyleNodes = XMLUtils.evaluateXPath(this.testSubject,
                    "//kml:PolyStyle[not(ancestor::kml:Update)]", NS_MAP);
        } catch (XPathExpressionException xpe) {
            throw new RuntimeException(xpe);
        }
        ValidationErrorHandler errHandler = new ValidationErrorHandler();
        for (int i = 0; i < polyStyleNodes.getLength(); i++) {
            Node polyStyle = polyStyleNodes.item(i);
            try {
                ETSAssert
                        .assertXPath(
                                "kml:color or kml:colorMode or kml:fill or kml:outline",
                                polyStyle, NS_MAP);
            } catch (AssertionError e) {
                errHandler.addError(ErrorSeverity.ERROR, ErrorMessage
                        .format("level2.PolyStyle.err"), new ErrorLocator(-1,
                        -1, XMLUtils.getXPointer(polyStyle)));
            }
        }
        Assert.assertFalse(errHandler.errorsDetected(), errHandler.toString());
    }

    /**
     * [{@code Test}] Verifies that if a kml:BalloonStyle element is not a
     * descendant of kml:Update, it is not empty.
     * 
     * @see "OGC 07-134r2, ATC 58: BalloonStyle"
     */
    @Test(description = "Implements ATC 58")
    public void verifyBalloonStyleNotEmpty() {
        ValidationErrorHandler errHandler = new ValidationErrorHandler();
        verifyElementNotEmpty("BalloonStyle", errHandler);
        Assert.assertFalse(errHandler.errorsDetected(), errHandler.toString());
    }

    /**
     * [{@code Test}] Verifies that if a kml:IconStyle element is not a
     * descendant of kml:Update, it is not empty.
     * 
     * @see "OGC 07-134r2, ATC 61: IconStyle"
     */
    @Test(description = "Implements ATC 61")
    public void verifyIconStyleNotEmpty() {
        ValidationErrorHandler errHandler = new ValidationErrorHandler();
        verifyElementNotEmpty("IconStyle", errHandler);
        Assert.assertFalse(errHandler.errorsDetected(), errHandler.toString());
    }

    /**
     * [{@code Test}] Verifies that if a kml:LabelStyle element is not a
     * descendant of kml:Update, it has at least one of the following child
     * elements: kml:color, kml:colorMode, or kml:scale.
     * 
     * @see "OGC 07-134r2, ATC 63: LabelStyle"
     */
    @Test(description = "Implements ATC 63")
    public void verifyLabelStyleNotEmpty() {
        ValidationErrorHandler errHandler = new ValidationErrorHandler();
        verifyElementNotEmpty("LabelStyle", errHandler);
        Assert.assertFalse(errHandler.errorsDetected(), errHandler.toString());
    }

    /**
     * [{@code Test}] Verifies that if a kml:ListStyle element is not a
     * descendant of kml:Update, it contains at least one of the following child
     * elements: kml:listItemType, kml:bgColor, or kml:ItemIcon.
     * 
     * @see "OGC 07-134r2, ATC 64: ListStyle"
     */
    @Test(description = "Implements ATC 64")
    public void verifyListStyle() {
        NodeList listStyleNodes = null;
        try {
            listStyleNodes = XMLUtils.evaluateXPath(this.testSubject,
                    "//kml:ListStyle[not(ancestor::kml:Update)]", NS_MAP);
        } catch (XPathExpressionException xpe) {
            throw new RuntimeException(xpe);
        }
        ValidationErrorHandler errHandler = new ValidationErrorHandler();
        for (int i = 0; i < listStyleNodes.getLength(); i++) {
            Node listStyle = listStyleNodes.item(i);
            try {
                ETSAssert.assertXPath(
                        "kml:listItemType or kml:bgColor or kml:ItemIcon",
                        listStyle, NS_MAP);
            } catch (AssertionError e) {
                errHandler.addError(ErrorSeverity.ERROR, ErrorMessage
                        .format("level2.ListStyle.err"), new ErrorLocator(-1,
                        -1, XMLUtils.getXPointer(listStyle)));
            }
        }
        Assert.assertFalse(errHandler.errorsDetected(), errHandler.toString());
    }

    /**
     * [{@code Test}] Verifies that if a kml:Style element is not a descendant
     * of kml:Update, it is not empty.
     * 
     * @see "OGC 07-134r2, ATC 65: Style"
     */
    @Test(description = "Implements ATC 65")
    public void verifyStyleNotEmpty() {
        ValidationErrorHandler errHandler = new ValidationErrorHandler();
        verifyElementNotEmpty("Style", errHandler);
        Assert.assertFalse(errHandler.errorsDetected(), errHandler.toString());
    }

    /**
     * [{@code Test}] Verifies that if a kml:StyleMap element is not a
     * descendant of kml:Update, it contains two kml:Pair elements where one key
     * value is "normal" and the other key value is "highlight".
     * 
     * @see "OGC 07-134r2, ATC 68: StyleMap"
     */
    @Test(description = "Implements ATC 68")
    public void verifyStyleMapPairs() {
        NodeList styleMapNodes = null;
        try {
            styleMapNodes = XMLUtils.evaluateXPath(this.testSubject,
                    "//kml:StyleMap[not(ancestor::kml:Update)]", NS_MAP);
        } catch (XPathExpressionException xpe) {
            throw new RuntimeException(xpe);
        }
        ValidationErrorHandler errHandler = new ValidationErrorHandler();
        for (int i = 0; i < styleMapNodes.getLength(); i++) {
            Node styleMap = styleMapNodes.item(i);
            try {
                ETSAssert
                        .assertXPath(
                                "kml:Pair[kml:key='normal'] and kml:Pair[kml:key='highlight']",
                                styleMap, NS_MAP);
            } catch (AssertionError e) {
                errHandler
                        .addError(
                                ErrorSeverity.ERROR,
                                ErrorMessage.format("level2.StyleMap.err"),
                                new ErrorLocator(-1, -1, XMLUtils
                                        .getXPointer(styleMap)));
            }
        }
        Assert.assertFalse(errHandler.errorsDetected(), errHandler.toString());
    }
}

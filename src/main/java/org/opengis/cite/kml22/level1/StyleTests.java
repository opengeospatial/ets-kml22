package org.opengis.cite.kml22.level1;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathExpressionException;

import org.opengis.cite.kml22.BaseFixture;
import org.opengis.cite.kml22.ETSAssert;
import org.opengis.cite.kml22.ErrorMessage;
import org.opengis.cite.kml22.Namespaces;
import org.opengis.cite.kml22.util.TestSuiteLogger;
import org.opengis.cite.kml22.util.URIUtils;
import org.opengis.cite.kml22.util.XMLUtils;
import org.opengis.cite.validation.ErrorLocator;
import org.opengis.cite.validation.ErrorSeverity;
import org.opengis.cite.validation.ValidationErrorHandler;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Contains test methods that apply to style definitions.
 * 
 * @see "OGC 07-134r2: OGC KML 2.2 - Abstract Test Suite"
 */
public class StyleTests extends BaseFixture {

    /**
     * [{@code Test}] Verifies that a kml:styleUrl element satisfies all of the
     * following constraints:
     * <ol>
     * <li>its value is a valid relative or absolute URL that refers to a shared
     * style definition (any element that substitutes for
     * <em>kml:AbstractStyleSelectorGroup</em>);</li>
     * <li>if the reference is an absolute URI, the value conforms to the 'http'
     * or 'file' URI schemes;</li>
     * <li>it includes a fragment identifier conforming to the shorthand pointer
     * syntax as defined in the W3C XPointer framework.</li>
     * </ol>
     * 
     * A relative URL is resolved according to the reference resolution
     * algorithm described in section 5 of RFC 3986.
     * 
     * @see "OGC 07-134r2, ATC 6: Style reference"
     * @see <a href="http://tools.ietf.org/html/rfc3986#section-5">RFC 3986:
     *      Reference Resolution</a>
     * @see <a href="http://www.w3.org/TR/xptr-framework/#shorthand">XPointer
     *      Framework: Shorthand Pointer</a>
     */
    @Test(description = "Implements ATC 6")
    public void verifyStyleReference() {
        NodeList allStyleUrlNodes = this.testSubject.getElementsByTagNameNS(
                Namespaces.KML22, "styleUrl");
        // use LinkedHashSet to preserve document order
        Set<Node> relativeStyleUrls = new LinkedHashSet<Node>();
        Set<Node> absoluteStyleUrls = new LinkedHashSet<Node>();
        ValidationErrorHandler errHandler = new ValidationErrorHandler();
        for (int i = 0; i < allStyleUrlNodes.getLength(); i++) {
            Node styleUrl = allStyleUrlNodes.item(i);
            String url = styleUrl.getTextContent();
            URI uri = null;
            try {
                uri = new URI(url);
            } catch (URISyntaxException e) {
                errHandler
                        .addError(
                                ErrorSeverity.ERROR,
                                ErrorMessage
                                        .format("level1.StyleReference.err1"),
                                new ErrorLocator(-1, -1, XMLUtils
                                        .getXPointer(styleUrl)));
                continue;
            }
            if (uri.isAbsolute()) {
                absoluteStyleUrls.add(styleUrl);
            } else {
                relativeStyleUrls.add(styleUrl);
            }
        }
        for (Node relativeStyleUrl : relativeStyleUrls) {
            checkFragmentIdentifier(relativeStyleUrl, errHandler);
            checkStyleTarget(relativeStyleUrl, errHandler);
        }
        for (Node absoluteStyleUrl : absoluteStyleUrls) {
            checkFragmentIdentifier(absoluteStyleUrl, errHandler);
            checkUriScheme(absoluteStyleUrl, errHandler);
            checkStyleTarget(absoluteStyleUrl, errHandler);
        }
        Assert.assertFalse(errHandler.errorsDetected(), errHandler.toString());

    }

    /**
     * [{@code Test}] Verifies that if a style element (any element that may
     * substitute for <em>kml:AbstractStyleSelectorGroup</em>) has a parent
     * kml:Document it is a 'shared' style. All shared styles must have the 'id'
     * attribute present.
     * 
     * Shared styles include kml:Style and kml:StyleMap elements.
     * 
     * @see "OGC 07-134r2, ATC 7: Shared style definition"
     */
    @Test(description = "Implements ATC 7")
    public void verifySharedStyleHasId() {
        NodeList sharedStyles = null;
        try {
            sharedStyles = XMLUtils.evaluateXPath(this.testSubject,
                    "//kml:Document/kml:Style | //kml:Document/kml:StyleMap",
                    NS_MAP);
        } catch (XPathExpressionException xpe) {
            TestSuiteLogger.log(Level.WARNING, this.getClass().getName(), xpe);
        }
        ValidationErrorHandler errHandler = new ValidationErrorHandler();
        for (int i = 0; i < sharedStyles.getLength(); i++) {
            Element style = (Element) sharedStyles.item(i);
            if (style.getAttribute("id").isEmpty()) {
                addError(style, "level1.SharedStyle.err3", errHandler);
            }
        }
        Assert.assertFalse(errHandler.errorsDetected(), errHandler.toString());
    }

    /**
     * [{@code Test}] Verifies that if a kml:StyleMap/kml:Pair element is not a
     * descendant of kml:Update, then it contains (a) a kml:key element, and (b)
     * at least one of kml:styleURL element or any element that substitutes for
     * <em>kml:AbstractStyleSelectorGroup</em> (kml:Style, kml:StyleMap).
     * 
     * @see "OGC 07-134r2, ATC 36: Pair"
     */
    @Test(description = "Implements ATC 36")
    public void verifyStyleMap() {
        NodeList styleMapPairs = null;
        try {
            styleMapPairs = XMLUtils.evaluateXPath(this.testSubject,
                    "//kml:Pair[not(ancestor::kml:Update)]", NS_MAP);
        } catch (XPathExpressionException xpe) {
            TestSuiteLogger.log(Level.WARNING,
                    "Error evaluating XPath expression", xpe);
        }
        ValidationErrorHandler errHandler = new ValidationErrorHandler();
        for (int i = 0; i < styleMapPairs.getLength(); i++) {
            Node pair = styleMapPairs.item(i);
            try {
                ETSAssert
                        .assertXPath(
                                "kml:key and (kml:styleUrl | kml:Style | kml:StyleMap)",
                                pair, NS_MAP);
            } catch (AssertionError e) {
                errHandler.addError(ErrorSeverity.ERROR,
                        ErrorMessage.format("level1.Pair.err", e.getMessage()),
                        new ErrorLocator(-1, -1, XMLUtils.getXPointer(pair)));
                continue;
            }
        }
        Assert.assertFalse(errHandler.errorsDetected(), errHandler.toString());
    }

    /**
     * [{@code Test}] Verifies that if a kml:ItemIcon element (with parent
     * kml:ListStyle) is not a descendant of kml:Update, then it has a kml:href
     * child element (that specifies the location of the image used in the list
     * view).
     * 
     * @see "OGC 07-134r2, ATC 37: ItemIcon"
     */
    @Test(description = "Implements ATC 37")
    public void verifyItemIconURI() {
        NodeList itemIcons = null;
        try {
            itemIcons = XMLUtils.evaluateXPath(this.testSubject,
                    "//kml:ItemIcon[not(ancestor::kml:Update)]", NS_MAP);
        } catch (XPathExpressionException xpe) {
            TestSuiteLogger.log(Level.WARNING,
                    "Error evaluating XPath expression", xpe);
        }
        ValidationErrorHandler errHandler = new ValidationErrorHandler();
        for (int i = 0; i < itemIcons.getLength(); i++) {
            Element itemIcon = (Element) itemIcons.item(i);
            if (itemIcon.getElementsByTagNameNS(Namespaces.KML22, "href")
                    .getLength() == 0) {
                errHandler
                        .addError(
                                ErrorSeverity.ERROR,
                                ErrorMessage.format("level1.ItemIcon.err"),
                                new ErrorLocator(-1, -1, XMLUtils
                                        .getXPointer(itemIcon)));
            }
        }
        Assert.assertFalse(errHandler.errorsDetected(), errHandler.toString());
    }

    /**
     * An error is produced if a kml:styleUrl does not contain fragment
     * identifier ("#").
     * 
     * @param styleUrl
     *            A kml:styleUrl element.
     * @param errHandler
     *            The error handler that receives any errors that were detected.
     */
    void checkFragmentIdentifier(Node styleUrl,
            ValidationErrorHandler errHandler) {
        URI uri;
        try {
            uri = new URI(styleUrl.getTextContent());
        } catch (Exception e) {
            addError(styleUrl, "level1.StyleReference.err1", errHandler);
            return;
        }
        String fragment = uri.getFragment();
        if (fragment == null) {
            addError(styleUrl, "level1.StyleReference.err2", errHandler);
        }
    }

    /**
     * An error is produced if an absolute kml:styleUrl is not of a supported
     * URI scheme (<code>http</code>).
     * 
     * @param styleUrl
     *            A kml:styleUrl element.
     * @param errHandler
     *            The error handler that receives any errors that were detected.
     */
    void checkUriScheme(Node styleUrl, ValidationErrorHandler errHandler) {
        URI uri;
        try {
            uri = new URI(styleUrl.getTextContent());
        } catch (Exception e) {
            addError(styleUrl, "level1.StyleReference.err1", errHandler);
            return;
        }
        String scheme = uri.getScheme();
        if (!scheme.equals("http")) {
            addError(styleUrl, "level1.StyleReference.err3", errHandler);
        }
    }

    /**
     * An error is produced if a kml:styleUrl does not refer to a valid style
     * element (which substitutes for <em>kml:AbstractStyleSelectorGroup</em>).
     * 
     * @param styleUrl
     *            A kml:styleUrl element.
     * @param errHandler
     *            The error handler that receives any errors that were detected.
     */
    void checkStyleTarget(Node styleUrl, ValidationErrorHandler errHandler) {
        URI uri;
        try {
            uri = new URI(styleUrl.getTextContent());
        } catch (Exception e) {
            addError(styleUrl, "level1.StyleReference.err1", errHandler);
            return;
        }
        String fragment = uri.getFragment();
        if (null == fragment) {
            addError(styleUrl, "level1.StyleReference.err4", errHandler);
            return;
        }
        Document rspDoc = null;
        if (uri.toString().startsWith("#")) {
            // in same document
            rspDoc = styleUrl.getOwnerDocument();
        } else if (!uri.isAbsolute()) { // Resolve relative URI
            String uriRef = uri.toString().replaceAll("#" + fragment, "");
            String docURI = styleUrl.getOwnerDocument().getDocumentURI();
            uri = URIUtils.resolveRelativeURI(docURI, uriRef);
        }
        if (null == rspDoc) {
            try {
                rspDoc = URIUtils.parseURI(uri);
            } catch (Exception e) {
                errHandler
                        .addError(
                                ErrorSeverity.ERROR,
                                ErrorMessage.format(
                                        "level1.StyleReference.err1", uri),
                                new ErrorLocator(-1, -1, XMLUtils
                                        .getXPointer(styleUrl)));
            }
        }
        // Fetch referenced style element (where @id = fragment name)
        String xpathExpr = "//*[@id='" + fragment + "']";
        NodeList styleNodeList = null;
        try {
            styleNodeList = XMLUtils.evaluateXPath(rspDoc, xpathExpr, null);
        } catch (XPathExpressionException xpe) {
            TestSuiteLogger.log(Level.WARNING, this.getClass().getName(), xpe);
        }
        Node styleNode = (styleNodeList.getLength() > 0) ? styleNodeList
                .item(0) : null;
        if (null == styleNode) {
            addError(styleUrl, "level1.StyleReference.err5", errHandler);
            return;
        }
        // Check if referenced element is a known/valid style
        QName currentStyle = new QName(styleNode.getNamespaceURI(),
                styleNode.getLocalName());
        if (!(currentStyle.getNamespaceURI().equals(Namespaces.KML22))
                || !(currentStyle.getLocalPart().equals("Style") || currentStyle
                        .getLocalPart().equals("StyleMap"))) {
            addError(styleUrl, "level1.StyleReference.err6", errHandler);
        }
    }

    void addError(Node node, String msgKey, ValidationErrorHandler errHandler) {
        errHandler.addError(ErrorSeverity.ERROR, ErrorMessage.format(msgKey),
                new ErrorLocator(-1, -1, XMLUtils.getXPointer(node)));
    }

}

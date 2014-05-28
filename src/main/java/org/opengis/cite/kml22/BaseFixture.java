package org.opengis.cite.kml22;

import java.util.Collections;
import java.util.Map;

import javax.xml.xpath.XPathExpressionException;

import org.opengis.cite.kml22.util.XMLUtils;
import org.opengis.cite.validation.ErrorLocator;
import org.opengis.cite.validation.ErrorSeverity;
import org.opengis.cite.validation.ValidationErrorHandler;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A supporting base class that provides some common configuration methods. The
 * configuration methods are invoked before any that may be defined in a
 * subclass.
 */
public class BaseFixture {

    /**
     * An immutable Map containing a KML namespace binding where the prefix is
     * "kml"
     */
    protected static final Map<String, String> NS_MAP = Collections
            .singletonMap(Namespaces.KML22, "kml");
    /** A DOM Document representing the main KML document */
    protected Document testSubject;

    /**
     * Obtains the test subject from the ISuite test context. The suite
     * attribute {@link org.opengis.cite.kml22.SuiteAttribute#TEST_SUBJECT}
     * should evaluate to a DOM Document node.
     * 
     * @param testContext
     *            The test (group) context.
     */
    @BeforeClass(alwaysRun = true)
    public void obtainTestSubject(ITestContext testContext) {
        Object obj = testContext.getSuite().getAttribute(
                SuiteAttribute.TEST_SUBJECT.getName());
        if ((null != obj) && Document.class.isAssignableFrom(obj.getClass())) {
            this.testSubject = Document.class.cast(obj);
        }
    }

    /**
     * Sets the test subject (intended only to facilitate unit testing).
     * 
     * @param testSubject
     *            A Document node representing the test subject.
     */
    public void setTestSubject(Document testSubject) {
        this.testSubject = testSubject;
    }

    /**
     * Checks that the KML element with the specified local name has one or more
     * child KML elements if it is <strong>not</strong> a descendant of
     * kml:Update. Extension elements in some foreign namespace are ignored.
     * 
     * @param localName
     *            The name of an element in the namespace
     *            {@value org.opengis.cite.kml22.Namespaces#KML22}.
     * @param errHandler
     *            The error handler that collects all detected constraint
     *            violations.
     */
    protected void verifyElementNotEmpty(String localName,
            ValidationErrorHandler errHandler) {
        String xpath = String.format("//kml:%s[not(ancestor::kml:Update)]",
                localName);
        NodeList nodeList = null;
        try {
            nodeList = XMLUtils.evaluateXPath(this.testSubject, xpath, NS_MAP);
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                NodeList kmlChildren = XMLUtils.evaluateXPath(node, "kml:*",
                        NS_MAP);
                if (kmlChildren.getLength() == 0) {
                    errHandler
                            .addError(
                                    ErrorSeverity.ERROR,
                                    ErrorMessage.format("level2.Empty",
                                            localName),
                                    new ErrorLocator(-1, -1, XMLUtils
                                            .getXPointer(node)));
                }
            }
        } catch (XPathExpressionException xpe) {
            throw new RuntimeException(xpe);
        }
    }
}

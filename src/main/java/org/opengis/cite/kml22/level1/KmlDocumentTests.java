package org.opengis.cite.kml22.level1;

import java.util.logging.Level;

import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPathExpressionException;

import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSModel;
import org.opengis.cite.kml22.BaseFixture;
import org.opengis.cite.kml22.ETSAssert;
import org.opengis.cite.kml22.ErrorMessage;
import org.opengis.cite.kml22.KML22;
import org.opengis.cite.kml22.Namespaces;
import org.opengis.cite.kml22.SuiteAttribute;
import org.opengis.cite.kml22.util.TestSuiteLogger;
import org.opengis.cite.kml22.util.XMLUtils;
import org.opengis.cite.validation.ErrorLocator;
import org.opengis.cite.validation.ErrorSeverity;
import org.opengis.cite.validation.ValidationErrorHandler;
import org.opengis.cite.validation.XSModelBuilder;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Contains fundamental test methods that apply to any KML document as a whole.
 * 
 * @see "OGC 07-134r2: OGC KML 2.2 - Abstract Test Suite"
 */
public class KmlDocumentTests extends BaseFixture {

    /**
     * [{@code Test}] Verify that the root element of the document has [local
     * name] = "kml" and [namespace name] = "http://www.opengis.net/kml/2.2".
     * 
     * @see "OGC 07-134r2, ATC 1: Root element"
     */
    @Test(description = "Implements ATC 1")
    public void verifyDocumentElement() {
        Element docElement = this.testSubject.getDocumentElement();
        Assert.assertEquals(docElement.getLocalName(), KML22.DOC_ELEMENT,
                "Document element has unexpected [local name].");
        Assert.assertEquals(docElement.getNamespaceURI(), Namespaces.KML22,
                "Document element has unexpected [namespace name].");
    }

    /**
     * [{@code Test}] Verifies that the document satisfies all KML 2.2 schema
     * constraints.
     * 
     * @param testContext
     *            The test context containing the SuiteAttribute.KML_SCHEMA
     *            attribute required to perform schema validation.
     * 
     * @see "OGC 07-134r2, ATC 2: XML Schema constraints"
     * @see <a target="_blank"
     *      href="http://schemas.opengis.net/kml/2.2.0/ogckml22.xsd"
     *      >ogckml22.xsd</a>
     */
    @Test(description = "Implements ATC 2")
    public void verifyXmlSchemaConstraints(ITestContext testContext) {
        Schema kmlSchema = (Schema) testContext.getSuite().getAttribute(
                SuiteAttribute.KML_SCHEMA.getName());
        Validator validator = kmlSchema.newValidator();
        Source source = new DOMSource(this.testSubject);
        ETSAssert.assertSchemaValid(validator, source);
    }

    /**
     * [{@code Test}] Verifies that a KML object that is not a descendant of
     * kml:Update is either (a) not empty, or (b) empty but has an 'id'
     * attribute value (so it can be easily updated). The relevant context is //
     * <em>kml:AbstractObjectType</em>[not(ancestor::kml:Update)].
     * 
     * @see "OGC 07-134r2, ATC 21: Empty object"
     * 
     * @param testContext
     *            The test context containing the SuiteAttribute.KML_SCHEMA
     *            attribute required to perform schema validation.
     * 
     */
    @Test(description = "Implements ATC 21")
    public void verifyEmptyObjectHasId(ITestContext testContext) {
        NodeList kmlElemsNotInUpdate = null;
        try {
            kmlElemsNotInUpdate = XMLUtils.evaluateXPath(this.testSubject,
                    "//kml:*[not(ancestor::kml:Update)]", NS_MAP);
        } catch (XPathExpressionException xpe) {
            TestSuiteLogger.log(Level.WARNING,
                    "Error evaluating XPath expression", xpe);
        }
        Schema kmlSchema = (Schema) testContext.getSuite().getAttribute(
                SuiteAttribute.KML_SCHEMA.getName());
        XSModel xsdModel = XSModelBuilder.buildXMLSchemaModel(kmlSchema,
                Namespaces.KML22);
        ValidationErrorHandler errHandler = new ValidationErrorHandler();
        for (int i = 0; i < kmlElemsNotInUpdate.getLength(); i++) {
            Element kmlElem = (Element) kmlElemsNotInUpdate.item(i);
            XSElementDeclaration decl = xsdModel.getElementDeclaration(
                    kmlElem.getLocalName(), kmlElem.getNamespaceURI());
            if (decl == null)
                continue;
            XSElementDeclaration subGroup = decl
                    .getSubstitutionGroupAffiliation();
            // Skip elements without a substitution group affiliation
            if (subGroup == null)
                continue;
            // Find elements that can substitute for kml:AbstractObjectGroup
            while (true) {
                XSElementDeclaration subDecl = xsdModel.getElementDeclaration(
                        subGroup.getName(), subGroup.getNamespace());
                XSElementDeclaration declSubGroup = subDecl
                        .getSubstitutionGroupAffiliation();
                if (declSubGroup == null) {
                    break;
                } else {
                    subGroup = declSubGroup;
                }
            }
            if (subGroup == null
                    || !subGroup.getName().equals("AbstractObjectGroup"))
                continue;
            checkEmptyElementHasIdAttribute(kmlElem, errHandler);
        }
        Assert.assertFalse(errHandler.errorsDetected(), errHandler.toString());
    }

    /**
     * An error is reported if any KML object is empty and does not have an id
     * attribute.
     * 
     * @param kmlElem
     *            A DOM Element representing a KML element (not a descendant of
     *            kml:Update).
     * @param errHandler
     *            The error handler that receives any errors that were detected.
     */
    void checkEmptyElementHasIdAttribute(Element kmlElem,
            ValidationErrorHandler errHandler) {
        if (kmlElem.getChildNodes().getLength() == 0
                && kmlElem.getAttribute("id").isEmpty()) {
            errHandler.addError(
                    ErrorSeverity.ERROR,
                    ErrorMessage.format("level1.EmptyObjects.err",
                            kmlElem.getNamespaceURI(), kmlElem.getLocalName()),
                    new ErrorLocator(-1, -1, XMLUtils.getXPointer(kmlElem)));
        }
    }
}

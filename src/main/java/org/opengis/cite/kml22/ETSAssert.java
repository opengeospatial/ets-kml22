package org.opengis.cite.kml22;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.logging.Level;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.opengis.cite.kml22.util.KMLUtils;
import org.opengis.cite.kml22.util.NamespaceBindings;
import org.opengis.cite.kml22.util.TestSuiteLogger;
import org.opengis.cite.kml22.util.URIUtils;
import org.opengis.cite.kml22.util.XMLUtils;
import org.opengis.cite.validation.SchematronValidator;
import org.opengis.cite.validation.ValidationErrorHandler;
import org.testng.Assert;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.Invocation.Builder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmValue;

/**
 * Provides a set of custom assertion methods.
 */
public class ETSAssert {

	private ETSAssert() {
	}

	/**
	 * Asserts that the qualified name of a DOM Node matches the expected value.
	 * @param node The Node to check.
	 * @param qName A QName object containing a namespace name (URI) and a local part.
	 */
	public static void assertQualifiedName(Node node, QName qName) {
		Assert.assertEquals(node.getLocalName(), qName.getLocalPart(), ErrorMessage.get(ErrorMessageKeys.LOCAL_NAME));
		Assert.assertEquals(node.getNamespaceURI(), qName.getNamespaceURI(),
				ErrorMessage.get(ErrorMessageKeys.NAMESPACE_NAME));
	}

	/**
	 * Asserts that an XPath 1.0 expression holds true for the given evaluation context.
	 * The following standard namespace bindings do not need to be explicitly declared:
	 *
	 * <ul>
	 * <li>ows: {@value org.opengis.cite.kml22.Namespaces#OWS}</li>
	 * <li>xlink: {@value org.opengis.cite.kml22.Namespaces#XLINK}</li>
	 * <li>gml: {@value org.opengis.cite.kml22.Namespaces#GML}</li>
	 * </ul>
	 * @param expr A valid XPath 1.0 expression.
	 * @param context The context node.
	 * @param namespaceBindings A collection of namespace bindings for the XPath
	 * expression, where each entry maps a namespace URI (key) to a prefix (value). It may
	 * be {@code null}.
	 */
	public static void assertXPath(String expr, Node context, Map<String, String> namespaceBindings) {
		if (null == context) {
			throw new NullPointerException("Context node is null.");
		}
		NamespaceBindings bindings = NamespaceBindings.withStandardBindings();
		bindings.addAllBindings(namespaceBindings);
		XPath xpath = XPathFactory.newInstance().newXPath();
		xpath.setNamespaceContext(bindings);
		Boolean result;
		try {
			result = (Boolean) xpath.evaluate(expr, context, XPathConstants.BOOLEAN);
		}
		catch (XPathExpressionException xpe) {
			String msg = ErrorMessage.format(ErrorMessageKeys.XPATH_ERROR, expr);
			TestSuiteLogger.log(Level.WARNING, msg, xpe);
			throw new AssertionError(msg);
		}
		Assert.assertTrue(result, ErrorMessage.format(ErrorMessageKeys.XPATH_RESULT, context.getNodeName(), expr));
	}

	/**
	 * Asserts that an XPath 2.0 expression evaluates to {@code true} for the given XML
	 * source. That is, the result set is not empty.
	 * @param expr An XPath 2.0 expression.
	 * @param source A Source object representing an XML resource.
	 * @param namespaceBindings A collection of namespace bindings for the XPath
	 * expression, where each entry maps a namespace URI (key) to a prefix (value). It may
	 * be {@code null}.
	 */
	public static void assertXPath2(String expr, Source source, Map<String, String> namespaceBindings) {
		XdmValue result = null;
		try {
			result = XMLUtils.evaluateXPath2(source, expr, namespaceBindings);
		}
		catch (SaxonApiException e) {
			throw new AssertionError(ErrorMessage.format(ErrorMessageKeys.XPATH_ERROR, expr + e.getMessage()));
		}
		Assert.assertTrue(result.size() > 0,
				ErrorMessage.format(ErrorMessageKeys.XPATH_RESULT, source.getSystemId(), expr));
	}

	/**
	 * Asserts that an XML resource is schema-valid.
	 * @param validator The Validator to use.
	 * @param source The XML Source to be validated.
	 */
	public static void assertSchemaValid(Validator validator, Source source) {
		ValidationErrorHandler errHandler = new ValidationErrorHandler();
		validator.setErrorHandler(errHandler);
		try {
			validator.validate(source);
		}
		catch (Exception e) {
			throw new AssertionError(ErrorMessage.format(ErrorMessageKeys.XML_ERROR, e.getMessage()));
		}
		Assert.assertFalse(errHandler.errorsDetected(), ErrorMessage.format(ErrorMessageKeys.NOT_SCHEMA_VALID,
				errHandler.getErrorCount(), errHandler.toString()));
	}

	/**
	 * Asserts that an XML resource satisfies all applicable constraints specified in a
	 * Schematron (ISO 19757-3) schema. The "xslt2" query language binding is supported.
	 * All patterns are checked.
	 * @param schemaRef A URL that denotes the location of a Schematron schema.
	 * @param xmlSource The XML Source to be validated.
	 */
	public static void assertSchematronValid(URL schemaRef, Source xmlSource) {
		SchematronValidator validator;
		try {
			validator = new SchematronValidator(new StreamSource(schemaRef.toString()), "#ALL");
		}
		catch (Exception e) {
			StringBuilder msg = new StringBuilder("Failed to process Schematron schema at ");
			msg.append(schemaRef).append('\n');
			msg.append(e.getMessage());
			throw new AssertionError(msg);
		}
		DOMResult result = (DOMResult) validator.validate(xmlSource);
		Assert.assertFalse(validator.ruleViolationsDetected(), ErrorMessage.format(ErrorMessageKeys.NOT_SCHEMA_VALID,
				validator.getRuleViolationCount(), XMLUtils.writeNodeToString(result.getNode())));
	}

	/**
	 * Asserts that the given XML entity contains the expected number of descendant
	 * elements having the specified name.
	 * @param xmlEntity A Document representing an XML entity.
	 * @param elementName The qualified name of the element.
	 * @param expectedCount The expected number of occurrences.
	 */
	public static void assertDescendantElementCount(Document xmlEntity, QName elementName, int expectedCount) {
		NodeList features = xmlEntity.getElementsByTagNameNS(elementName.getNamespaceURI(), elementName.getLocalPart());
		Assert.assertEquals(features.getLength(), expectedCount,
				String.format("Unexpected number of %s descendant elements.", elementName));
	}

	/**
	 * Asserts that the resource identified by the given link element exists.
	 * @param xpath An XPath expression that is evaluated against the given linkNode to
	 * yield a URI reference.
	 * @param linkNode A DOM node containing a URI reference (e.g. kml:Link, kml:Icon).
	 * @param httpClient An HTTP client component. If {@code null}, one will be created.
	 * @param mediaTypes An array or sequence of String values that describe the expected
	 * media type(s) of the target resource.
	 */
	public static void assertReferentExists(String xpath, Node linkNode, Client httpClient, String... mediaTypes) {
		URI uriRef = null;
		try {
			String href = (String) XMLUtils.evaluateXPath(linkNode, xpath, BaseFixture.NS_MAP, XPathConstants.STRING);
			uriRef = URI.create(href.trim());
		}
		catch (XPathExpressionException xpe) {
			throw new AssertionError("Failed to evaluate XPath expression " + xpath);
		}
		if (uriRef.toString().isEmpty()) {
			throw new AssertionError("URI reference not found in " + linkNode.getNodeName() + xpath);
		}
		assertReferentExists(uriRef, linkNode.getOwnerDocument().getBaseURI(), httpClient, mediaTypes);
	}

	/**
	 * Asserts that the resource identified by the given URI reference exists.
	 * @param uriRef A URI reference.
	 * @param baseURI A base URI for resolving a relative URI reference.
	 * @param httpClient An HTTP client component. If {@code null}, one will be created.
	 * @param mediaTypes An array or sequence of String values that describe the expected
	 * media type(s) of the target resource.
	 */
	public static void assertReferentExists(URI uriRef, String baseURI, Client httpClient, String... mediaTypes) {
		if (null == httpClient) {
			httpClient = ClientBuilder.newClient();
		}
		if (uriRef.isAbsolute() && !uriRef.getScheme().equals("file")) {
			WebTarget target = httpClient.target(uriRef);
			Builder reqBuilder = target.request();
			reqBuilder.accept(mediaTypes);
			Invocation req = reqBuilder.buildGet();
			Response rsp = req.invoke();
			if (rsp.getStatusInfo().getFamily() == Response.Status.Family.REDIRECTION) {
				// client won't automatically redirect from HTTP to HTTPS
				URI newURI = rsp.getLocation();
				target = httpClient.target(newURI);
				reqBuilder = target.request();
				reqBuilder.accept(mediaTypes);
				req = reqBuilder.buildGet();
				rsp = req.invoke();
			}
			if ((rsp.getStatus() != Response.Status.OK.getStatusCode())) {
				throw new AssertionError("No acceptable resource available at " + uriRef);
			}
		}
		else {
			URI uri = URIUtils.resolveRelativeURI(baseURI, uriRef.toString());
			File fileRef = null;
			try {
				fileRef = URIUtils.dereferenceURI(uri);
			}
			catch (IOException iox) {
				throw new AssertionError("Unable to access resource at " + uri);
			}
			if (fileRef.length() == 0) {
				throw new AssertionError("No content found at " + uri);
			}
		}
	}

	/**
	 * Asserts that the given kml:Update element is valid. More precisely, for each
	 * targetId attribute appearing in the update, the expression //kml:*[ {@literal @}id
	 * = targetId] must evaluate to {@code true} when evaluated against the referenced
	 * data (kml:targetHref).
	 * @param updateNode A node representing a kml:Update element.
	 */
	public static void assertValidUpdate(Node updateNode) {
		if (!updateNode.getLocalName().equals(KML22.UPDATE)) {
			return;
		}
		Element updateElem = (Element) updateNode;
		URI targetHref = URI
			.create(updateElem.getElementsByTagNameNS(KML22.NS_NAME, "targetHref").item(0).getTextContent().trim());
		if (!targetHref.isAbsolute()) {
			String docURI = updateNode.getOwnerDocument().getDocumentURI();
			targetHref = URIUtils.resolveRelativeURI(docURI, targetHref.toString());
		}
		Document kmlData;
		File kmlFile = null;
		try {
			try {
				kmlFile = URIUtils.dereferenceURI(targetHref);
				kmlData = URIUtils.parseURI(kmlFile.toURI());
			}
			catch (SAXException e) {
				// perhaps it's a KMZ file (ZIP archive)
				kmlData = KMLUtils.extractKMLFromArchive(kmlFile);
			}
		}
		catch (Exception e) {
			throw new AssertionError("Failed to parse KML resource from " + targetHref);
		}
		try {
			NodeList targetIdList = XMLUtils.evaluateXPath(updateNode, "//kml:*/@targetId", BaseFixture.NS_MAP);
			for (int i = 0; i < targetIdList.getLength(); i++) {
				Node targetIdNode = targetIdList.item(i);
				String expr = String.format("//kml:*[@id='%s']", targetIdNode.getTextContent());
				ETSAssert.assertXPath(expr, kmlData, BaseFixture.NS_MAP);
			}
		}
		catch (XPathExpressionException xpe) {
			throw new RuntimeException(xpe);
		}
	}

}

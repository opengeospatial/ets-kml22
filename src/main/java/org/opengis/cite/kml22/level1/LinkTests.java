package org.opengis.cite.kml22.level1;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;

import javax.imageio.ImageIO;
import javax.xml.xpath.XPathExpressionException;

import org.glassfish.jersey.client.ClientResponse;
import org.opengis.cite.kml22.BaseFixture;
import org.opengis.cite.kml22.ETSAssert;
import org.opengis.cite.kml22.ErrorMessage;
import org.opengis.cite.kml22.KML22;
import org.opengis.cite.kml22.Namespaces;
import org.opengis.cite.kml22.util.ClientUtils;
import org.opengis.cite.kml22.util.TestSuiteLogger;
import org.opengis.cite.kml22.util.URIUtils;
import org.opengis.cite.kml22.util.XMLUtils;
import org.opengis.cite.validation.ErrorLocator;
import org.opengis.cite.validation.ErrorSeverity;
import org.opengis.cite.validation.ValidationErrorHandler;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Invocation.Builder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Contains test methods that apply to link elements and attributes that refer to various
 * kinds of external resources. Link elements include kml:Link,
 * <em>kml:AbstractOverlayType</em>, and kml:Icon.
 *
 * @see "OGC 07-134r2: OGC KML 2.2 - Abstract Test Suite"
 */
public class LinkTests extends BaseFixture {

	private Client client;

	/**
	 * Initializes the HTTP client component. It is configured to follow redirects (status
	 * code 3nn) and log the request/response messages to the test suite logger (at INFO
	 * level).
	 */
	@BeforeClass
	public void initHttpClient() {
		this.client = ClientUtils.buildClient();
	}

	/**
	 * [{@code Test}] Verifies that a link element (of type kml:LinkType) satisfies the
	 * following constraint: if present, the child kml:refreshInterval element has a
	 * positive value (&gt; 0).
	 *
	 * @see "OGC 07-134r2, ATC 9: Link elements"
	 */
	@Test(description = "Implements ATC 9")
	public void verifyLinkRefreshInterval() {
		NodeList linkNodes = null;
		try {
			linkNodes = XMLUtils.evaluateXPath(this.testSubject, "//kml:Link | //kml:Icon", NS_MAP);
		}
		catch (XPathExpressionException xpe) {
			TestSuiteLogger.log(Level.WARNING, "Error evaluating XPath expression", xpe);
		}
		ValidationErrorHandler errHandler = new ValidationErrorHandler();
		for (int i = 0; i < linkNodes.getLength(); i++) {
			Element link = (Element) linkNodes.item(i);
			NodeList refreshIntervalNodes = link.getElementsByTagNameNS(Namespaces.KML22, "refreshInterval");
			boolean refreshIntervalExists = (refreshIntervalNodes.getLength() > 0) ? true : false;
			if (refreshIntervalExists) {
				Double refreshInterval = Double.valueOf(refreshIntervalNodes.item(0).getTextContent());
				if (refreshInterval == null) {
					errHandler.addError(ErrorSeverity.ERROR, ErrorMessage.format("level1.LinkElements.err1"),
							new ErrorLocator(-1, -1, XMLUtils.getXPointer(link)));
					continue;
				}
				if (refreshInterval <= 0) {
					errHandler.addError(ErrorSeverity.ERROR, ErrorMessage.format("level1.LinkElements.err2"),
							new ErrorLocator(-1, -1, XMLUtils.getXPointer(link)));
				}
			}
		}
		Assert.assertFalse(errHandler.errorsDetected(), errHandler.toString());
	}

	/**
	 * [{@code Test}] Verifies that a link element (of type kml:LinkType) satisfies the
	 * following constraint: if present, the child kml:viewRefreshTime element has a
	 * positive value (&gt; 0).
	 *
	 * @see "OGC 07-134r2, ATC 9: Link elements"
	 */
	@Test(description = "Implements ATC 9")
	public void verifyLinkViewRefreshTime() {
		NodeList linkNodes = null;
		try {
			linkNodes = XMLUtils.evaluateXPath(this.testSubject, "//kml:Link | //kml:Icon", NS_MAP);
		}
		catch (XPathExpressionException xpe) {
			TestSuiteLogger.log(Level.WARNING, "Error evaluating XPath expression", xpe);
		}
		ValidationErrorHandler errHandler = new ValidationErrorHandler();
		for (int i = 0; i < linkNodes.getLength(); i++) {
			Element link = (Element) linkNodes.item(i);
			NodeList viewRefreshTimeNodes = ((Element) link).getElementsByTagNameNS(Namespaces.KML22,
					"viewRefreshTime");
			boolean viewRefreshTimeExists = (viewRefreshTimeNodes.getLength() > 0) ? true : false;
			if (viewRefreshTimeExists) {
				Double viewRefreshTime = Double.valueOf(viewRefreshTimeNodes.item(0).getTextContent());
				if (viewRefreshTime == null) {
					errHandler.addError(ErrorSeverity.ERROR, ErrorMessage.format("level1.LinkElements.err3"),
							new ErrorLocator(-1, -1, XMLUtils.getXPointer(link)));
					continue;
				}
				if (viewRefreshTime <= 0) {
					errHandler.addError(ErrorSeverity.ERROR, ErrorMessage.format("level1.LinkElements.err4"),
							new ErrorLocator(-1, -1, XMLUtils.getXPointer(link)));
				}
			}
		}
		Assert.assertFalse(errHandler.errorsDetected(), errHandler.toString());
	}

	/**
	 * [{@code Test}] Verifies that a link element (of type kml:LinkType) satisfies the
	 * following constraint: if present, the child kml:viewBoundScale element has a
	 * positive value (&gt; 0).
	 *
	 * @see "OGC 07-134r2, ATC 9: Link elements"
	 */
	@Test(description = "Implements ATC 9")
	public void verifyLinkViewBoundScale() {
		NodeList linkNodes = null;
		try {
			linkNodes = XMLUtils.evaluateXPath(this.testSubject, "//kml:Link | //kml:Icon", NS_MAP);
		}
		catch (XPathExpressionException xpe) {
			TestSuiteLogger.log(Level.WARNING, "Error evaluating XPath expression", xpe);
		}
		ValidationErrorHandler errHandler = new ValidationErrorHandler();
		for (int i = 0; i < linkNodes.getLength(); i++) {
			Element link = (Element) linkNodes.item(i);
			NodeList viewBoundScaleNodes = ((Element) link).getElementsByTagNameNS(Namespaces.KML22, "viewBoundScale");
			boolean viewBoundScaleExists = (viewBoundScaleNodes.getLength() > 0) ? true : false;
			if (viewBoundScaleExists) {
				Double viewBoundScale = Double.valueOf(viewBoundScaleNodes.item(0).getTextContent());
				if (viewBoundScale == null) {
					errHandler.addError(ErrorSeverity.ERROR, ErrorMessage.format("level1.LinkElements.err5"),
							new ErrorLocator(-1, -1, XMLUtils.getXPointer(link)));
					continue;
				}
				if (viewBoundScale <= 0) {
					errHandler.addError(ErrorSeverity.ERROR, ErrorMessage.format("level1.LinkElements.err6"),
							new ErrorLocator(-1, -1, XMLUtils.getXPointer(link)));
				}
			}
		}
		Assert.assertFalse(errHandler.errorsDetected(), errHandler.toString());
	}

	/**
	 * [{@code Test}] Verifies that a link element refers to the correct resource type,
	 * according to one of the following cases:
	 *
	 * <ol type="a">
	 * <li>if the parent element is kml:NetworkLink - a KML or KMZ resource;</li>
	 * <li>if the parent element is kml:Model - a textured 3D object resource;</li>
	 * <li>if the parent element is kml:GroundOverlay, kml:ScreenOverlay, or
	 * kml:PhotoOverlay - an image resource (see ATC 18).</li>
	 * </ol>
	 *
	 * @see "OGC 07-134r2, ATC 10: Link referent"
	 */
	@Test(description = "Implements ATC 10")
	public void verifyLinkReferent() {
		NodeList linkNodes = null;
		try {
			linkNodes = XMLUtils.evaluateXPath(this.testSubject, "//kml:Link | //kml:Icon", NS_MAP);
		}
		catch (XPathExpressionException xpe) {
			TestSuiteLogger.log(Level.WARNING, "Error evaluating XPath expression", xpe);
		}
		ValidationErrorHandler errHandler = new ValidationErrorHandler();
		for (int i = 0; i < linkNodes.getLength(); i++) {
			Element linkNode = (Element) linkNodes.item(i);
			Element parent = (Element) linkNode.getParentNode();
			String hrefUrl = getLinkHref(linkNode, errHandler);
			if (hrefUrl == null || hrefUrl.equals(""))
				continue;
			URI hrefUri = URI.create(hrefUrl.trim());
			// Validate link based on its parent element
			if (parent.getNamespaceURI().equals(Namespaces.KML22) && parent.getLocalName().equals("NetworkLink")) {
				checkNetworkLinkReferent(linkNode, hrefUri, errHandler);
			}
			else if (parent.getNamespaceURI().equals(Namespaces.KML22) && parent.getLocalName().equals("Model")) {
				checkModelReferent(linkNode, hrefUri, errHandler);
			}
			else if (parent.getNamespaceURI().equals(Namespaces.KML22) && (parent.getLocalName().equals("GroundOverlay")
					|| parent.getLocalName().equals("ScreenOverlay") || parent.getLocalName().equals("PhotoOverlay"))) {
				checkOverlayIconReferent(linkNode, hrefUrl, errHandler);
			}
			else {
				if (TestSuiteLogger.isLoggable(Level.FINER)) {
					TestSuiteLogger.log(Level.FINER, "Found Link element with unknown parent: "
							+ parent.getNamespaceURI() + ":" + parent.getLocalName());
				}
			}
		}
		Assert.assertFalse(errHandler.errorsDetected(), errHandler.toString());
	}

	/**
	 * [{@code Test}] Verifies that the kml:Icon/kml:href element refers to an image
	 * resource. The image format is expected to correspond to a registered image media
	 * type (PNG, JPEG, and GIF images are commonly used for this purpose). This test
	 * applies to kml:Icon elements in both kml:IconStyle and
	 * <em>kml:AbstractOverlayType</em> contexts.
	 *
	 * @see "OGC 07-134r2, ATC 18: Icon - href"
	 */
	@Test(description = "Implements ATC 18")
	public void verifyIconReferent() {
		NodeList iconNodes = null;
		try {
			iconNodes = XMLUtils.evaluateXPath(this.testSubject, "//kml:Icon[@href]", NS_MAP);
		}
		catch (XPathExpressionException xpe) {
			TestSuiteLogger.log(Level.WARNING, "Error evaluating XPath expression", xpe);
		}
		ValidationErrorHandler errHandler = new ValidationErrorHandler();
		for (int i = 0; i < iconNodes.getLength(); i++) {
			Element iconNode = (Element) iconNodes.item(i);
			String hrefUrl = getLinkHref(iconNode, errHandler);
			if (hrefUrl == null || hrefUrl.isEmpty())
				continue;
			checkOverlayIconReferent(iconNode, hrefUrl, errHandler);
		}
		Assert.assertFalse(errHandler.errorsDetected(), errHandler.toString());
	}

	/**
	 * [{@code Test}] Verifies that the kml:NetworkLinkControl/kml:minRefreshPeriod
	 * element has a non-negative value (&gt;=0).
	 *
	 * @see "OGC 07-134r2, ATC 20: NetworkLinkControl - minRefreshPeriod"
	 */
	@Test(description = "Implements ATC 20")
	public void verifyNetworkLinkControlRefresh() {
		NodeList minRefreshNodes = null;
		try {
			minRefreshNodes = XMLUtils.evaluateXPath(this.testSubject, "//kml:NetworkLinkControl/kml:minRefreshPeriod",
					NS_MAP);
		}
		catch (XPathExpressionException xpe) {
			TestSuiteLogger.log(Level.WARNING, "Error evaluating XPath expression", xpe);
		}
		ValidationErrorHandler errHandler = new ValidationErrorHandler();
		for (int i = 0; i < minRefreshNodes.getLength(); i++) {
			Node minRefresh = minRefreshNodes.item(i);
			if (Double.valueOf(minRefresh.getTextContent().trim()) < 0) {
				errHandler.addError(ErrorSeverity.ERROR, ErrorMessage.format("level1.NetworkLinkControl.err2"),
						new ErrorLocator(-1, -1, XMLUtils.getXPointer(minRefresh.getParentNode())));
			}
		}
		Assert.assertFalse(errHandler.errorsDetected(), errHandler.toString());
	}

	/**
	 * [{@code Test}] Verifies that the value of the kml:Update/kml:targetHref element
	 * satisfies all of the following constraints:
	 *
	 * <ol>
	 * <li>it is an absolute URL that refers to a KML or KMZ resource;</li>
	 * <li>the target resource contains the data to be updated.</li>
	 * </ol>
	 *
	 * <p>
	 * The kml:Update element specifies an update to a KML resource that has previously
	 * been retrieved via some kml:NetworkLink. The <a target="_blank" href=
	 * "https://developers.google.com/kml/documentation/updates">Updates page</a> in the
	 * <em>KML Developer's Guide</em> provides a detailed example of how updates are
	 * applied in practice.
	 * </p>
	 *
	 * <h4 style="margin-bottom: 0.5em">Sources</h4>
	 * <ul>
	 * <li>OGC 07-134r2, ATC 22: Update - targetHref</li>
	 * <li>OGC 07-147r2, cl. 13.3: kml:Update</li>
	 * <li><a target="_blank" href=
	 * "https://developers.google.com/kml/documentation/kmlreference#update">KML Reference
	 * - Update</a>
	 * </ul>
	 */
	@Test(description = "Implements ATC 22")
	public void verifyUpdateTargetExists() {
		NodeList updateTargets = null;
		try {
			updateTargets = XMLUtils.evaluateXPath(this.testSubject, "//kml:Update/kml:targetHref", NS_MAP);
		}
		catch (XPathExpressionException xpe) {
			TestSuiteLogger.log(Level.WARNING, "Error evaluating XPath expression", xpe);
		}
		ValidationErrorHandler errHandler = new ValidationErrorHandler();
		for (int i = 0; i < updateTargets.getLength(); i++) {
			Node updateTarget = updateTargets.item(i);
			try {
				ETSAssert.assertReferentExists("text()", updateTarget, this.client, MediaType.APPLICATION_XML,
						KML22.KML_MEDIA_TYPE, KML22.KMZ_MEDIA_TYPE);
			}
			catch (AssertionError e) {
				addHrefError(updateTarget, e.getMessage(), errHandler);
			}
			ETSAssert.assertValidUpdate(updateTarget.getParentNode());
		}
		Assert.assertFalse(errHandler.errorsDetected(), errHandler.toString());
	}

	/**
	 * [{@code Test}] Verifies that if a kml:Link or a kml:Icon element is not a
	 * descendant of kml:Update, then it contains a kml:href child element.
	 *
	 * @see "OGC 07-134r2, ATC 40: Link"
	 */
	@Test(description = "Implements ATC 40")
	public void verifyLinkHasHref() {
		NodeList linkNodes = null;
		try {
			linkNodes = XMLUtils.evaluateXPath(this.testSubject,
					"//kml:Link[not(ancestor::kml:Update)] | //kml:Icon[not(ancestor::kml:Update)]", NS_MAP);
		}
		catch (XPathExpressionException xpe) {
			TestSuiteLogger.log(Level.WARNING, "Error evaluating XPath expression", xpe);
		}
		ValidationErrorHandler errHandler = new ValidationErrorHandler();
		for (int i = 0; i < linkNodes.getLength(); i++) {
			Element link = (Element) linkNodes.item(i);
			if (link.getElementsByTagNameNS(Namespaces.KML22, "href").getLength() == 0) {
				errHandler.addError(ErrorSeverity.ERROR, ErrorMessage.format("level1.Link.err"),
						new ErrorLocator(-1, -1, XMLUtils.getXPointer(link)));
			}
		}
		Assert.assertFalse(errHandler.errorsDetected(), errHandler.toString());
	}

	/**
	 * Checks that a link element occurring within a kml:Overlay element refers to an
	 * image resource. Common image formats will be auto-detected. If a suitable
	 * {@link javax.imageio.ImageReader ImageReader} cannot be found, an error is
	 * reported.
	 *
	 * @see <a href="http://www.iana.org/assignments/media-types/image/" target=
	 * "_blank">Image media types</a>
	 * @param linkNode A link node (containing a child kml:href element).
	 * @param href An absolute URI (extracted from the link node) that refers to an image
	 * resource.
	 * @param errHandler The error handler that receives any errors that were detected.
	 */
	void checkOverlayIconReferent(Node linkNode, String href, ValidationErrorHandler errHandler) {
		URI uriRef = URI.create(href);
		try {
			ETSAssert.assertReferentExists(uriRef, linkNode.getOwnerDocument().getBaseURI(), this.client, "image/*");
		}
		catch (AssertionError e) {
			addHrefError(linkNode, e.getMessage(), errHandler);
			return;
		}
		BufferedImage image = null;
		try {
			image = readImageDataFromURI(uriRef);
		}
		catch (IOException iox) {
			addHrefError(linkNode, iox.getMessage(), errHandler);
		}
		if (null == image) {
			addHrefError(linkNode, "Failed to read image data from " + uriRef, errHandler);
		}
	}

	/**
	 * Attempts to create a BufferedImage using the data retrieved from some URI.
	 * @param uriRef An absolute URI reference ('http' or 'file' schemes).
	 * @return A BufferedImage object containing the image data, or <code>null</code>.
	 * @throws IOException If an error occurs while reading the image data.
	 */
	BufferedImage readImageDataFromURI(URI uriRef) throws IOException {
		BufferedImage image = null;
		if (uriRef.getScheme().equalsIgnoreCase("file")) {
			image = ImageIO.read(uriRef.toURL());
		}
		else {
			WebTarget resource = this.client.target(uriRef);
			Builder builder = resource.request("image/*");
			ClientResponse rsp = builder.get(ClientResponse.class);
			if (null != rsp.getLocation()) { // 3nn Redirection
				resource = this.client.target(rsp.getLocation());
				rsp = resource.request("image/*").get(ClientResponse.class);
			}
			int status = rsp.getStatus();
			if (status == Response.Status.OK.getStatusCode() && rsp.hasEntity()) {
				image = ImageIO.read(rsp.getEntityStream());
			}
		}
		return image;
	}

	/**
	 * Checks that Link elements which are children of a kml:Model refer to a (3D)
	 * graphics resource. No attempt is made to validate the format or content of the
	 * resource, although the media types "model/*" or "application/octet-stream" are
	 * expected.
	 * @param linkNode A link element containing a child kml:href element.
	 * @param uriRef A URI reference.
	 * @param errHandler The error handler that receives any errors that were detected.
	 */
	void checkModelReferent(Element linkNode, URI uriRef, ValidationErrorHandler errHandler) {
		try {
			ETSAssert.assertReferentExists(".//kml:href", linkNode, this.client, "model/*",
					MediaType.APPLICATION_OCTET_STREAM);
		}
		catch (AssertionError e) {
			addHrefError(linkNode, e.getMessage(), errHandler);
		}
	}

	/**
	 * Checks that the network link referent exists. If not, an error is added to the
	 * handler.
	 * @param linkNode A link element containing a child kml:href element.
	 * @param uriRef A URI reference.
	 * @param errHandler The error handler that receives any errors that were detected.
	 */
	void checkNetworkLinkReferent(Node linkNode, URI uriRef, ValidationErrorHandler errHandler) {
		try {
			ETSAssert.assertReferentExists(".//kml:href", linkNode, this.client, MediaType.APPLICATION_XML,
					KML22.KML_MEDIA_TYPE, KML22.KMZ_MEDIA_TYPE);
		}
		catch (AssertionError e) {
			addHrefError(linkNode, e.getMessage(), errHandler);
		}
	}

	/**
	 * Gets the value of the child kml:href element in a Link element. If the value is a
	 * relative URI it is resolved against the base URI of the associated Document node.
	 *
	 * <p>
	 * <strong>Note:</strong> A kml:Icon/kml:href value will include tile parameters if a
	 * kml:PhotoOverlay element has a child kml:ImagePyramid element (<code>level</code>,
	 * <code>x</code>, <code>y</code>). Substituting 0 (zero) for all parameter references
	 * should produce a URL that refers to the lowest resolution image.
	 * </p>
	 * @param linkElem An Element node representing a link element.
	 * @param errHandler The error handler that receives any errors that were detected.
	 * @return A String representing an absolute URI reference, or <code>null</code> if it
	 * was missing or not a valid URI.
	 */
	String getLinkHref(Element linkElem, ValidationErrorHandler errHandler) {
		NodeList hrefNodes = linkElem.getElementsByTagNameNS(Namespaces.KML22, "href");
		if (hrefNodes.getLength() == 0) {
			errHandler.addError(ErrorSeverity.ERROR, ErrorMessage.format("level1.LinkReferents.err5"),
					new ErrorLocator(-1, -1, XMLUtils.getXPointer(linkElem)));
			return null;
		}
		String href = hrefNodes.item(0).getTextContent().trim();
		if (href.contains("$")) {
			href = URIUtils.replaceImageTileParams(href);
		}
		try {
			URI uriRef = new URI(href);
			if (!uriRef.isAbsolute()) {
				String baseURI = linkElem.getOwnerDocument().getBaseURI();
				uriRef = URIUtils.resolveRelativeURI(baseURI, href);
				href = uriRef.toString();
			}
		}
		catch (URISyntaxException e) {
			errHandler.addError(ErrorSeverity.ERROR, ErrorMessage.format("level1.LinkReferents.err6"),
					new ErrorLocator(-1, -1, XMLUtils.getXPointer(linkElem)));
			return null;
		}
		return href;
	}

	/**
	 * @param linkNode linkNode
	 * @param details details
	 * @param errHandler errHandler
	 */
	void addHrefError(Node linkNode, String details, ValidationErrorHandler errHandler) {
		errHandler.addError(ErrorSeverity.ERROR, ErrorMessage.format("level1.LinkReferents.err1", details),
				new ErrorLocator(-1, -1, XMLUtils.getXPointer(linkNode.getParentNode())));
	}

}

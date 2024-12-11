package org.opengis.cite.kml22.level1;

import java.lang.reflect.Constructor;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import javax.xml.namespace.QName;
import javax.xml.transform.dom.DOMSource;
import javax.xml.xpath.XPathExpressionException;

import net.sf.saxon.dom.ElementOverNodeInfo;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;

import org.opengis.cite.kml22.BaseFixture;
import org.opengis.cite.kml22.ErrorMessage;
import org.opengis.cite.kml22.Namespaces;
import org.opengis.cite.kml22.util.TestSuiteLogger;
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
 * Contains test methods that verify constraints applicable to user-defined data elements
 * and their corresponding definitions.
 *
 * <h2 style="margin-bottom: 0.5em">Sources</h2>
 * <ul>
 * <li>OGC 07-147r2: OGC KML 2.2, cl. 9.8 (kml:Schema)</li>
 * <li><a href= "https://developers.google.com/kml/documentation/kmlreference#schema"
 * target="_blank">KML Reference - Schema</a></li>
 * </ul>
 */
public class UserDefinedDataTests extends BaseFixture {

	/**
	 * List of XML Schema data types that may be used to define simple fields.
	 */
	static final Set<QName> SIMPLE_FIELD_TYPES;
	static {
		final Set<QName> types = new HashSet<QName>();
		types.add(new QName(Namespaces.XSD, "string", "xsd"));
		types.add(new QName(Namespaces.XSD, "int", "xsd"));
		types.add(new QName(Namespaces.XSD, "unsignedInt", "xsd"));
		types.add(new QName(Namespaces.XSD, "short", "xsd"));
		types.add(new QName(Namespaces.XSD, "unsignedShort", "xsd"));
		types.add(new QName(Namespaces.XSD, "float", "xsd"));
		types.add(new QName(Namespaces.XSD, "double", "xsd"));
		types.add(new QName(Namespaces.XSD, "boolean", "xsd"));
		SIMPLE_FIELD_TYPES = Collections.unmodifiableSet(types);
	}

	/**
	 * Java type mappings for XML Schema datatypes (from JAXB specification).
	 */
	static final Map<QName, Class<?>> XSD_JAVA_MAPPINGS;
	static {
		final Map<QName, Class<?>> mappings = new HashMap<QName, Class<?>>();
		mappings.put(new QName(Namespaces.XSD, "string", "xsd"), String.class);
		mappings.put(new QName(Namespaces.XSD, "int", "xsd"), Integer.class);
		mappings.put(new QName(Namespaces.XSD, "unsignedInt", "xsd"), Long.class);
		mappings.put(new QName(Namespaces.XSD, "short", "xsd"), Short.class);
		mappings.put(new QName(Namespaces.XSD, "unsignedShort", "xsd"), Integer.class);
		mappings.put(new QName(Namespaces.XSD, "float", "xsd"), Float.class);
		mappings.put(new QName(Namespaces.XSD, "double", "xsd"), Double.class);
		mappings.put(new QName(Namespaces.XSD, "boolean", "xsd"), Boolean.class);
		XSD_JAVA_MAPPINGS = Collections.unmodifiableMap(mappings);
	}

	/**
	 * [{@code Test}] Verifies that a kml:Schema element has an 'id' attribute value.
	 *
	 * @see "OGC 07-134r2, ATC 25: Schema"
	 */
	@Test(description = "Implements ATC 25")
	public void schemaHasIdAttribute() {
		NodeList schemaNodes = this.testSubject.getElementsByTagNameNS(Namespaces.KML22, "Schema");
		ValidationErrorHandler errHandler = new ValidationErrorHandler();
		for (int i = 0; i < schemaNodes.getLength(); i++) {
			Element schema = (Element) schemaNodes.item(i);
			if (schema.getAttribute("id").isEmpty()) {
				errHandler.addError(ErrorSeverity.ERROR, ErrorMessage.format("level1.Schema.err"),
						new ErrorLocator(-1, -1, XMLUtils.getXPointer(schema)));
			}
		}
		Assert.assertFalse(errHandler.errorsDetected(), errHandler.toString());
	}

	/**
	 * [{@code Test}] Verifies that a kml:SimpleField element satisfies all of the
	 * following constraints:
	 * <ol>
	 * <li>it has a 'name' attribute;</li>
	 * <li>the value of the 'type' attribute is one of the following XML Schema data
	 * types:
	 * <ul>
	 * <li>xsd:string</li>
	 * <li>xsd:int</li>
	 * <li>xsd:unsignedInt</li>
	 * <li>xsd:short</li>
	 * <li>xsd:unsignedShort</li>
	 * <li>xsd:float</li>
	 * <li>xsd:double</li>
	 * <li>xsd:boolean</li>
	 * </ul>
	 * </li>
	 * </ol>
	 *
	 * @see "OGC 07-134r2, ATC 26: Schema - SimpleField"
	 */
	@Test(description = "Implements ATC 26")
	public void verifySimpleField() {
		NodeList simpleFieldNodes = this.testSubject.getElementsByTagNameNS(Namespaces.KML22, "SimpleField");
		ValidationErrorHandler errHandler = new ValidationErrorHandler();
		for (int i = 0; i < simpleFieldNodes.getLength(); i++) {
			Element field = (Element) simpleFieldNodes.item(i);
			if (field.getAttribute("name").isEmpty()) {
				errHandler.addError(ErrorSeverity.ERROR, ErrorMessage.format("level1.SchemaSimpleField.err1"),
						new ErrorLocator(-1, -1, XMLUtils.getXPointer(field)));
			}
			QName fieldType = getFieldType(field);
			if (!SIMPLE_FIELD_TYPES.contains(fieldType)) {
				errHandler.addError(ErrorSeverity.ERROR,
						ErrorMessage.format("level1.SchemaSimpleField.err2", field.getAttribute("name")),
						new ErrorLocator(-1, -1, XMLUtils.getXPointer(field)));
			}
		}
		Assert.assertFalse(errHandler.errorsDetected(), errHandler.toString());
	}

	/**
	 * [{@code Test}] Verifies that a kml:SchemaData element satisfies all of the
	 * following constraints:
	 * <ol>
	 * <li>the 'schemaUrl' attribute value is a URL with a fragment component that refers
	 * to a kml:Schema element;</li>
	 * <li>all kml:SimpleData child elements have a 'name' attribute that matches the name
	 * of a declared kml:SimpleField element in the corresponding Schema;</li>
	 * <li>the values of all kml:SimpleData child elements conform to their declared
	 * types.</li>
	 * </ol>
	 *
	 * @see "OGC 07-134r2, ATC 27: ExtendedData - SchemaData"
	 */
	@Test(description = "Implements ATC 27")
	public void verifySchemaData() {
		NodeList schemaDataNodes = this.testSubject.getElementsByTagNameNS(Namespaces.KML22, "SchemaData");
		ValidationErrorHandler errHandler = new ValidationErrorHandler();
		for (int i = 0; i < schemaDataNodes.getLength(); i++) {
			Element schemaData = (Element) schemaDataNodes.item(i);
			String schemaUrl = schemaData.getAttribute("schemaUrl");
			if (schemaUrl.isEmpty()) {
				errHandler.addError(ErrorSeverity.ERROR, ErrorMessage.format("level1.ExtendedDataSchemaData.err1"),
						new ErrorLocator(-1, -1, XMLUtils.getXPointer(schemaData)));
				continue;
			}
			URI schemaURI = URI.create(schemaUrl);
			String xpath = null;
			if (null == schemaURI.getScheme()) { // relative URI
				xpath = String.format("//kml:Schema[@id='%s']", schemaURI.getFragment());
			}
			else {
				// strip fragment identifier before dereferencing absolute URI
				int numSign = schemaURI.toString().indexOf('#');
				xpath = String.format("doc('%s')//kml:Schema[@id='%s']", schemaURI.toString().substring(0, numSign),
						schemaURI.getFragment());
			}
			DOMSource src = new DOMSource(this.testSubject, this.testSubject.getBaseURI());
			XdmValue result = null;
			try {
				result = XMLUtils.evaluateXPath2(src, xpath, NS_MAP);
			}
			catch (SaxonApiException sae) {
				TestSuiteLogger.log(Level.WARNING, "Failed to evaluate XPath expression", sae);
			}
			if (null == result || result.size() == 0) {
				errHandler.addError(ErrorSeverity.ERROR,
						ErrorMessage.format("level1.ExtendedDataSchemaData.err3", xpath),
						new ErrorLocator(-1, -1, XMLUtils.getXPointer(schemaData)));
				continue;
			}
			checkSimpleDataElements(schemaData, (XdmNode) result.itemAt(0), errHandler);
		}
		Assert.assertFalse(errHandler.errorsDetected(), errHandler.toString());
	}

	/**
	 * [{@code Test}] Verifies that the value of the 'name' attribute on a kml:Data
	 * element is unique within the context of the parent kml:ExtendedData element.
	 * @throws javax.xml.xpath.XPathExpressionException If an error occurred while
	 * evaluating an XPath expression.
	 * @see "OGC 07-134r2, ATC 28: ExtendedData - Data"
	 */
	@Test(description = "Implements ATC 28")
	public void verifyUntypedDataAreUnique() throws XPathExpressionException {
		NodeList extData = XMLUtils.evaluateXPath(this.testSubject, "//kml:ExtendedData[kml:Data]", NS_MAP);
		ValidationErrorHandler errHandler = new ValidationErrorHandler();
		Set<String> nameSet = new HashSet<String>();
		for (int i = 0; i < extData.getLength(); i++) {
			nameSet.clear();
			NodeList names = XMLUtils.evaluateXPath(extData.item(i), "kml:Data/@name", NS_MAP);
			for (int j = 0; j < names.getLength(); j++) {
				Node nameAttr = names.item(j);
				if (!nameSet.add(nameAttr.getTextContent())) {
					errHandler.addError(ErrorSeverity.ERROR,
							ErrorMessage.format("level1.ExtendedDataData.err", nameAttr.getTextContent()),
							new ErrorLocator(-1, -1, XMLUtils.getXPointer(extData.item(i))));
				}
			}
		}
		Assert.assertFalse(errHandler.errorsDetected(), errHandler.toString());
	}

	/**
	 * Checks that the content of a kml:SchemaData element conforms to the declarations in
	 * the referenced Schema.
	 * @param schemaData A kml:SchemaData element (with parent kml:ExtendedData).
	 * @param schemaNode An XdmNode representing the referenced kml:Schema element.
	 * @param errHandler An error handler that accepts reported constraint violations.
	 */
	void checkSimpleDataElements(Element schemaData, XdmNode schemaNode, ValidationErrorHandler errHandler) {
		NodeList simpleDataNodes = schemaData.getElementsByTagNameNS(Namespaces.KML22, "SimpleData");
		Element schema = (Element) ElementOverNodeInfo.wrap(schemaNode.getUnderlyingNode());
		// Collect all SimpleField declarations from kml:Schema
		Map<String, QName> schemaMap = new HashMap<String, QName>();
		NodeList simpleFieldNodes = schema.getElementsByTagNameNS(Namespaces.KML22, "SimpleField");
		for (int i = 0; i < simpleFieldNodes.getLength(); i++) {
			Element simpleField = (Element) simpleFieldNodes.item(i);
			String name = simpleField.getAttribute("name");
			schemaMap.put(name, getFieldType(simpleField));
		}
		for (int i = 0; i < simpleDataNodes.getLength(); i++) {
			Element simpleData = (Element) simpleDataNodes.item(i);
			String name = simpleData.getAttribute("name");
			if (!schemaMap.containsKey(name)) {
				errHandler.addError(ErrorSeverity.ERROR, ErrorMessage.format("level1.ExtendedDataSchemaData.err4"),
						new ErrorLocator(-1, -1, XMLUtils.getXPointer(simpleData)));
				continue;
			}
			String value = simpleData.getTextContent().trim();
			QName dataType = schemaMap.get(name);
			if (!valueConformsToType(value, dataType)) {
				errHandler.addError(ErrorSeverity.ERROR,
						ErrorMessage.format("level1.ExtendedDataSchemaData.err5",
								String.format("Value '%s' does not conform to the type %s", value, dataType)),
						new ErrorLocator(-1, -1, XMLUtils.getXPointer(simpleData)));
			}
		}
	}

	/**
	 * Determines whether or not the given datum value conforms to a specified XML Schema
	 * datatype. The assessment is based on the type mappings defined in the JAXB
	 * specification.
	 * @param value A String representing a datum value.
	 * @param dataType A QName identifying some simple datatype.
	 * @return {@code true} if the value conforms to the data type; {@code false}
	 * otherwise.
	 */
	boolean valueConformsToType(String value, QName dataType) {
		Class<?> type = XSD_JAVA_MAPPINGS.get(dataType);
		if (null == type) {
			return false;
		}
		Object obj = null;
		try {
			Constructor<?> ctor = type.getConstructor(String.class);
			obj = ctor.newInstance(value);
		}
		catch (Exception e) {
		}
		return (null != obj);
	}

	/**
	 * Determines the data type declared for a given kml:SimpleField element.
	 * @param field A kml:SimpleField element.
	 * @return A QName representing a (simple) XML Schema data type.
	 */
	QName getFieldType(Element field) {
		QName fieldType;
		String fieldTypeVal = field.getAttribute("type");
		if (fieldTypeVal.contains(":")) {
			String[] qName = fieldTypeVal.split(":");
			fieldType = new QName(field.lookupNamespaceURI(qName[0]), qName[1], qName[0]);
		}
		else {
			fieldType = new QName(Namespaces.XSD, fieldTypeVal);
		}
		return fieldType;
	}

}

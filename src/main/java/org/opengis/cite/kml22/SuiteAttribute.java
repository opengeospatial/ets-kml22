package org.opengis.cite.kml22;

import javax.xml.validation.Schema;

import org.w3c.dom.Document;

/**
 * An enumerated type defining ISuite attributes that may be set to constitute a shared
 * test fixture.
 */
@SuppressWarnings("rawtypes")
public enum SuiteAttribute {

	/**
	 * An immutable Schema object representing the complete KML 2.2 schema.
	 */
	KML_SCHEMA("kmlSchema", Schema.class),
	/**
	 * A DOM Document representation of the test subject or metadata about it.
	 */
	TEST_SUBJECT("testSubject", Document.class),
	/**
	 * An integer denoting the conformance level to check. A given conformance level
	 * includes all lower levels.
	 */
	LEVEL("level", Integer.class);

	private final Class attrType;

	private final String attrName;

	private SuiteAttribute(String attrName, Class attrType) {
		this.attrName = attrName;
		this.attrType = attrType;
	}

	/**
	 * <p>
	 * getType.
	 * </p>
	 * @return a {@link java.lang.Class} object
	 */
	public Class getType() {
		return attrType;
	}

	/**
	 * <p>
	 * getName.
	 * </p>
	 * @return a {@link java.lang.String} object
	 */
	public String getName() {
		return attrName;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(attrName);
		sb.append('(').append(attrType.getName()).append(')');
		return sb.toString();
	}

}

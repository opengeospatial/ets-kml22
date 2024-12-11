package org.opengis.cite.kml22;

/**
 * An enumerated type defining all recognized test run arguments.
 */
public enum TestRunArg {

	/**
	 * An absolute URI that refers to a representation of the test subject or metadata
	 * about it.
	 */
	IUT,
	/**
	 * An integer value denoting the conformance level to check. A given conformance level
	 * includes all lower levels.
	 */
	ICS;

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return name().toLowerCase();
	}

}

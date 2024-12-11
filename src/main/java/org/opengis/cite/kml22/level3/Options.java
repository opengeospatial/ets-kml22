package org.opengis.cite.kml22.level3;

import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import org.opengis.cite.kml22.BaseFixture;
import org.opengis.cite.kml22.ErrorMessage;
import org.opengis.cite.kml22.util.ValidationUtils;
import org.opengis.cite.kml22.util.XMLUtils;
import org.opengis.cite.validation.SchematronValidator;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Contains test methods for various optional constraints that may be satisfied by a KML
 * instance.
 *
 * <h2 style="margin-bottom: 0.5em">Sources</h2>
 * <ul>
 * <li>OGC 07-134r2: OGC KML 2.2 - Abstract Test Suite</li>
 * </ul>
 */
public class Options extends BaseFixture {

	/**
	 * [{@code Test}] Checks for the occurrence of deprecated elements.
	 *
	 * <h4 style="margin-bottom: 0.5em">Sources</h4>
	 * <ul>
	 * <li>OGC 07-134r2, ATC 71: BalloonStyle - color</li>
	 * <li>OGC 07-134r2, ATC 72: Metadata</li>
	 * <li>OGC 07-134r2, ATC 76: Snippet</li>
	 * <li>OGC 07-134r2, ATC 77: NetworkLink-Url</li>
	 * </ul>
	 */
	@Test(description = "Implements ATCs 71,72,76,77")
	public void deprecatedElements() {
		SchematronValidator validator = ValidationUtils.buildSchematronValidator("kml-2.2.sch", "Deprecated");
		DOMResult result = (DOMResult) validator
			.validate(new DOMSource(this.testSubject, this.testSubject.getDocumentURI()));
		Assert.assertFalse(validator.ruleViolationsDetected(), ErrorMessage.format("NotSchemaValid",
				validator.getRuleViolationCount(), XMLUtils.writeNodeToString(result.getNode())));
	}

}

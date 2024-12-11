package org.opengis.cite.kml22.level2;

import org.opengis.cite.kml22.SuiteAttribute;
import org.opengis.cite.kml22.TestRunArg;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.BeforeTest;

/**
 * Conformance Level 2 extends Level 1 by adding test cases covering recommended
 * constraints that <em>should</em> be satisfied by a KML document.
 *
 * @see <a target="_href" href=
 * "http://portal.opengeospatial.org/files/?artifact_id=27811">OGC KML 2.2 - Abstract Test
 * Suite</a>
 */
public class Level2Tests {

	/**
	 * Checks if any conformance level 2 tests will be run. These tests will be run only
	 * if the test run argument {@link org.opengis.cite.kml22.TestRunArg#ICS ics} has a
	 * value &gt; 1; otherwise the tests are skipped.
	 * @param testContext The test (group) context.
	 */
	@BeforeTest
	public void runConformanceLevel2(ITestContext testContext) {
		Object obj = testContext.getSuite().getAttribute(SuiteAttribute.LEVEL.getName());
		if ((null != obj)) {
			Integer level = Integer.class.cast(obj);
			Assert.assertTrue(level.intValue() > 1, "Conformance level 2 will not be checked since ics = " + level);
		}
	}

}

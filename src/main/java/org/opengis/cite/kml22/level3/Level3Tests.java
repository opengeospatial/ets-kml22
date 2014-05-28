package org.opengis.cite.kml22.level3;

import org.opengis.cite.kml22.SuiteAttribute;
import org.opengis.cite.kml22.TestRunArg;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.BeforeTest;

/**
 * Conformance Level 3 extends Level 2 by adding test cases addressing
 * constraints that fall into the category of "best practices" but are not
 * essential in every circumstance. However, some parties may require
 * conformance at this level.
 * 
 * @see <a target="_href"
 *      href="http://portal.opengeospatial.org/files/?artifact_id=27811">OGC KML
 *      2.2 - Abstract Test Suite</a>
 */
public class Level3Tests {

    /**
     * Checks if conformance level 3 tests will be run. These tests will be run
     * only if the test run argument {@link TestRunArg#ICS ics} has a value &gt; 2;
     * otherwise the tests are skipped.
     * 
     * @param testContext
     *            The test (group) context.
     */
    @BeforeTest
    public void runConformanceLevel3(ITestContext testContext) {
        Object obj = testContext.getSuite().getAttribute(
                SuiteAttribute.LEVEL.getName());
        if ((null != obj)) {
            Integer level = Integer.class.cast(obj);
            Assert.assertTrue(level.intValue() > 2,
                    "Conformance level 3 will not be checked since ics = "
                            + level);
        }
    }

}

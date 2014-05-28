package org.opengis.cite.kml22;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.logging.Level;

import javax.xml.validation.Schema;

import org.opengis.cite.kml22.util.KMLUtils;
import org.opengis.cite.kml22.util.TestSuiteLogger;
import org.opengis.cite.kml22.util.URIUtils;
import org.opengis.cite.kml22.util.ValidationUtils;
import org.opengis.cite.kml22.util.XMLUtils;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.Reporter;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * A listener that performs various tasks before and after a test suite is run,
 * usually concerned with maintaining a shared test suite fixture. Since this
 * listener is loaded using the ServiceLoader mechanism, its methods will be
 * called before those of other suite listeners listed in the test suite
 * definition and before any annotated configuration methods.
 * 
 * Attributes set on an ISuite instance are not inherited by constituent test
 * group contexts (ITestContext). However, suite attributes are still accessible
 * from lower contexts.
 * 
 * @see org.testng.ISuite ISuite interface
 */
public class SuiteFixtureListener implements ISuiteListener {

    @Override
    public void onStart(ISuite suite) {
        Schema kmlSchema = ValidationUtils.createKMLSchema();
        if (null != kmlSchema) {
            suite.setAttribute(SuiteAttribute.KML_SCHEMA.getName(), kmlSchema);
        }
        processSuiteParameters(suite);
    }

    @Override
    public void onFinish(ISuite suite) {
        Reporter.clear(); // clear output from previous test runs
        Reporter.log("Test suite parameters:");
        Reporter.log(suite.getXmlSuite().getAllParameters().toString());
    }

    /**
     * Processes test suite arguments and sets suite attributes accordingly. The
     * entity referenced by the {@link TestRunArg#IUT iut} argument is parsed
     * and the resulting Document is set as the value of the "testSubject"
     * attribute.
     * <p>
     * The {@link TestRunArg#ICS ics} argument value is set as the value of the
     * "level" attribute; if not specified the lowest conformance level will be
     * checked.
     * </p>
     * 
     * @param suite
     *            An ISuite object representing a TestNG test suite.
     */
    void processSuiteParameters(ISuite suite) {
        Map<String, String> params = suite.getXmlSuite().getParameters();
        TestSuiteLogger.log(Level.CONFIG,
                "Suite parameters\n" + params.toString());
        Integer level = new Integer(1);
        if (null != params.get(TestRunArg.ICS.toString())) {
            try {
                level = Integer.valueOf(params.get(TestRunArg.ICS.toString()));
            } catch (NumberFormatException nfe) { // use default value instead
            }
        }
        suite.setAttribute(SuiteAttribute.LEVEL.getName(), level);
        String iutParam = params.get(TestRunArg.IUT.toString());
        if ((null == iutParam) || iutParam.isEmpty()) {
            throw new IllegalArgumentException(
                    "Required test run parameter not found: "
                            + TestRunArg.IUT.toString());
        }
        URI iutRef = URI.create(iutParam.trim());
        File entityFile = null;
        try {
            entityFile = URIUtils.dereferenceURI(iutRef);
        } catch (IOException iox) {
            // push exception up through ISuiteListener interface
            throw new RuntimeException("Unable to access resource located at "
                    + iutRef, iox);
        }
        Document kmlDoc = null;
        try {
            try {
                kmlDoc = URIUtils.parseURI(entityFile.toURI());
            } catch (SAXException e) {
                // perhaps it's a KMZ file (ZIP archive)
                kmlDoc = KMLUtils.extractKMLFromArchive(entityFile);
            }
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to read KML document from file at "
                            + entityFile.toURI(), e);
        }
        suite.setAttribute(SuiteAttribute.TEST_SUBJECT.getName(), kmlDoc);
        if (TestSuiteLogger.isLoggable(Level.FINE)) {
            StringBuilder logMsg = new StringBuilder(
                    "Parsed resource retrieved from ");
            logMsg.append(iutRef).append("\n");
            logMsg.append(XMLUtils.writeNodeToString(kmlDoc));
            TestSuiteLogger.log(Level.FINE, logMsg.toString());
        }
    }
}

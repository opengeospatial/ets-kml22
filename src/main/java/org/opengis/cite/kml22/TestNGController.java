package org.opengis.cite.kml22;

import com.occamlab.te.spi.executors.TestRunExecutor;
import com.occamlab.te.spi.executors.testng.TestNGExecutor;
import com.occamlab.te.spi.jaxrs.TestSuiteController;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.opengis.cite.kml22.util.TestSuiteLogger;
import org.w3c.dom.Document;

/**
 * Main test run controller oversees execution of TestNG test suites.
 */
public class TestNGController implements TestSuiteController {

    private TestRunExecutor executor;
    private Properties etsProperties = new Properties();

    /**
     * A convenience method to facilitate test development.
     * 
     * @param args
     *            Test run arguments (optional). The first argument must refer
     *            to an XML properties file containing the expected set of test
     *            run arguments. If no argument is supplied, the file located at
     *            ${user.home}/test-run-props.xml will be used.
     * @throws Exception
     *             If the test run cannot be executed (usually due to
     *             unsatisfied pre-conditions).
     */
    public static void main(String[] args) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        File xmlArgs = null;
        if (args.length > 0) {
            xmlArgs = (args[0].startsWith("file:")) ? new File(
                    URI.create(args[0])) : new File(args[0]);
        } else {
            String homeDir = System.getProperty("user.home");
            xmlArgs = new File(homeDir, "test-run-props.xml");
        }
        if (!xmlArgs.exists()) {
            throw new IllegalArgumentException(
                    "Test run arguments not found at " + xmlArgs);
        }
        Document testRunArgs = db.parse(xmlArgs);
        TestNGController controller = new TestNGController();
        Source testResults = controller.doTestRun(testRunArgs);
        System.out.println("Test results: " + testResults.getSystemId());
    }

    /**
     * Default constructor uses the location given by the "user.home" system
     * property as the root output directory.
     */
    public TestNGController() {
        this(new File(System.getProperty("user.home")).toURI().toString());
    }

    /**
     * Construct a controller that writes results to the given output directory.
     * 
     * @param outputDirUri
     *            A file URI that specifies the location of the directory in
     *            which test results will be written. It will be created if it
     *            does not exist.
     */
    public TestNGController(String outputDirUri) {
        InputStream is = getClass().getResourceAsStream("ets.properties");
        try {
            this.etsProperties.load(is);
        } catch (IOException ex) {
            TestSuiteLogger.log(Level.WARNING,
                    "Unable to load ets.properties. " + ex.getMessage());
        }
        URL tngSuite = TestNGController.class.getResource("testng.xml");
        File resultsDir = new File(URI.create(outputDirUri));
        TestSuiteLogger.log(Level.CONFIG, "Using TestNG config: " + tngSuite);
        TestSuiteLogger.log(Level.CONFIG,
                "Using outputDir: " + resultsDir.getAbsolutePath());
        // NOTE: setting third argument to 'true' enables the default listeners
        this.executor = new TestNGExecutor(tngSuite.toString(),
                resultsDir.getAbsolutePath(), false);
    }

    @Override
    public String getCode() {
        return etsProperties.getProperty("ets-code");
    }

    @Override
    public String getVersion() {
        return etsProperties.getProperty("ets-version");
    }

    @Override
    public String getTitle() {
        return etsProperties.getProperty("ets-title");
    }

    @Override
    public Source doTestRun(Document testRunArgs) throws Exception {
        validateTestRunArgs(testRunArgs);
        return executor.execute(testRunArgs);
    }

    /**
     * Validates the given set of test run arguments. The test run is aborted if
     * any checks fail.
     * 
     * @param testRunArgs
     *            A DOM Document containing a set of XML properties (key-value
     *            pairs).
     * @throws Exception
     *             If any arguments are missing or invalid for some reason.
     */
    void validateTestRunArgs(Document testRunArgs) throws Exception {
        if (null == testRunArgs
                || testRunArgs.getElementsByTagName("entry").getLength() == 0) {
            throw new Exception("No test run arguments were supplied.");
        }
        XPath xpath = XPathFactory.newInstance().newXPath();
        Boolean hasIUTKey = (Boolean) xpath.evaluate(
                String.format("//entry[@key='%s']", TestRunArg.IUT),
                testRunArgs, XPathConstants.BOOLEAN);
        if (!hasIUTKey) {
            throw new Exception(String.format(
                    "Missing argument: '%s' must be present.", TestRunArg.IUT));
        }
    }
}

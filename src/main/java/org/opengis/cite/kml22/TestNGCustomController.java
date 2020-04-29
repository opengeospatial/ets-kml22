package org.opengis.cite.kml22;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.opengis.cite.kml22.util.TestSuiteLogger;
import org.testng.TestNG;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.occamlab.te.spi.executors.testng.AlterSuiteParametersListener;

/**
 * <code>TestNGCustomController</code> is a class which will provide interface
 * to run single test or selected test methods.
 * Add test method names with comma separated values by specifying element
 * 'includetestmethods' in test-run-props.xml.
 */
public class TestNGCustomController {

    private Properties etsProperties = new Properties();
    private File resultsDir;
    private URI testngConfig;

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
            xmlArgs = (args[0].startsWith("file:")) ? new File(URI.create(args[0])) : new File(args[0]);
        } else {
            String homeDir = System.getProperty("user.home");
            xmlArgs = new File(homeDir, "test-run-props.xml");
        }
        if (!xmlArgs.exists()) {
            throw new IllegalArgumentException("Test run arguments not found at " + xmlArgs);
        }
        Document testRunArgs = db.parse(xmlArgs);
        TestNGCustomController controller = new TestNGCustomController();
        Source testResults = controller.doTestRun(testRunArgs);
        System.out.println("Test results: " + testResults.getSystemId());
    }

    /**
     * Default constructor uses the location given by the "user.home" system
     * property as the root output directory.
     */
    public TestNGCustomController() {
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
    public TestNGCustomController(String outputDirUri) {
        InputStream is = getClass().getResourceAsStream("ets.properties");
        try {
            this.etsProperties.load(is);
        } catch (IOException ex) {
            TestSuiteLogger.log(Level.WARNING, "Unable to load ets.properties. " + ex.getMessage());
        }
        URL tngSuite = TestNGCustomController.class.getResource("testng.xml");
        this.testngConfig = URI.create(tngSuite.toString());
        this.resultsDir = new File(URI.create(outputDirUri));
        TestSuiteLogger.log(Level.CONFIG, "Using TestNG config: " + tngSuite);
        TestSuiteLogger.log(Level.CONFIG, "Using outputDir: " + resultsDir.getAbsolutePath());
    }

    public Source doTestRun(Document testRunArgs) throws Exception {
        validateTestRunArgs(testRunArgs);
        if (null == testRunArgs) {
            throw new IllegalArgumentException("No test run arguments were supplied.");
        }
        TestNG driver = new TestNG();
        List<String> testSuites = new ArrayList<>();
        File tngFile = new File(this.testngConfig);
        testSuites.add(tngFile.getAbsolutePath());
        driver.setTestSuites(testSuites);
        driver.setVerbose(0);
        // NOTE: setting argument to 'true' enables the default listeners
        driver.setUseDefaultListeners(false);
        UUID runId = UUID.randomUUID();
        File outputDir = new File(this.resultsDir, "testng");
        if (!outputDir.exists() && !outputDir.mkdirs()) {
            TestSuiteLogger.log(Level.CONFIG, "Failed to create output directory at " + outputDir);
            outputDir = new File(System.getProperty("java.io.tmpdir"));
        }
        File runDir = new File(outputDir, runId.toString());
        if (!runDir.mkdir()) {
            runDir = outputDir;
            TestSuiteLogger.log(Level.CONFIG, "Created test run directory at " + runDir.getAbsolutePath());
        }
        driver.setOutputDirectory(runDir.getAbsolutePath());
        AlterSuiteParametersListener listener = new AlterSuiteParametersListener();
        listener.setTestRunArgs(testRunArgs);
        listener.setTestRunId(runId);
        driver.addAlterSuiteListener(listener);
        TestFilterListener testFilterListener = new TestFilterListener(testRunArgs);
        driver.addListener(testFilterListener);
        driver.run();
        Source source = null;
        try {
            File resultsFile = new File(driver.getOutputDirectory() + File.separator + "testng-results.xml");
            InputStream inStream = new FileInputStream(resultsFile);
            InputSource inSource = new InputSource(new InputStreamReader(inStream, StandardCharsets.UTF_8));
            source = new SAXSource(inSource);
            source.setSystemId(resultsFile.toURI().toString());
        } catch (IOException e) {
            TestSuiteLogger.log(Level.SEVERE, "Error reading test results: " + e.getMessage());
        }
        return source;
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
        if (null == testRunArgs || testRunArgs.getElementsByTagName("entry").getLength() == 0) {
            throw new Exception("No test run arguments were supplied.");
        }
        TestSuiteLogger.log(Level.CONFIG, "testRunArgs type: " + testRunArgs.getClass().getName());
        XPath xpath = XPathFactory.newInstance().newXPath();
        XPathExpression xpe = xpath.compile(String.format("//entry[@key='%s']", TestRunArg.IUT));
        Boolean hasIUTKey = (Boolean) xpe.evaluate(testRunArgs, XPathConstants.BOOLEAN);
        if (!hasIUTKey) {
            throw new Exception(String.format("Missing argument: '%s' must be present.", TestRunArg.IUT));
        }
    }
}
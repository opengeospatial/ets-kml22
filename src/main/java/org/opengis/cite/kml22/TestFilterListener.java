package org.opengis.cite.kml22;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ITestContext;
import org.w3c.dom.Document;

/**
 * The class TestFilterListener used to filter the provided test method names
 * with available test methods in test suite.
 */
public class TestFilterListener implements IMethodInterceptor {

    private static Set<String> patterns;
    private String includeTestMethods;

    /**
     * Initialize includeTestMethods variable evaluating Xpath.
     * 
     * @param testRunArgs
     *            Test run arguments used for running tests
     * @throws Exception
     *             If xpath gets failed to execute.
     */
    public TestFilterListener(Document testRunArgs) throws Exception {
        XPath xpath = XPathFactory.newInstance().newXPath();
        XPathExpression xpe = xpath.compile(String.format("//entry[@key='includeTestMethods']"));
        this.includeTestMethods = (String) xpe.evaluate(testRunArgs, XPathConstants.STRING);
    }

    /**
     * Check provided test names are present in test suite.
     * 
     * @param testsToInclude
     *            Comma separated provided test method names.
     * @param currentTestName
     *            Test method name present in test suite.
     * @return A boolean value does test is present or absent.
     */
    private boolean includeTest(String testsToInclude, String currentTestName) {
        boolean result = false;
        if (patterns == null) {
            patterns = new HashSet<>();
            String[] testPatterns = testsToInclude.split(",");
            for (String testPattern : testPatterns) {
                testPattern = testPattern.trim();
                patterns.add(testPattern);
            }
        }
        for (String pattern : patterns) {
            if (pattern.equals(currentTestName)) {
                result = true;
                break;
            }
        }
        return result;
    }

    @Override
    public List<IMethodInstance> intercept(List<IMethodInstance> methods,
            ITestContext context) {
        List<IMethodInstance> result = new ArrayList<IMethodInstance>();

        if (this.includeTestMethods == null || this.includeTestMethods.trim().isEmpty()) {
            return methods;
        } else {
            for (int i = 0; i < methods.size(); i++) {
                IMethodInstance instns = methods.get(i);
                String testCase = instns.getMethod().getConstructorOrMethod().getName();
                if (includeTest(this.includeTestMethods, testCase)) {
                    result.add(instns);
                }
            }
        }
        return result;
    }
}

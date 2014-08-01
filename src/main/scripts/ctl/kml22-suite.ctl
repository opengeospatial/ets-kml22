<?xml version="1.0" encoding="UTF-8"?>
<ctl:package xmlns:ctl="http://www.occamlab.com/ctl"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:tns="http://www.opengis.net/cite/kml22"
  xmlns:saxon="http://saxon.sf.net/"
  xmlns:tec="java:com.occamlab.te.TECore"
  xmlns:tng="java:org.opengis.cite.kml22.TestNGController">

  <ctl:function name="tns:run-ets-kml22">
    <ctl:param name="testRunArgs">A Document node containing test run arguments (as XML properties).</ctl:param>
    <ctl:param name="outputDir">The directory in which the test results will be written.</ctl:param>
    <ctl:return>The test results as a Source object (root node).</ctl:return>
    <ctl:description>Runs the kml22 ${version} test suite.</ctl:description>
    <ctl:code>
      <xsl:variable name="controller" select="tng:new($outputDir)" />
      <xsl:copy-of select="tng:doTestRun($controller, $testRunArgs)" />
    </ctl:code>
  </ctl:function>

  <ctl:suite name="tns:ets-kml22-${version}">
    <ctl:title>KML 2.2 Validator</ctl:title>
    <ctl:description>
     Checks KML 2.2 instance documents for conformance to the OGC specification (OGC 07-147r2).
    </ctl:description>
    <ctl:starting-test>tns:Main</ctl:starting-test>
  </ctl:suite>

  <ctl:test name="tns:Main">
    <ctl:assertion>The test subject satisfies all applicable constraints.</ctl:assertion>
    <ctl:code>
      <xsl:variable name="form-data">
        <ctl:form method="POST" width="800" height="600" xmlns="http://www.w3.org/1999/xhtml">
          <h2>KML 2.2 Validator</h2>
          <div class="scope" 
			      style="background:#F0F8FF" bgcolor="#F0F8FF">
            <p>This validator verifies correct implementation of KML 2.2. It is expected that the implementation generates correct KML documents following these specifications:</p>
            <ul>
              <li><a href="http://portal.opengeospatial.org/files/?artifact_id=27810" 
                 target="_blank">OGC KML 2.2.0</a> (OGC 07-147r2)</li>
              <li><a href="http://portal.opengeospatial.org/files/?artifact_id=27811" 
                 target="_blank">OGC KML 2.2 - Abstract Test Suite</a> (OGC 07-134r2)</li>
            </ul>
            <p>Three conformance levels are defined in the specifications. Level 2 is based on Level 1 and Level 3 is based on Level 2. An implementation needs to pass at least level 1 to get an OGC certificate.</p>
            <ol>
              <li>Level 1: Mandatory constraints</li>
              <li>Level 2: Recommended constraints</li>
              <li>Level 3: Optional constraints</li>
            </ol>
          </div>
          
          <p>More details about this test can be found in the<a href="index.html" target="_blank"> description page</a>.
          </p>
          
        
          <fieldset style="background:#ccffff">
            <legend style="font-family: sans-serif; color: #000099; 
			                 background-color:#F0F8FF; border-style: solid; 
                       border-width: medium; padding:4px">KML (or KMZ) Document to Test</legend>
          
              <label class="form-label" for="uri">
                <h4 style="margin-bottom: 0.5em">Provide the location (URL) of the document</h4>
                <p>(For example: 'http://org.com/myDocument.kml' or 'file://myDocument.kml')</p>
              </label>
              <input id="uri" name="uri" size="96" type="text" value="https://developers.google.com/kml/documentation/KML_Samples.kml" />
            
              <label class="form-label" for="doc">
                <h4 style="margin-bottom: 0.5em">Upload the document form your local computer</h4>
              </label>
              <p>
              <input id="doc" name="doc" size="96" type="file" />
               </p>
            
            <p><strong>Note: </strong>If both a URL reference and a file are given below, the uploaded document takes precedence.</p>
            
            <h4>Select the conformance class you want to test against:</h4>
            
          
              <label class="form-label" for="level">Conformance class: </label>
              
              <input id="level-1" type="radio" name="level" value="1" checked="checked" />
              <label class="form-label" for="level-1"> Level 1 | </label>
                         
              <input id="level-2" type="radio" name="level" value="2" />
              <label class="form-label" for="level-2"> Level 2 (includes Level 1) | </label>
                         
              <input id="level-3" type="radio" name="level" value="3" />
              <label class="form-label" for="level-3"> Level 3 (includes Level 1 and Level 2) </label>
              <p></p>
          
          </fieldset>
          <p>
            <input class="form-button" type="submit" value="Start" />
            <input class="form-button" type="reset" value="Clear" />
          </p>
        </ctl:form>
      </xsl:variable>
      <xsl:variable name="uri" select="$form-data//value[@key='uri']" />
      <xsl:variable name="file" select="$form-data//value[@key='doc']/ctl:file-entry" />
      <xsl:variable name="test-run-props">
        <properties version="1.0">
          <entry key="iut">
            <xsl:choose>
              <xsl:when test="(string-length($uri) gt 0) and empty($file/@full-path)">
                <xsl:value-of select="$uri"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="concat('file:///', $file/@full-path)" />
              </xsl:otherwise>
            </xsl:choose>
          </entry>
          <entry key="ics"><xsl:value-of select="$form-data/values/value[@key='level']"/></entry>
        </properties>
      </xsl:variable>
      <xsl:variable name="testRunDir">
        <xsl:value-of select="tec:getTestRunDirectory($te:core)"/>
      </xsl:variable>
      <xsl:variable name="test-results">
        <ctl:call-function name="tns:run-ets-kml22">
          <ctl:with-param name="testRunArgs" select="$test-run-props"/>
          <ctl:with-param name="outputDir" select="$testRunDir" />
        </ctl:call-function>
      </xsl:variable>
      <xsl:call-template name="tns:testng-report">
        <xsl:with-param name="results" select="$test-results" />
        <xsl:with-param name="outputDir" select="$testRunDir" />
      </xsl:call-template>
      <xsl:variable name="summary-xsl" select="tec:findXMLResource($te:core, '/testng-summary.xsl')" />
      <ctl:message>
      <xsl:value-of select="saxon:transform(saxon:compile-stylesheet($summary-xsl), $test-results)"/>
See detailed test report in the TE_BASE/users/<xsl:value-of 
select="concat(substring-after($testRunDir, 'users/'), '/html/')" /> directory.
      </ctl:message>
      <xsl:if test="xs:integer($test-results/testng-results/@failed) gt 0">
        <xsl:for-each select="$test-results//test-method[@status='FAIL' and not(@is-config='true')]">
          <ctl:message>
Test method <xsl:value-of select="./@name"/>: <xsl:value-of select=".//message"/>
          </ctl:message>
        </xsl:for-each>
        <ctl:fail/>
      </xsl:if>
    </ctl:code>
  </ctl:test>

  <xsl:template name="tns:testng-report">
    <xsl:param name="results" />
    <xsl:param name="outputDir" />
    <xsl:variable name="stylesheet" select="tec:findXMLResource($te:core, '/testng-report.xsl')" />
    <xsl:variable name="reporter" select="saxon:compile-stylesheet($stylesheet)" />
    <xsl:variable name="report-params" as="node()*">
      <xsl:element name="testNgXslt.outputDir">
        <xsl:value-of select="concat($outputDir, '/html')" />
      </xsl:element>
    </xsl:variable>
    <xsl:copy-of select="saxon:transform($reporter, $results, $report-params)" />
  </xsl:template>
</ctl:package>

<?xml version="1.0" encoding="UTF-8"?>
<sch:schema id="kml-2.2" 
  xmlns:sch="http://purl.oclc.org/dsdl/schematron" 
  xml:lang="en"
  queryBinding="xslt2"
  see="https://portal.opengeospatial.org/files/?artifact_id=27810">

  <sch:title>ISO Schematron schema for OGC KML 2.2 instance documents.</sch:title>

  <sch:ns prefix="kml" uri="http://www.opengis.net/kml/2.2" />

  <sch:p>This schema specifies constraints concerning the structure and content 
  of OGC KML 2.2 documents (OGC 07-147r2).</sch:p>

  <sch:phase id="Deprecated">
    <sch:active pattern="DeprecatedElementsPattern" />
  </sch:phase>

  <sch:pattern id="DeprecatedElementsPattern">
    <sch:title>Reports the occurrence of deprecated elements.</sch:title>
    <sch:rule context="kml:Placemark | kml:NetworkLink | kml:Folder | kml:Document | kml:GroundOverlay | kml:PhotoOverlay | kml:ScreenOverlay">
      <sch:report flag="warning" diagnostics="parent"
        test="kml:Metadata">The kml:Metadata element is deprecated in KML features (OGC 07-134r2, ATC 72). Use kml:ExtendedData instead.</sch:report>
      <sch:report flag="warning" diagnostics="parent"
        test="kml:Snippet">The kml:Snippet element is deprecated in KML features (OGC 07-134r2, ATC 76). Use kml:snippet instead.</sch:report>
    </sch:rule>
    <sch:rule context="kml:BalloonStyle">
      <sch:report flag="warning"
        test="kml:color">The kml:BalloonStyle/kml:color element is deprecated (OGC 07-134r2, ATC 71). Use kml:bgColor instead.</sch:report>
    </sch:rule>
    <sch:rule context="kml:NetworkLink">
      <sch:report flag="warning"
        test="kml:Url">The kml:NetworkLink/kml:Url element is deprecated (OGC 07-134r2, ATC 77). Use kml:Link instead.</sch:report>
    </sch:rule>
  </sch:pattern>

  <sch:diagnostics>
    <sch:diagnostic id="parent" 
    xml:lang="en">Parent element is <sch:value-of select="local-name(.)"/>[@id='<sch:value-of select="@id"/>']</sch:diagnostic>
  </sch:diagnostics>

</sch:schema>

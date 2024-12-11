package org.opengis.cite.kml22.util;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Verifies the behavior of the KMLUtils class.
 */
public class VerifyKMLUtils {

	public VerifyKMLUtils() {
	}

	@Test
	public void extractKMLFromArchive() throws URISyntaxException, IOException, SAXException {
		URL fileUrl = this.getClass().getResource("/kmz/archive-1.kmz");
		File kmzFile = new File(fileUrl.toURI());
		Document kmlDoc = KMLUtils.extractKMLFromArchive(kmzFile);
		assertNotNull(kmlDoc);
		assertEquals("Document element has unexpected local name", "kml", kmlDoc.getDocumentElement().getLocalName());
		assertTrue("Expected document URI ending with 'doc.kml'.", kmlDoc.getDocumentURI().endsWith("doc.kml"));
	}

}

package org.opengis.cite.kml22.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * 
 * Provides various utility methods for reading or manipulating KML and KMZ
 * resources.
 */
public class KMLUtils {

    /**
     * Extracts the contents of the given ZIP archive and parses the first
     * root-level KML document found within it. The main KML document is
     * conventionally named <em>doc.kml</em> but this is not required; the
     * {@code .kml} extension is expected, however.
     * 
     * @param file
     *            A File object that presumably represents a KMZ file (ZIP
     *            archive).
     * @return A KML document, or {@code null} if a root-level KML file could
     *         not be found in the archive.
     * @throws IOException
     *             The file was not a valid ZIP archive or some other I/O error
     *             occurred.
     * @throws SAXException
     *             If a KML document was found but it is not well-formed.
     * 
     * @see <a
     *      href="https://developers.google.com/kml/documentation/kmzarchives">What
     *      is a KMZ File?</a>
     */
    public static Document extractKMLFromArchive(File file) throws IOException,
            SAXException {
        if (!file.exists()) {
            throw new IllegalArgumentException("File does not exist: "
                    + file.getAbsolutePath());
        }
        Document mainDoc = null;
        try (ZipFile zipFile = new ZipFile(file)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                File destFile = new File(file.getParent(), entry.getName());
                if (entry.isDirectory()) {
                    destFile.mkdirs();
                    continue;
                }
                InputStream input = zipFile.getInputStream(entry);
                OutputStream output = new FileOutputStream(destFile);
                int nBytes = IOUtils.copy(input, output);
                IOUtils.closeQuietly(input);
                IOUtils.closeQuietly(output);
                if ((null == mainDoc) && destFile.getName().endsWith(".kml")) {
                    mainDoc = URIUtils.parseURI(destFile.toURI());
                }
                if (TestSuiteLogger.isLoggable(Level.FINER)) {
                    TestSuiteLogger.log(Level.FINER, String.format(
                            "Extracted %d bytes to %s", nBytes,
                            destFile.toURI()));
                }
            }
        }
        return mainDoc;
    }

}

/*
 * Copyright 2004 Sun Microsystems, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package top.mrxiaom.pluginbase.resolver.plexus.util.xml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Character stream that handles (or at least attempts to) all the necessary Voodo to figure out the charset encoding of
 * the XML document within the stream.
 * <p>
 * IMPORTANT: This class is not related in any way to the org.xml.sax.XMLReader. This one IS a character stream.
 * <p>
 * All this has to be done without consuming characters from the stream, if not the XML parser will not recognized the
 * document as a valid XML. This is not 100% true, but it's close enough (UTF-8 BOM is not handled by all parsers right
 * now, XmlReader handles it and things work in all parsers).
 * <p>
 * The XmlReader class handles the charset encoding of XML documents in Files, raw streams and HTTP streams by offering
 * a wide set of constructors.
 * <P>
 * By default the charset encoding detection is lenient, the constructor with the lenient flag can be used for an script
 * (following HTTP MIME and XML specifications). All this is nicely explained by Mark Pilgrim in his blog,
 * <a href="http://diveintomark.org/archives/2004/02/13/xml-media-types"> Determining the character encoding of a
 * feed</a>.
 * <p>
 * 
 * @author Alejandro Abdelnur
 * @since 1.4.4
 */
public class XmlStreamReader
    extends XmlReader
{
    /**
     * Creates a Reader for a File.
     * <p>
     * It looks for the UTF-8 BOM first, if none sniffs the XML prolog charset, if this is also missing defaults to
     * UTF-8.
     * <p>
     * It does a lenient charset encoding detection, check the constructor with the lenient parameter for details.
     * <p>
     * 
     * @param file File to create a Reader from.
     * @throws IOException thrown if there is a problem reading the file.
     */
    public XmlStreamReader( File file )
        throws IOException
    {
        super( file );
    }

    /**
     * Creates a Reader for a raw InputStream.
     * <p>
     * It follows the same logic used for files.
     * <p>
     * It does a lenient charset encoding detection, check the constructor with the lenient parameter for details.
     * <p>
     * 
     * @param is InputStream to create a Reader from.
     * @throws IOException thrown if there is a problem reading the stream.
     */
    public XmlStreamReader( InputStream is )
        throws IOException
    {
        super( is );
    }

    /**
     * Creates a Reader using the InputStream of a URL.
     * <p>
     * If the URL is not of type HTTP and there is not 'content-type' header in the fetched data it uses the same logic
     * used for Files.
     * <p>
     * If the URL is a HTTP Url or there is a 'content-type' header in the fetched data it uses the same logic used for
     * an InputStream with content-type.
     * <p>
     * It does a lenient charset encoding detection, check the constructor with the lenient parameter for details.
     * <p>
     * 
     * @param url URL to create a Reader from.
     * @throws IOException thrown if there is a problem reading the stream of the URL.
     */
    public XmlStreamReader( URL url )
        throws IOException
    {
        super( url );
    }
}

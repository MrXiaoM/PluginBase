package top.mrxiaom.pluginbase.resolver.plexus.util;

/*
 * Copyright The Codehaus Foundation.
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
 */

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.charset.Charset;

import top.mrxiaom.pluginbase.resolver.plexus.util.xml.XmlStreamReader;

/**
 * Utility to create Readers from streams, with explicit encoding choice: platform default, XML, or specified.
 *
 * @author <a href="mailto:hboutemy@codehaus.org">Herve Boutemy</a>
 * @see Charset
 * @see <a href="http://java.sun.com/j2se/1.4.2/docs/guide/intl/encoding.doc.html">Supported encodings</a>
 *
 * @since 1.4.3
 */
public class ReaderFactory
{
    /**
     * Eight-bit Unicode Transformation Format. Every implementation of the Java platform is required to support this
     * character encoding.
     * 
     * @see Charset
     */
    public static final String UTF_8 = "UTF-8";

    /**
     * Create a new Reader with XML encoding detection rules.
     *
     * @param in not null input stream.
     * @return an XML reader instance for the input stream.
     * @throws IOException if any.
     * @see XmlStreamReader
     */
    public static XmlStreamReader newXmlReader( InputStream in )
        throws IOException
    {
        return new XmlStreamReader( in );
    }

    /**
     * Create a new Reader with XML encoding detection rules.
     *
     * @param file not null file.
     * @return an XML reader instance for the input file.
     * @throws IOException if any.
     * @see XmlStreamReader
     */
    public static XmlStreamReader newXmlReader( File file )
        throws IOException
    {
        return new XmlStreamReader( file );
    }

    /**
     * Create a new Reader with XML encoding detection rules.
     *
     * @param url not null url.
     * @return an XML reader instance for the input url.
     * @throws IOException if any.
     * @see XmlStreamReader
     */
    public static XmlStreamReader newXmlReader( URL url )
        throws IOException
    {
        return new XmlStreamReader( url );
    }

    /**
     * Create a new Reader with specified encoding.
     *
     * @param in not null input stream.
     * @param encoding not null supported encoding.
     * @return a reader instance for the input stream using the given encoding.
     * @throws UnsupportedEncodingException if any.
     * @see <a href="http://java.sun.com/j2se/1.4.2/docs/guide/intl/encoding.doc.html">Supported encodings</a>
     */
    public static Reader newReader( InputStream in, String encoding )
        throws UnsupportedEncodingException
    {
        return new InputStreamReader( in, encoding );
    }
}

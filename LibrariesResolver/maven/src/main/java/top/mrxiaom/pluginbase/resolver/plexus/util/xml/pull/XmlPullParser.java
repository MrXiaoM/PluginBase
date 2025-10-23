/* -*-             c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
// for license please see accompanying LICENSE.txt file (available also at http://www.xmlpull.org/)

package top.mrxiaom.pluginbase.resolver.plexus.util.xml.pull;

import java.io.InputStream;
import java.io.IOException;
import java.io.Reader;

/**
 * XML Pull Parser is an interface that defines parsing functionality provided in
 * <a href="http://www.xmlpull.org/">XMLPULL V1 API</a> (visit this website to learn more about API and its
 * implementations).
 * <p>
 * There are following different kinds of parser depending on which features are set:
 * <ul>
 * <li><b>non-validating</b> parser as defined in XML 1.0 spec when FEATURE_PROCESS_DOCDECL is set to true
 * <li><b>validating parser</b> as defined in XML 1.0 spec when FEATURE_VALIDATION is true (and that implies that
 * FEATURE_PROCESS_DOCDECL is true)
 * <li>when FEATURE_PROCESS_DOCDECL is false (this is default and if different value is required necessary must be
 * changed before parsing is started) then parser behaves like XML 1.0 compliant non-validating parser under condition
 * that <em>no DOCDECL is present</em> in XML documents (internal entities can still be defined with
 * defineEntityReplacementText()). This mode of operation is intended <b>for operation in constrained environments</b>
 * such as J2ME.
 * </ul>
 * <p>
 * There are two key methods: next() and nextToken(). While next() provides access to high level parsing events,
 * nextToken() allows access to lower level tokens.
 * <p>
 * The current event state of the parser can be determined by calling the <a href="#getEventType()">getEventType()</a>
 * method. Initially, the parser is in the <a href="#START_DOCUMENT">START_DOCUMENT</a> state.
 * <p>
 * The method <a href="#next()">next()</a> advances the parser to the next event. The int value returned from next
 * determines the current parser state and is identical to the value returned from following calls to getEventType ().
 * <p>
 * The following event types are seen by next()
 * <dl>
 * <dt><a href="#START_TAG">START_TAG</a>
 * <dd>An XML start tag was read.
 * <dt><a href="#TEXT">TEXT</a>
 * <dd>Text content was read; the text content can be retrieved using the getText() method. (when in validating mode
 * next() will not report ignorable whitespaces, use nextToken() instead)
 * <dt><a href="#END_TAG">END_TAG</a>
 * <dd>An end tag was read
 * <dt><a href="#END_DOCUMENT">END_DOCUMENT</a>
 * <dd>No more events are available
 * </dl>
 * <p>
 * after first next() or nextToken() (or any other next*() method) is called user application can obtain XML version,
 * standalone and encoding from XML declaration in following ways:
 * <ul>
 * <li><b>version</b>: getProperty(&quot;<a href=
 * "http://xmlpull.org/v1/doc/properties.html#xmldecl-version">http://xmlpull.org/v1/doc/properties.html#xmldecl-version</a>&quot;)
 * returns String ("1.0") or null if XMLDecl was not read or if property is not supported
 * <li><b>standalone</b>: getProperty(&quot;<a href=
 * "http://xmlpull.org/v1/doc/features.html#xmldecl-standalone">http://xmlpull.org/v1/doc/features.html#xmldecl-standalone</a>&quot;)
 * returns Boolean: null if there was no standalone declaration or if property is not supported otherwise returns
 * Boolean(true) if standalone="yes" and Boolean(false) when standalone="no"
 * <li><b>encoding</b>: obtained from getInputEncoding() null if stream had unknown encoding (not set in setInputStream)
 * and it was not declared in XMLDecl
 * </ul>
 * A minimal example for using this API may look as follows:
 * 
 * <pre>
 * import java.io.IOException;
 * import java.io.StringReader;
 *
 * import org.xmlpull.v1.XmlPullParser;
 * import org.xmlpull.v1.XmlPullParserException;
 * import org.xmlpull.v1.XmlPullParserFactory;
 *
 * public class SimpleXmlPullApp
 * {
 *
 *     public static void main (String args[])
 *         throws XmlPullParserException, IOException
 *     {
 *         XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
 *         factory.setNamespaceAware(true);
 *         XmlPullParser xpp = factory.newPullParser();
 *
 *         xpp.setInput( new StringReader ( "&lt;foo%gt;Hello World!&lt;/foo&gt;" ) );
 *         int eventType = xpp.getEventType();
 *         while (eventType != xpp.END_DOCUMENT) {
 *          if(eventType == xpp.START_DOCUMENT) {
 *              System.out.println("Start document");
 *          } else if(eventType == xpp.END_DOCUMENT) {
 *              System.out.println("End document");
 *          } else if(eventType == xpp.START_TAG) {
 *              System.out.println("Start tag "+xpp.getName());
 *          } else if(eventType == xpp.END_TAG) {
 *              System.out.println("End tag "+xpp.getName());
 *          } else if(eventType == xpp.TEXT) {
 *              System.out.println("Text "+xpp.getText());
 *          }
 *          eventType = xpp.next();
 *         }
 *     }
 * }
 * </pre>
 * <p>
 * The above example will generate the following output:
 * 
 * <pre>
 * Start document
 * Start tag foo
 * Text Hello World!
 * End tag foo
 * </pre>
 * 
 * For more details on API usage, please refer to the quick Introduction available at
 * <a href="http://www.xmlpull.org">http://www.xmlpull.org</a>
 *
 * @see #getName
 * @see #getNamespace
 * @see #getText
 * @see #next
 * @see #setInput
 * @see #START_DOCUMENT
 * @see #START_TAG
 * @see #TEXT
 * @see #END_TAG
 * @see #END_DOCUMENT
 * @author <a href="http://www-ai.cs.uni-dortmund.de/PERSONAL/haustein.html">Stefan Haustein</a>
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */

public interface XmlPullParser
{

    /** This constant represents the default namespace (empty string "") */
    String NO_NAMESPACE = "";

    // ----------------------------------------------------------------------------
    // EVENT TYPES as reported by next()

    /**
     * Signalize that parser is at the very beginning of the document and nothing was read yet. This event type can only
     * be observed by calling getEvent() before the first call to next(), nextToken, or nextTag()).
     *
     * @see #next
     */
    int START_DOCUMENT = 0;

    /**
     * Logical end of the xml document. Returned from getEventType, next() and nextToken() when the end of the input
     * document has been reached.
     * <p>
     * <strong>NOTE:</strong> calling again <a href="#next()">next()</a> or <a href="#nextToken()">nextToken()</a> will
     * result in exception being thrown.
     *
     * @see #next
     */
    int END_DOCUMENT = 1;

    /**
     * Returned from getEventType(), <a href="#next()">next()</a>, <a href="#nextToken()">nextToken()</a> when a start
     * tag was read. The name of start tag is available from getName(), its namespace and prefix are available from
     * getNamespace() and getPrefix() if <a href='#FEATURE_PROCESS_NAMESPACES'>namespaces are enabled</a>. See
     * getAttribute* methods to retrieve element attributes. See getNamespace* methods to retrieve newly declared
     * namespaces.
     *
     * @see #next
     * @see #getName
     * @see #getPrefix
     * @see #getNamespace
     * @see #getAttributeCount
     * @see #getDepth
     * @see #getNamespace
     */
    int START_TAG = 2;

    /**
     * Returned from getEventType(), <a href="#next()">next()</a>, or <a href="#nextToken()">nextToken()</a> when an end
     * tag was read. The name of start tag is available from getName(), its namespace and prefix are available from
     * getNamespace() and getPrefix().
     *
     * @see #next
     * @see #getName
     * @see #getPrefix
     * @see #getNamespace
     */
    int END_TAG = 3;

    /**
     * Character data was read and will is available by calling getText().
     * <p>
     * <strong>Please note:</strong> <a href="#next()">next()</a> will accumulate multiple events into one TEXT event,
     * skipping IGNORABLE_WHITESPACE, PROCESSING_INSTRUCTION and COMMENT events, In contrast,
     * <a href="#nextToken()">nextToken()</a> will stop reading text when any other event is observed. Also, when the
     * state was reached by calling next(), the text value will be normalized, whereas getText() will return
     * unnormalized content in the case of nextToken(). This allows an exact roundtrip without changing line ends when
     * examining low level events, whereas for high level applications the text is normalized appropriately.
     *
     * @see #next
     * @see #getText
     */
    int TEXT = 4;

    // ----------------------------------------------------------------------------
    // additional events exposed by lower level nextToken()

    /**
     * A CDATA sections was just read; this token is available only from calls to
     * <a href="#nextToken()">nextToken()</a>. A call to next() will accumulate various text events into a single event
     * of type TEXT. The text contained in the CDATA section is available by calling getText().
     *
     * @see #getText
     */
    int CDSECT = 5;

    /**
     * An entity reference was just read; this token is available from <a href="#nextToken()">nextToken()</a> only. The
     * entity name is available by calling getName(). If available, the replacement text can be obtained by calling
     * getTextt(); otherwise, the user is responsible for resolving the entity reference. This event type is never
     * returned from next(); next() will accumulate the replacement text and other text events to a single TEXT event.
     *
     * @see #getText
     */
    int ENTITY_REF = 6;

    /**
     * Ignorable whitespace was just read. This token is available only from <a href="#nextToken()">nextToken()</a>).
     * For non-validating parsers, this event is only reported by nextToken() when outside the root element. Validating
     * parsers may be able to detect ignorable whitespace at other locations. The ignorable whitespace string is
     * available by calling getText()
     * <p>
     * <strong>NOTE:</strong> this is different from calling the isWhitespace() method, since text content may be
     * whitespace but not ignorable. Ignorable whitespace is skipped by next() automatically; this event type is never
     * returned from next().
     *
     * @see #getText
     */
    int IGNORABLE_WHITESPACE = 7;

    /**
     * An XML processing instruction declaration was just read. This event type is available only via
     * <a href="#nextToken()">nextToken()</a>. getText() will return text that is inside the processing instruction.
     * Calls to next() will skip processing instructions automatically.
     *
     * @see #getText
     */
    int PROCESSING_INSTRUCTION = 8;

    /**
     * An XML comment was just read. This event type is this token is available via
     * <a href="#nextToken()">nextToken()</a> only; calls to next() will skip comments automatically. The content of the
     * comment can be accessed using the getText() method.
     *
     * @see #getText
     */
    int COMMENT = 9;

    /**
     * An XML document type declaration was just read. This token is available from
     * <a href="#nextToken()">nextToken()</a> only. The unparsed text inside the doctype is available via the getText()
     * method.
     *
     * @see #getText
     */
    int DOCDECL = 10;

    /**
     * This array can be used to convert the event type integer constants such as START_TAG or TEXT to to a string. For
     * example, the value of TYPES[START_TAG] is the string "START_TAG". This array is intended for diagnostic output
     * only. Relying on the contents of the array may be dangerous since malicious applications may alter the array,
     * although it is final, due to limitations of the Java language.
     */
    String[] TYPES = { "START_DOCUMENT", "END_DOCUMENT", "START_TAG", "END_TAG", "TEXT", "CDSECT", "ENTITY_REF",
        "IGNORABLE_WHITESPACE", "PROCESSING_INSTRUCTION", "COMMENT", "DOCDECL" };

    // ----------------------------------------------------------------------------
    // namespace related features

    /**
     * Look up the value of a property. The property name is any fully-qualified URI.
     * <p>
     * <strong>NOTE:</strong> unknown properties are <strong>always</strong> returned as null.
     *
     * @param name The name of property to be retrieved.
     * @return The value of named property.
     */
    Object getProperty( String name );

    /**
     * Set the input source for parser to the given reader and resets the parser. The event type is set to the initial
     * value START_DOCUMENT. Setting the reader to null will just stop parsing and reset parser state, allowing the
     * parser to free internal resources such as parsing buffers.
     * @param in the Reader
     */
    void setInput( Reader in )
    ;

    /**
     * Sets the input stream the parser is going to process. This call resets the parser state and sets the event type
     * to the initial value START_DOCUMENT.
     * <p>
     * <strong>NOTE:</strong> If an input encoding string is passed, it MUST be used. Otherwise, if inputEncoding is
     * null, the parser SHOULD try to determine input encoding following XML 1.0 specification (see below). If encoding
     * detection is supported then following feature <a href=
     * "http://xmlpull.org/v1/doc/features.html#detect-encoding">http://xmlpull.org/v1/doc/features.html#detect-encoding</a>
     * MUST be true and otherwise it must be false
     *
     * @param inputStream contains a raw byte input stream of possibly unknown encoding (when inputEncoding is null).
     * @param inputEncoding if not null it MUST be used as encoding for inputStream
     * @throws XmlPullParserException parsing issue
     */
    void setInput( InputStream inputStream, String inputEncoding )
        throws XmlPullParserException;

    /**
     * @return the input encoding if known, null otherwise. If setInput(InputStream, inputEncoding) was called with an
     * inputEncoding value other than null, this value must be returned from this method. Otherwise, if inputEncoding is
     * null and the parser supports the encoding detection feature
     * (<a href="http://xmlpull.org/v1/doc/features.html#detect-encoding">...</a>), it must return the detected encoding. If
     * setInput(Reader) was called, null is returned. After first call to next if XML declaration was present this
     * method will return encoding declared.
     */
    String getInputEncoding();

    /**
     * @return the URI corresponding to the given prefix, depending on current state of the parser.
     * <p>
     * If the prefix was not declared in the current scope, null is returned. The default namespace is included in the
     * namespace table and is available via getNamespace (null).
     * <p>
     * This method is a convenience method for
     *
     * <pre>
     * for ( int i = getNamespaceCount( getDepth() ) - 1; i &gt;= 0; i-- )
     * {
     *     if ( getNamespacePrefix( i ).equals( prefix ) )
     *     {
     *         return getNamespaceUri( i );
     *     }
     * }
     * return null;
     * </pre>
     * <p>
     * <strong>Please note:</strong> parser implementations may provide more efficient lookup, e.g. using a Hashtable.
     * The 'xml' prefix is bound to "<a href="http://www.w3.org/XML/1998/namespace">...</a>", as defined in the
     * <a href="http://www.w3.org/TR/REC-xml-names/#ns-using">Namespaces in XML</a> specification. Analogous, the
     * 'xmlns' prefix is resolved to <a href="http://www.w3.org/2000/xmlns/">http://www.w3.org/2000/xmlns/</a>
     * @param prefix given prefix
     */
    String getNamespace( String prefix );

    // --------------------------------------------------------------------------
    // miscellaneous reporting methods

    /**
     * @return the current depth of the element. Outside the root element, the depth is 0. The depth is incremented by 1
     * when a start tag is reached. The depth is decremented AFTER the end tag event was observed.
     *
     * <pre>
     * &lt;!-- outside --&gt;     0
     * &lt;root&gt;                  1
     *   sometext                 1
     *     &lt;foobar&gt;         2
     *     &lt;/foobar&gt;        2
     * &lt;/root&gt;              1
     * &lt;!-- outside --&gt;     0
     * </pre>
     */
    int getDepth();

    /**
     * @return a short text describing the current parser state, including the position, a description of the current
     * event and the data source if known. This method is especially useful to provide meaningful error messages and for
     * debugging purposes.
     */
    String getPositionDescription();

    /**
     * Returns the current line number, starting from 1. When the parser does not know the current line number or can
     * not determine it, -1 is returned (e.g. for WBXML).
     *
     * @return current line number or -1 if unknown.
     */
    int getLineNumber();

    /**
     * Returns the current column number, starting from 0. When the parser does not know the current column number or
     * can not determine it, -1 is returned (e.g. for WBXML).
     *
     * @return current column number or -1 if unknown.
     */
    int getColumnNumber();

    // --------------------------------------------------------------------------
    // TEXT related methods

    /**
     * @return Checks whether the current TEXT event contains only whitespace characters. For IGNORABLE_WHITESPACE, this is
     * always true. For TEXT and CDSECT, false is returned when the current event text contains at least one non-white
     * space character. For any other event type an exception is thrown.
     * <p>
     * <b>Please note:</b> non-validating parsers are not able to distinguish whitespace and ignorable whitespace,
     * except from whitespace outside the root element. Ignorable whitespace is reported as separate event, which is
     * exposed via nextToken only.
     * @throws XmlPullParserException parsing issue
     */
    boolean isWhitespace()
        throws XmlPullParserException;

    /**
     * @return  the text content of the current event as String. The value returned depends on current event type, for
     * example for TEXT event it is element content (this is typical case when next() is used). See description of
     * nextToken() for detailed description of possible returned values for different types of events.
     * <p>
     * <strong>NOTE:</strong> in case of ENTITY_REF, this method returns the entity replacement text (or null if not
     * available). This is the only case where getText() and getTextCharacters() return different values.
     *
     * @see #getEventType
     * @see #next
     */
    String getText();

    // --------------------------------------------------------------------------
    // START_TAG / END_TAG shared methods

    /**
     * @return the namespace URI of the current element. The default namespace is represented as empty string. If
     * namespaces are not enabled, an empty String ("") is always returned. The current event must be START_TAG or
     * END_TAG; otherwise, null is returned.
     */
    String getNamespace();

    /**
     * @return For START_TAG or END_TAG events, the (local) name of the current element is returned when namespaces are enabled.
     * When namespace processing is disabled, the raw name is returned. For ENTITY_REF events, the entity name is
     * returned. If the current event is not START_TAG, END_TAG, or ENTITY_REF, null is returned.
     * <p>
     * <b>Please note:</b> To reconstruct the raw element name when namespaces are enabled and the prefix is not null,
     * you will need to add the prefix and a colon to localName..
     */
    String getName();

    /**
     * @return the prefix of the current element. If the element is in the default namespace (has no prefix), null is
     * returned. If namespaces are not enabled, or the current event is not START_TAG or END_TAG, null is returned.
     */
    String getPrefix();

    /**
     * @return true if the current event is START_TAG and the tag is degenerated (e.g. &lt;foobar/&gt;).
     * <p>
     * <b>NOTE:</b> if the parser is not on START_TAG, an exception will be thrown.
     * @throws XmlPullParserException parsing issue
     */
    boolean isEmptyElementTag()
        throws XmlPullParserException;

    // --------------------------------------------------------------------------
    // START_TAG Attributes retrieval methods

    /**
     * @return the number of attributes of the current start tag, or -1 if the current event type is not START_TAG
     *
     * @see #getAttributeName
     * @see #getAttributeValue
     */
    int getAttributeCount();

    /**
     * Returns the local name of the specified attribute if namespaces are enabled or just attribute name if namespaces
     * are disabled. Throws an IndexOutOfBoundsException if the index is out of range or current event type is not
     * START_TAG.
     *
     * @param index zero based index of attribute
     * @return attribute name (null is never returned)
     */
    String getAttributeName( int index );

    /**
     * Returns the given attributes value. Throws an IndexOutOfBoundsException if the index is out of range or current
     * event type is not START_TAG.
     * <p>
     * <strong>NOTE:</strong> attribute value must be normalized (including entity replacement text if PROCESS_DOCDECL
     * is false) as described in <a href="http://www.w3.org/TR/REC-xml#AVNormalize">XML 1.0 section 3.3.3
     * Attribute-Value Normalization</a>
     *
     * @param index zero based index of attribute
     * @return value of attribute (null is never returned)
     */
    String getAttributeValue( int index );

    // --------------------------------------------------------------------------
    // actual parsing methods

    /**
     * @return the type of the current event (START_TAG, END_TAG, TEXT, etc.)
     *
     * @see #next()
     */
    int getEventType()
    ;

    /**
     * @return Get next parsing event - element content wil be coalesced and only one TEXT event must be returned for whole
     * element content (comments and processing instructions will be ignored and entity references must be expanded or
     * exception mus be thrown if entity reference can not be expanded). If element content is empty (content is "")
     * then no TEXT event will be reported.
     * <p>
     * <b>NOTE:</b> empty element (such as &lt;tag/&gt;) will be reported with two separate events: START_TAG, END_TAG - it
     * must be so to preserve parsing equivalency of empty element to &lt;tag&gt;&lt;/tag&gt;. (see isEmptyElementTag ())
     *
     * @see #isEmptyElementTag
     * @see #START_TAG
     * @see #TEXT
     * @see #END_TAG
     * @see #END_DOCUMENT
     * @throws XmlPullParserException parsing issue
     * @throws IOException io issue
     */
    int next()
        throws XmlPullParserException, IOException;

    // -----------------------------------------------------------------------------
    // utility methods to mak XML parsing easier ...

    /**
     * Test if the current event is of the given type and if the namespace and name do match. null will match any
     * namespace and any name. If the test is not passed, an exception is thrown. The exception text indicates the
     * parser position, the expected event and the current event that is not meeting the requirement.
     * <p>
     * Essentially it does this
     * 
     * <pre>
     * if ( type != getEventType() || ( namespace != null &amp;&amp; !namespace.equals( getNamespace() ) )
     *     || ( name != null &amp;&amp; !name.equals( getName() ) ) )
     *     throw new XmlPullParserException( "expected " + TYPES[type] + getPositionDescription() );
     * </pre>
     * @param type type
     * @param name name
     * @param namespace namespace
     * @throws XmlPullParserException parsing issue
     */
    void require( int type, String namespace, String name )
        throws XmlPullParserException;

    /**
     * If current event is START_TAG then if next element is TEXT then element content is returned or if next event is
     * END_TAG then empty string is returned, otherwise exception is thrown. After calling this function successfully
     * parser will be positioned on END_TAG.
     * <p>
     * The motivation for this function is to allow to parse consistently both empty elements and elements that has non
     * empty content, for example for input:
     * <ol>
     * <li>&lt;tag&gt;foo&lt;/tag&gt;
     * <li>&lt;tag&gt;&lt;/tag&gt; (which is equivalent to &lt;tag/&gt; both input can be parsed with the same code:
     * 
     * <pre>
     *   p.nextTag()
     *   p.requireEvent(p.START_TAG, "", "tag");
     *   String content = p.nextText();
     *   p.requireEvent(p.END_TAG, "", "tag");
     * </pre></li></ol>
     * 
     * This function together with nextTag make it very easy to parse XML that has no mixed content.
     * <p>
     * Essentially it does this
     * 
     * <pre>
     * if ( getEventType() != START_TAG )
     * {
     *     throw new XmlPullParserException( "parser must be on START_TAG to read next text", this, null );
     * }
     * int eventType = next();
     * if ( eventType == TEXT )
     * {
     *     String result = getText();
     *     eventType = next();
     *     if ( eventType != END_TAG )
     *     {
     *         throw new XmlPullParserException( "event TEXT it must be immediately followed by END_TAG", this, null );
     *     }
     *     return result;
     * }
     * else if ( eventType == END_TAG )
     * {
     *     return "";
     * }
     * else
     * {
     *     throw new XmlPullParserException( "parser must be on START_TAG or TEXT to read text", this, null );
     * }
     * </pre>
     * @return see description
     * @throws XmlPullParserException parsing issue
     * @throws IOException io issue
     */
    String nextText()
        throws XmlPullParserException, IOException;

    /**
     * Call next() and return event if it is START_TAG or END_TAG otherwise throw an exception. It will skip whitespace
     * TEXT before actual tag if any.
     * <p>
     * essentially it does this
     * 
     * <pre>
     * int eventType = next();
     * if ( eventType == TEXT &amp;&amp; isWhitespace() )
     * { // skip whitespace
     *     eventType = next();
     * }
     * if ( eventType != START_TAG &amp;&amp; eventType != END_TAG )
     * {
     *     throw new XmlPullParserException( "expected start or end tag", this, null );
     * }
     * return eventType;
     * </pre>
     * @return see description
     * @throws XmlPullParserException parsing issue
     * @throws
     * IOException io issue
     */
    int nextTag()
        throws XmlPullParserException, IOException;

}

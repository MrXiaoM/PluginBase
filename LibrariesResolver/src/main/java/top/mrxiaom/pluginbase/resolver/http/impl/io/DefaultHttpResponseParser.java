/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package top.mrxiaom.pluginbase.resolver.http.impl.io;

import java.io.IOException;

import top.mrxiaom.pluginbase.resolver.http.HttpResponse;
import top.mrxiaom.pluginbase.resolver.http.HttpResponseFactory;
import top.mrxiaom.pluginbase.resolver.http.NoHttpResponseException;
import top.mrxiaom.pluginbase.resolver.http.ParseException;
import top.mrxiaom.pluginbase.resolver.http.StatusLine;
import top.mrxiaom.pluginbase.resolver.http.config.MessageConstraints;
import top.mrxiaom.pluginbase.resolver.http.impl.DefaultHttpResponseFactory;
import top.mrxiaom.pluginbase.resolver.http.io.SessionInputBuffer;
import top.mrxiaom.pluginbase.resolver.http.message.LineParser;
import top.mrxiaom.pluginbase.resolver.http.message.ParserCursor;
import top.mrxiaom.pluginbase.resolver.http.util.CharArrayBuffer;

/**
 * HTTP response parser that obtain its input from an instance
 * of {@link SessionInputBuffer}.
 *
 * @since 4.2
 */
public class DefaultHttpResponseParser extends AbstractMessageParser<HttpResponse> {

    private final HttpResponseFactory responseFactory;
    private final CharArrayBuffer lineBuf;

    /**
     * Creates new instance of DefaultHttpResponseParser.
     *
     * @param buffer the session input buffer.
     * @param lineParser the line parser. If {@code null}
     *   {@link top.mrxiaom.pluginbase.resolver.http.message.BasicLineParser#INSTANCE} will be used
     * @param responseFactory the response factory. If {@code null}
     *   {@link DefaultHttpResponseFactory#INSTANCE} will be used.
     * @param constraints the message constraints. If {@code null}
     *   {@link MessageConstraints#DEFAULT} will be used.
     *
     * @since 4.3
     */
    public DefaultHttpResponseParser(
            final SessionInputBuffer buffer,
            final LineParser lineParser,
            final HttpResponseFactory responseFactory,
            final MessageConstraints constraints) {
        super(buffer, lineParser, constraints);
        this.responseFactory = responseFactory != null ? responseFactory :
            DefaultHttpResponseFactory.INSTANCE;
        this.lineBuf = new CharArrayBuffer(128);
    }

    @Override
    protected HttpResponse parseHead(
            final SessionInputBuffer sessionBuffer)
        throws IOException, ParseException {

        this.lineBuf.clear();
        final int readLen = sessionBuffer.readLine(this.lineBuf);
        if (readLen == -1) {
            throw new NoHttpResponseException("The target server failed to respond");
        }
        //create the status line from the status string
        final ParserCursor cursor = new ParserCursor(0, this.lineBuf.length());
        final StatusLine statusline = lineParser.parseStatusLine(this.lineBuf, cursor);
        return this.responseFactory.newHttpResponse(statusline, null);
    }

}

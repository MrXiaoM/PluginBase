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

package top.mrxiaom.pluginbase.resolver.http.message;

import java.io.Serializable;

import top.mrxiaom.pluginbase.resolver.http.Header;
import top.mrxiaom.pluginbase.resolver.http.HeaderElement;
import top.mrxiaom.pluginbase.resolver.http.ParseException;
import top.mrxiaom.pluginbase.resolver.http.annotation.Contract;
import top.mrxiaom.pluginbase.resolver.http.annotation.ThreadingBehavior;
import top.mrxiaom.pluginbase.resolver.http.util.Args;

/**
 * Implements a basic {@link Header}.
 *
 * @since 4.0
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE)
public class BasicHeader implements Header, Cloneable, Serializable {

    private static final HeaderElement[] EMPTY_HEADER_ELEMENTS = new HeaderElement[] {};

    private static final long serialVersionUID = -5427236326487562174L;

    private final String name;
    private final String value;

    /**
     * Constructs with name and value.
     *
     * @param name the header name
     * @param value the header value
     */
    public BasicHeader(final String name, final String value) {
        this.name = Args.notNull(name, "Name");
        this.value = value;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public HeaderElement[] getElements() throws ParseException {
        if (this.getValue() != null) {
            // result intentionally not cached, it's probably not used again
            return BasicHeaderValueParser.parseElements(this.getValue(), null);
        }
        return EMPTY_HEADER_ELEMENTS;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        // no need for non-default formatting in toString()
        return BasicLineFormatter.INSTANCE.formatHeader(null, this).toString();
    }

}

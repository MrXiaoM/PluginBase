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
package top.mrxiaom.pluginbase.resolver.http.client.utils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Stack;

import top.mrxiaom.pluginbase.resolver.http.HttpHost;
import top.mrxiaom.pluginbase.resolver.http.conn.routing.RouteInfo;
import top.mrxiaom.pluginbase.resolver.http.util.Args;
import top.mrxiaom.pluginbase.resolver.http.util.TextUtils;

/**
 * A collection of utilities for {@link URI URIs}, to workaround
 * bugs within the class or for ease-of-use features.
 *
 * @since 4.0
 */
public class URIUtils {

    /**
     * Flags that control how URI is being rewritten.
     *
     * @since 4.5.8
     */
    public enum UriFlag {
        DROP_FRAGMENT,
        NORMALIZE
    }

    /**
     * Empty set of uri flags.
     *
     * @since 4.5.8
     */
    public static final EnumSet<UriFlag> NO_FLAGS = EnumSet.noneOf(UriFlag.class);

    /**
     * Set of uri flags containing {@link UriFlag#DROP_FRAGMENT}.
     *
     * @since 4.5.8
     */
    public static final EnumSet<UriFlag> DROP_FRAGMENT = EnumSet.of(UriFlag.DROP_FRAGMENT);

    /**
     * Set of uri flags containing {@link UriFlag#NORMALIZE}.
     *
     * @since 4.5.8
     */
    public static final EnumSet<UriFlag> NORMALIZE = EnumSet.of(UriFlag.NORMALIZE);

    /**
     * Set of uri flags containing {@link UriFlag#DROP_FRAGMENT} and {@link UriFlag#NORMALIZE}.
     *
     * @since 4.5.8
     */
    public static final EnumSet<UriFlag> DROP_FRAGMENT_AND_NORMALIZE = EnumSet.of(UriFlag.DROP_FRAGMENT, UriFlag.NORMALIZE);

    /**
     * A convenience method for creating a new {@link URI} whose scheme, host
     * and port are taken from the target host, but whose path, query and
     * fragment are taken from the existing URI. What exactly is used and how
     * is driven by the passed in flags. The path is set to "/" if not explicitly specified.
     *
     * @param uri
     *            Contains the path, query and fragment to use.
     * @param target
     *            Contains the scheme, host and port to use.
     * @param flags
     *            True if the fragment should not be copied.
     *
     * @throws URISyntaxException
     *             If the resulting URI is invalid.
     * @since 4.5.8
     */
    public static URI rewriteURI(
            final URI uri,
            final HttpHost target,
            final EnumSet<UriFlag> flags) throws URISyntaxException {
        Args.notNull(uri, "URI");
        Args.notNull(flags, "URI flags");
        if (uri.isOpaque()) {
            return uri;
        }
        final URIBuilder uribuilder = new URIBuilder(uri);
        if (target != null) {
            uribuilder.setScheme(target.getSchemeName());
            uribuilder.setHost(target.getHostName());
            uribuilder.setPort(target.getPort());
        } else {
            uribuilder.setScheme(null);
            uribuilder.setHost(null);
            uribuilder.setPort(-1);
        }
        if (flags.contains(UriFlag.DROP_FRAGMENT)) {
            uribuilder.setFragment(null);
        }
        if (flags.contains(UriFlag.NORMALIZE)) {
            final List<String> originalPathSegments = uribuilder.getPathSegments();
            final List<String> pathSegments = new ArrayList<>(originalPathSegments);
            pathSegments.removeIf(String::isEmpty);
            if (pathSegments.size() != originalPathSegments.size()) {
                uribuilder.setPathSegments(pathSegments);
            }
        }
        if (uribuilder.isPathEmpty()) {
            uribuilder.setPathSegments("");
        }
        return uribuilder.build();
    }

    /**
     * A convenience method that creates a new {@link URI} whose scheme, host, port, path,
     * query are taken from the existing URI, dropping any fragment or user-information.
     * The path is set to "/" if not explicitly specified. The existing URI is returned
     * unmodified if it has no fragment or user-information and has a path.
     *
     * @param uri
     *            original URI.
     * @throws URISyntaxException
     *             If the resulting URI is invalid.
     */
    public static URI rewriteURI(final URI uri) throws URISyntaxException {
        Args.notNull(uri, "URI");
        if (uri.isOpaque()) {
            return uri;
        }
        final URIBuilder uribuilder = new URIBuilder(uri);
        if (uribuilder.getUserInfo() != null) {
            uribuilder.setUserInfo(null);
        }
        if (uribuilder.getPathSegments().isEmpty()) {
            uribuilder.setPathSegments("");
        }
        if (TextUtils.isEmpty(uribuilder.getPath())) {
            uribuilder.setPath("/");
        }
        if (uribuilder.getHost() != null) {
            uribuilder.setHost(uribuilder.getHost().toLowerCase(Locale.ROOT));
        }
        uribuilder.setFragment(null);
        return uribuilder.build();
    }

    /**
     * A convenience method that optionally converts the original {@link java.net.URI} either
     * to a relative or an absolute form as required by the specified route.
     *
     * @param uri
     *            original URI.
     * @throws URISyntaxException
     *             If the resulting URI is invalid.
     *
     * @since 4.5.8
     */
    public static URI rewriteURIForRoute(final URI uri, final RouteInfo route, final boolean normalizeUri) throws URISyntaxException {
        if (uri == null) {
            return null;
        }
        // Make sure the request URI is relative
        return uri.isAbsolute() ? rewriteURI(uri, null, normalizeUri ? DROP_FRAGMENT_AND_NORMALIZE : DROP_FRAGMENT) : rewriteURI(uri);
    }

    /**
     * Resolves a URI reference against a base URI. Work-around for bug in
     * java.net.URI (http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4708535)
     *
     * @param baseURI the base URI
     * @param reference the URI reference
     * @return the resulting URI
     */
    public static URI resolve(final URI baseURI, final String reference) {
        return resolve(baseURI, URI.create(reference));
    }

    /**
     * Resolves a URI reference against a base URI. Work-around for bugs in
     * java.net.URI (e.g. http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4708535)
     *
     * @param baseURI the base URI
     * @param reference the URI reference
     * @return the resulting URI
     */
    public static URI resolve(final URI baseURI, final URI reference){
        Args.notNull(baseURI, "Base URI");
        Args.notNull(reference, "Reference URI");
        final String s = reference.toASCIIString();
        if (s.startsWith("?")) {
            String baseUri = baseURI.toASCIIString();
            final int i = baseUri.indexOf('?');
            baseUri = i > -1 ? baseUri.substring(0, i) : baseUri;
            return URI.create(baseUri + s);
        }
        final boolean emptyReference = s.isEmpty();
        URI resolved;
        if (emptyReference) {
            resolved = baseURI.resolve(URI.create("#"));
            final String resolvedString = resolved.toASCIIString();
            resolved = URI.create(resolvedString.substring(0, resolvedString.indexOf('#')));
        } else {
            resolved = baseURI.resolve(reference);
        }
        try {
            return normalizeSyntax(resolved);
        } catch (final URISyntaxException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * Removes dot segments according to RFC 3986, section 5.2.4 and
     * Syntax-Based Normalization according to RFC 3986, section 6.2.2.
     *
     * @param uri the original URI
     * @return the URI without dot segments
     *
     * @since 4.5
     */
    public static URI normalizeSyntax(final URI uri) throws URISyntaxException {
        if (uri.isOpaque() || uri.getAuthority() == null) {
            // opaque and file: URIs
            return uri;
        }
        final URIBuilder builder = new URIBuilder(uri);
        final List<String> inputSegments = builder.getPathSegments();
        final Stack<String> outputSegments = new Stack<>();
        for (final String inputSegment : inputSegments) {
            if (".".equals(inputSegment)) {
                // Do nothing
            } else if ("..".equals(inputSegment)) {
                if (!outputSegments.isEmpty()) {
                    outputSegments.pop();
                }
            } else {
                outputSegments.push(inputSegment);
            }
        }
        if (outputSegments.isEmpty()) {
            outputSegments.add("");
        }
        builder.setPathSegments(outputSegments);
        if (builder.getScheme() != null) {
            builder.setScheme(builder.getScheme().toLowerCase(Locale.ROOT));
        }
        if (builder.getHost() != null) {
            builder.setHost(builder.getHost().toLowerCase(Locale.ROOT));
        }
        return builder.build();
    }

    /**
     * Extracts target host from the given {@link URI}.
     *
     * @return the target host if the URI is absolute or {@code null} if the URI is
     * relative or does not contain a valid host name.
     *
     * @since 4.1
     */
    public static HttpHost extractHost(final URI uri) {
        if (uri == null) {
            return null;
        }
        if (uri.isAbsolute()) {
            if (uri.getHost() == null) { // normal parse failed; let's do it ourselves
                // authority does not seem to care about the valid character-set for host names
                if (uri.getAuthority() != null) {
                    String content = uri.getAuthority();
                    // Strip off any leading user credentials
                    int at = content.indexOf('@');
                    if (at != -1) {
                        content = content.substring(at + 1);
                    }
                    final String scheme = uri.getScheme();
                    final String hostname;
                    final int port;
                    at = content.indexOf(":");
                    if (at != -1) {
                        hostname = content.substring(0, at);
                        try {
                            final String portText = content.substring(at + 1);
                            port = !TextUtils.isEmpty(portText) ? Integer.parseInt(portText) : -1;
                        } catch (final NumberFormatException ex) {
                            return null;
                        }
                    } else {
                        hostname = content;
                        port = -1;
                    }
                    try {
                        return new HttpHost(hostname, port, scheme);
                    } catch (final IllegalArgumentException ex) {
                        // ignore
                    }
                }
            } else {
                return new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme());
            }
        }
        return null;
    }

    /**
     * This class should not be instantiated.
     */
    private URIUtils() {
    }

}
